package com.cauchymop.goblob.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoogleApiClientListener;
import com.cauchymop.goblob.model.GoogleApiClientManager;
import com.cauchymop.goblob.proto.PlayGameData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.google.android.gms.games.Games.Achievements;
import static com.google.android.gms.games.Games.TurnBasedMultiplayer;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClientListener, AndroidGameRepository.GameRepositoryListener {

  private static final int RC_REQUEST_ACHIEVEMENTS = 1;
  private static final int RC_SELECT_PLAYER = 2;
  private static final int RC_CHECK_MATCHES = 3;
  private static final int RC_SIGN_IN = 4;

  private static final String TAG = MainActivity.class.getName();
  private static final String CURRENT_MATCH_ID = "CURRENT_MATCH_ID";

  @BindView(R.id.toolbar_match_spinner)
  Spinner matchSpinner;
  @BindView(R.id.app_toolbar)
  Toolbar toolbar;
  @BindView(R.id.waiting_view)
  View waitingScreen;

  private MatchesAdapter navigationSpinnerAdapter;
  private List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
  private boolean resolvingError;
  private boolean signInClicked;
  private boolean autoStartSignInFlow = true;

  @Inject
  GoogleApiClient googleApiClient;
  @Inject
  GameDatas gameDatas;
  @Inject
  AndroidGameRepository androidGameRepository;
  @Inject
  GoogleApiClientManager googleApiClientManager;
  private Unbinder unbinder;
  private GameFragment gameFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_main);
    unbinder = ButterKnife.bind(this);

    ((GoApplication) getApplication()).getComponent().inject(this);

    androidGameRepository.addGameRepositoryListener(this);

    setUpToolbar();

    if (savedInstanceState != null) {
      androidGameRepository.selectGame(savedInstanceState.getString(CURRENT_MATCH_ID));
    }

    googleApiClientManager.registerGoogleApiClientListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart");
    updateMatchSpinner();
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop");
    if (isSignedIn()) {
      googleApiClient.disconnect();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
    googleApiClientManager.unregisterGoogleApiClientListener(this);
    androidGameRepository.removeGameRepositoryListener(this);
    unbinder.unbind();
  }

  private void setUpToolbar() {
    // Set up the action bar to show a dropdown list.
    setSupportActionBar(toolbar);

    ActionBar supportActionBar = getSupportActionBar();
    supportActionBar.setDisplayShowTitleEnabled(false);
    navigationSpinnerAdapter = new MatchesAdapter(supportActionBar.getThemedContext(), matchMenuItems);

    matchSpinner.setAdapter(navigationSpinnerAdapter);
  }

  @OnItemSelected(R.id.toolbar_match_spinner)
  void onMatchItemSelected(int position) {
    MatchMenuItem item = navigationSpinnerAdapter.getItem(position);
    Log.d(TAG, "onItemSelected: " + item.getMatchId());
    androidGameRepository.selectGame(item.getMatchId());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_menu, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean signedIn = isSignedIn();
    menu.setGroupVisible(R.id.group_signedIn, signedIn);
    menu.setGroupVisible(R.id.group_signedOut, !signedIn);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_achievements) {
      startActivityForResult(Achievements.getAchievementsIntent(googleApiClient), RC_REQUEST_ACHIEVEMENTS);
      return true;
    } else if (id == R.id.menu_signout) {
      signOut();
    } else if (id == R.id.menu_signin) {
      Log.d(TAG, "signIn from menu");
      signInClicked = true;
      googleApiClient.connect();
    } else if (id == R.id.menu_check_matches) {
      checkMatches();
    }
    return false;
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    super.onActivityResult(requestCode, responseCode, intent);
    Log.d(TAG, String.format("onActivityResult requestCode = %d, responseCode = %d", requestCode, responseCode));
    switch (requestCode) {
      case RC_SELECT_PLAYER:
        if (responseCode == RESULT_OK) {
          androidGameRepository.handlePlayersSelected(intent);
        } else {
          setWaitingScreenVisible(false);
        }
        break;
      case RC_CHECK_MATCHES:
        androidGameRepository.handleCheckMatchesResult(responseCode, intent);
        break;
      case RC_REQUEST_ACHIEVEMENTS:
        break;
      case RC_SIGN_IN:
        signInClicked = false;
        resolvingError = false;
        if (responseCode == RESULT_OK) {
          googleApiClient.connect();
        } else {
          BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
        }
        break;
      default:
        Log.e(TAG, "onActivityResult unexpected requestCode " + requestCode);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString(CURRENT_MATCH_ID, androidGameRepository.getCurrentMatchId());
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.d(TAG, "onConnected");
    updateFromConnectionStatus();
    TurnBasedMultiplayer.registerMatchUpdateListener(googleApiClient, androidGameRepository);

    // Retrieve the TurnBasedMatch from the connectionHint in order to select it
    if (bundle != null) {
      TurnBasedMatch turnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
      androidGameRepository.selectGame(turnBasedMatch.getMatchId());
    }

    androidGameRepository.refreshRemoteGameListFromServer();
    androidGameRepository.publishUnpublishedGames();
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.d(TAG, "onConnectionSuspended");
    updateFromConnectionStatus();
    googleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult result) {
    Log.d(TAG, "onConnectionFailed(): attempting to resolve");
    if (resolvingError) {
      // Already resolving
      Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
      return;
    }
    updateFromConnectionStatus();

    // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
    if (signInClicked || autoStartSignInFlow) {
      autoStartSignInFlow = false;
      signInClicked = false;

      resolvingError = BaseGameUtils.resolveConnectionFailure(this, googleApiClient, result,
          RC_SIGN_IN, getString(R.string.signin_other_error));
    }
  }

  public void updateFromConnectionStatus() {
    Log.d(TAG, "updateFromConnectionStatus isSignedIn = " + isSignedIn());
    invalidateOptionsMenu();

    // When initial connection fails, there is no fragment yet.
    GoBlobBaseFragment currentFragment = getCurrentFragment();
    if (currentFragment != null) {
      currentFragment.updateFromConnectionStatus();
    }
  }

  private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems) {
    matchMenuItems.clear();
    matchMenuItems.addAll(newMatchMenuItems);

    matchMenuItems.add(new CreateNewGameMenuItem(getString(R.string.new_game_label)));
    navigationSpinnerAdapter.notifyDataSetChanged();

    selectMenuItem(androidGameRepository.getCurrentMatchId());
  }

  @Nullable
  private MatchMenuItem getCurrentMatchMenuItem() {
    Log.d(TAG, "before getSelectedItem(), matchSpinner = " + matchSpinner);
    Log.d(TAG, "MainActivity = " + this);
    return (MatchMenuItem) matchSpinner.getSelectedItem();
  }

  private List<MatchMenuItem> getMatchMenuItems(Iterable<GameData> gameDataList) {
    List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    for (GameData gameData : gameDataList) {
      matchMenuItems.add(new GameMatchMenuItem(gameDatas, gameData));
    }
    return matchMenuItems;
  }

  protected void signOut() {
    Log.d(TAG, "signOut");
    signInClicked = false;
    googleApiClientManager.signout();
    updateFromConnectionStatus();
  }

  private GoBlobBaseFragment getCurrentFragment() {
    return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.current_fragment);
  }

  private void displayFragment(GoBlobBaseFragment fragment) {
    setWaitingScreenVisible(false);
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    // Replace whatever is the current_fragment view with this fragment,
    // and add the transaction to the back stack
    ft.replace(R.id.current_fragment, fragment);

    // Commit the transaction
    ft.commitAllowingStateLoss();
  }

  public void checkMatches() {
    startActivityForResult(TurnBasedMultiplayer.getInboxIntent(googleApiClient), RC_CHECK_MATCHES);
  }

  public void configureGame(boolean isLocal) {
    if (isLocal) {
      GameData localGame = androidGameRepository.createNewLocalGame();
      androidGameRepository.selectGame(localGame.getMatchId());
    } else {
      setWaitingScreenVisible(true);
      Log.d(TAG, "Starting getSelectOpponentsIntent");
      startActivityForResult(TurnBasedMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1, false), RC_SELECT_PLAYER);
    }
  }


  @Override
  public void gameListChanged() {
    updateMatchSpinner();
  }

  @Override
  public void gameChanged(GameData gameData) {
    if (Objects.equal(androidGameRepository.getCurrentMatchId(), gameData.getMatchId())) {
      gameSelected(gameData);
    }

    if (gameData.getGameConfiguration().getGameType() == PlayGameData.GameType.REMOTE) {
      Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      vibrator.vibrate(200);
    }
  }

  @Override
  public void gameSelected(GameData gameData) {
    Log.d(TAG, "gameSelected gameData = " + (gameData == null ? null : gameData.getMatchId()));
    if (gameData == null) {
      displayFragment(new PlayerChoiceFragment());
      return;
    }

    if (gameDatas.needsApplicationUpdate(gameData)) {
      displayFragment(UpdateApplicationFragment.newInstance());
      return;
    }

    selectMenuItem(gameData.getMatchId());
    displayFragment(getGameFragment());
  }

  protected GameFragment getGameFragment() {
    if (gameFragment == null) {
      gameFragment = GameFragment.newInstance();
    }
    return gameFragment;
  }

  private void updateMatchSpinner() {
    Log.d(TAG, "updateMatchSpinner");

    List<MatchMenuItem> newMatchMenuItems = Lists.newArrayList();
    newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getMyTurnGames()));
    newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getTheirTurnGames()));

    setMatchMenuItems(newMatchMenuItems);
  }

  /**
   * Selects the given match (or the first one) and return its index.
   */
  private int selectMenuItem(@Nullable String matchId) {
    Log.d(TAG, "selectMenuItem matchId = " + matchId);
    for (int index = 0; index < navigationSpinnerAdapter.getCount(); index++) {
      MatchMenuItem item = navigationSpinnerAdapter.getItem(index);
      if (Objects.equal(item.getMatchId(), matchId)) {
        matchSpinner.setSelection(index);
        return index;
      }
    }
    Log.d(TAG, String.format("selectMenuItem(%s) didn't find anything; selecting first", matchId));
    matchSpinner.setSelection(0);
    return 0;
  }

  public void unlockAchievement(String achievementId) {
    if (isSignedIn()) {
      Achievements.unlock(googleApiClient, achievementId);
    }
  }

  public void setWaitingScreenVisible(boolean visible) {
    waitingScreen.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public boolean isSignedIn() {
    return googleApiClient.isConnected();
  }

}
