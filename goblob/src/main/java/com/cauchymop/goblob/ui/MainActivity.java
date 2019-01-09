package com.cauchymop.goblob.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AccountStateListener;
import com.cauchymop.goblob.model.GameChangeListener;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameListListener;
import com.cauchymop.goblob.model.GameSelectionListener;
import com.cauchymop.goblob.model.GoogleAccountManager;
import com.cauchymop.goblob.proto.PlayGameData;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.TurnBasedMultiplayerClient;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

import static com.cauchymop.goblob.model.GameRepositoryKt.NO_MATCH_ID;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;

public class MainActivity extends AppCompatActivity
    implements GameListListener, GameChangeListener, GameSelectionListener, AccountStateListener {

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

  @Inject
  GameDatas gameDatas;
  @Inject
  AndroidGameRepository androidGameRepository;
  @Inject
  GoogleAccountManager googleAccountManager;
  @Inject
  Provider<TurnBasedMultiplayerClient> turnBasedClientProvider;
  @Inject
  Provider<PlayersClient> playersClientProvider;

  private Unbinder unbinder;
  private GameFragment gameFragment;
  private GoogleSignInClient signInClient;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Crashlytics.log(Log.DEBUG, TAG, "onCreate - intent = " + getIntent().getExtras());

    setContentView(R.layout.activity_main);
    unbinder = ButterKnife.bind(this);

    ((GoApplication) getApplication()).getComponent().inject(this);

    setUpToolbar();

    androidGameRepository.addGameListListener(this);
    androidGameRepository.addGameChangeListener(this);
    androidGameRepository.addGameSelectionListener(this);
    googleAccountManager.addAccountStateListener(this);

    if (savedInstanceState != null) {
      androidGameRepository.selectGame(savedInstanceState.getString(CURRENT_MATCH_ID));
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Crashlytics.log(Log.DEBUG, TAG, "onStart");
    updateMatchSpinner();
    signIn();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Crashlytics.log(Log.DEBUG, TAG, "onDestroy");
    androidGameRepository.removeGameListListener(this);
    androidGameRepository.removeGameChangeListener(this);
    androidGameRepository.removeGameSelectionListener(this);
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
    Crashlytics.log(Log.DEBUG, TAG, "onItemSelected: " + item.getMatchId());
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
    boolean signedIn = googleAccountManager.getSignInComplete();
    menu.setGroupVisible(R.id.group_signedIn, signedIn);
    menu.setGroupVisible(R.id.group_signedOut, !signedIn);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_achievements) {
      Games.getAchievementsClient(this, googleAccountManager.getSignedInAccount()).getAchievementsIntent()
          .addOnCompleteListener(intentTask -> startActivityForResult(intentTask.getResult(), RC_REQUEST_ACHIEVEMENTS));
      return true;
    } else if (id == R.id.menu_signout) {
      signOut();
    } else if (id == R.id.menu_signin) {
      Crashlytics.log(Log.DEBUG, TAG, "signIn from menu");
      signIn();
    } else if (id == R.id.menu_check_matches) {
      checkMatches();
    } else if (id == R.id.menu_about) {
      startActivity(new Intent(this, AboutActivity.class));
    }
    return false;
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    super.onActivityResult(requestCode, responseCode, intent);
    Crashlytics.log(Log.DEBUG, TAG, String.format("onActivityResult requestCode = %d, responseCode = %d", requestCode, responseCode));
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
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        if (result.isSuccess()) {
          googleAccountManager.onSignInSuccess();
        } else {
          String message = result.getStatus().getStatusMessage();
          if (message == null || message.isEmpty()) {
            message = getString(R.string.signin_other_error);
          }
          new AlertDialog.Builder(this).setMessage(message)
              .setNeutralButton(android.R.string.ok, null).show();
        }
        break;
      default:
        Crashlytics.log(Log.ERROR, TAG, "onActivityResult unexpected requestCode " + requestCode);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString(CURRENT_MATCH_ID, androidGameRepository.getCurrentMatchId());
    super.onSaveInstanceState(outState);
  }

  public void updateUiFromConnectionStatus(boolean isSignInComplete) {
    Crashlytics.log(Log.DEBUG, TAG, "updateUiFromConnectionStatus isSignedIn = " + isSignInComplete);
    invalidateOptionsMenu();

    // When initial connection fails, there is no fragment yet.
    GoBlobBaseFragment currentFragment = getCurrentFragment();
    if (currentFragment != null) {
      currentFragment.updateFromConnectionStatus(isSignInComplete);
    }
  }

  private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems) {
    matchMenuItems.clear();
    matchMenuItems.addAll(newMatchMenuItems);

    matchMenuItems.add(new CreateNewGameMenuItem(getString(R.string.new_game_label)));
    navigationSpinnerAdapter.notifyDataSetChanged();

    selectMenuItem(androidGameRepository.getCurrentMatchId());
  }

  private List<MatchMenuItem> getMatchMenuItems(Iterable<GameData> gameDataList) {
    List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    for (GameData gameData : gameDataList) {
      matchMenuItems.add(new GameMatchMenuItem(gameDatas, gameData));
    }
    return matchMenuItems;
  }

  private void signIn() {
    signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
    signInClient.silentSignIn().addOnCompleteListener(this,
        task -> {
          if (task.isSuccessful()) {
            // The signed in account is stored in the task's result.
            googleAccountManager.onSignInSuccess();
          } else {
            // Player will need to sign-in explicitly using via UI
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
          }
        });
  }

  private void signOut() {
    Crashlytics.log(Log.DEBUG, TAG, "signOut");
    signInClient.signOut().addOnCompleteListener(this,
        task -> googleAccountManager.onSignOut());
  }

  private GoBlobBaseFragment getCurrentFragment() {
    return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.current_fragment);
  }

  private void displayFragment(GoBlobBaseFragment fragment) {
    Crashlytics.log(Log.DEBUG, TAG, "displayFragment " + fragment.getClass().getSimpleName());
    setWaitingScreenVisible(false);
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    // Replace whatever is the current_fragment view with this fragment,
    // and add the transaction to the back stack
    ft.replace(R.id.current_fragment, fragment);

    // Commit the transaction
    ft.commitAllowingStateLoss();
  }

  public void checkMatches() {
    turnBasedClientProvider.get().getInboxIntent().addOnCompleteListener(task -> startActivityForResult( task.getResult(), RC_CHECK_MATCHES));

  }

  public void configureGame(boolean isLocal) {
    if (isLocal) {
      GameData localGame = androidGameRepository.createNewLocalGame();
      androidGameRepository.selectGame(localGame.getMatchId());
    } else {
      setWaitingScreenVisible(true);
      Crashlytics.log(Log.DEBUG, TAG, "Starting getSelectOpponentsIntent");
      getTurnBasedMultiplayerClient().getSelectOpponentsIntent(1, 1, false).addOnCompleteListener(
          task -> startActivityForResult(task.getResult(), RC_SELECT_PLAYER)
      );
    }
  }

  @Override
  public void gameListChanged() {
    updateMatchSpinner();
  }

  @Override
  public void gameChanged(GameData gameData) {
    if (gameData.getGameConfiguration().getGameType() == PlayGameData.GameType.REMOTE) {
      Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
      vibrator.vibrate(200);
    }
  }

  @Override
  public void gameSelected(GameData gameData) {
    Crashlytics.log(Log.DEBUG, TAG, "gameSelected gameData = " + (gameData == null ? null : gameData.getMatchId()));
    if (gameData == null) {
      selectMenuItem(NO_MATCH_ID);
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
      gameFragment = GameFragment.Companion.newInstance();
    }
    return gameFragment;
  }

  private void updateMatchSpinner() {
    Crashlytics.log(Log.DEBUG, TAG, "updateMatchSpinner");

    List<MatchMenuItem> newMatchMenuItems = Lists.newArrayList();
    newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getMyTurnGames()));
    newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getTheirTurnGames()));

    setMatchMenuItems(newMatchMenuItems);
  }

  /**
   * Selects the given match (or the first one) and return its index.
   */
  private void selectMenuItem(@NonNull String matchId) {
    Crashlytics.log(Log.DEBUG, TAG, "selectMenuItem matchId = " + matchId);
    for (int index = 0; index < navigationSpinnerAdapter.getCount(); index++) {
      MatchMenuItem item = navigationSpinnerAdapter.getItem(index);
      if (Objects.equal(item.getMatchId(), matchId)) {
        matchSpinner.setSelection(index);
        return;
      }
    }

    Crashlytics.log(Log.DEBUG, TAG, String.format("selectMenuItem(%s) didn't find anything; we do nothing (it's probably loading...)", matchId));
  }

  public void setWaitingScreenVisible(boolean visible) {
    waitingScreen.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  @Override
  public void accountStateChanged(boolean isSignInComplete) {
    if (isSignInComplete) {
      androidGameRepository.refreshRemoteGameListFromServer();
      androidGameRepository.publishUnpublishedGames();
      Games.getGamesClient(this, googleAccountManager.getSignedInAccount()).getActivationHint().addOnSuccessListener(bundle -> {
        // Retrieve the TurnBasedMatch from the connectionHint in order to select it
        if (bundle != null) {
          TurnBasedMatch turnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
          Crashlytics.log(Log.DEBUG, TAG, " ==> We have an invite! " + turnBasedMatch);
          androidGameRepository.setPendingMatchId(turnBasedMatch.getMatchId());
        }
      });
    }
    invalidateOptionsMenu();
    updateUiFromConnectionStatus(isSignInComplete);
  }

  private TurnBasedMultiplayerClient getTurnBasedMultiplayerClient() {
    return Games.getTurnBasedMultiplayerClient(this, googleAccountManager.getSignedInAccount());
  }
}
