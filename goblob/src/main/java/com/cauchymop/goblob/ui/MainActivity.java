package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoogleApiClientListener;
import com.cauchymop.goblob.model.GoogleApiClientManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchBuffer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import dagger.Lazy;

import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import static com.google.android.gms.games.Games.Achievements;
import static com.google.android.gms.games.Games.TurnBasedMultiplayer;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClientListener, GameRepository.GameRepositoryListener {

  private static final int RC_REQUEST_ACHIEVEMENTS = 1;
  private static final int RC_SELECT_PLAYER = 2;
  private static final int RC_CHECK_MATCHES = 3;
  private static final int RC_SIGN_IN = 4;

  private static final String TAG = MainActivity.class.getName();
  private static final String CURRENT_MATCH_ID = "CURRENT_MATCH_ID";

  private int boardSize = 9;

  @Bind(R.id.toolbar_match_spinner) Spinner matchSpinner;
  @Bind(R.id.app_toolbar) Toolbar toolbar;
  @Bind(R.id.waiting_view) View waitingScreen;

  private MatchesAdapter navigationSpinnerAdapter;
  private List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
  private boolean resolvingError;
  private boolean signInClicked;
  private boolean autoStartSignInFlow = true;


  @Inject GoogleApiClient googleApiClient;
  @Inject GameDatas gameDatas;
  @Inject GameRepository gameRepository;
  @Inject AvatarManager avatarManager;
  @Inject GoogleApiClientManager googleApiClientManager;
  @Inject @Named("LocalGoogleIdentity") Lazy<String> localGoogleIdentity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    ((GoApplication)getApplication()).getComponent().inject(this);

    gameRepository.addGameRepositoryListener(this);

    if (savedInstanceState != null) {
      gameRepository.selectGame(savedInstanceState.getString(CURRENT_MATCH_ID));
    }

    setUpToolbar();

    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
      displayFragment(new PlayerChoiceFragment());
    }

    googleApiClientManager.registerGoogleApiClientListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart");
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
    gameRepository.removeGameRepositoryListener(this);
    ButterKnife.unbind(this);
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
    handleMatchMenuItemSelection(item);
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
        if (responseCode == Activity.RESULT_OK) {
          handleSelectPlayersResult(intent);
        }
        break;
      case RC_CHECK_MATCHES:
        Log.d(TAG, "onActivityResult RC_CHECK_MATCHES");
        updateMatchSpinner();
        if (responseCode == Activity.RESULT_OK) {
          final TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
          gameRepository.selectGame(match.getMatchId());
        }
        break;
      case RC_REQUEST_ACHIEVEMENTS:
        break;
      case RC_SIGN_IN:
        signInClicked = false;
        resolvingError = false;
        if (responseCode == RESULT_OK) {
          googleApiClient.connect();
        } else {
          BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
        }
        break;
      default:
        Log.e(TAG, "onActivityResult unexpected requestCode " + requestCode);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString(CURRENT_MATCH_ID, gameRepository.getCurrentMatchId());
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.d(TAG, "onConnected");
    TurnBasedMultiplayer.registerMatchUpdateListener(googleApiClient, gameRepository);

    // Retrieve the TurnBasedMatch from the connectionHint
    if (bundle != null) {
      TurnBasedMatch turnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
      gameRepository.selectGame(turnBasedMatch.getMatchId());
    }

    // selectedMatchId values cases:
    //  1) Match retrieved from connection hint (user selected a game from Notification), value set in if above
    //  2) selectedMatchId was set either last time we selected a game or reset on view recreation through onSavedInstanceState
    //  3) selectedMatchId was null as no game was ever selected

    updateMatchSpinner();
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

    setWaitingScreenVisible(false);
  }

  /**
   * Update asynchronously the spinner with all the current games, and run the given callback.
   */
  private void updateMatchSpinner() {
    final long requestId = System.currentTimeMillis();
    Log.d(TAG, String.format("updateMatchSpinner: requestId = %d", requestId));
    if (!isSignedIn()) {
      setMatchMenuItems(ImmutableList.<MatchMenuItem>of());
      return;
    }

    PendingResult<TurnBasedMultiplayer.LoadMatchesResult> matchListResult =
        TurnBasedMultiplayer.loadMatchesByStatus(googleApiClient,
            Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION,
            new int[]{TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN});
    ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> matchListResultCallBack =
        new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
          @Override
          public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
            Log.d(TAG, String.format("matchResult: requestId = %d, latency = %d ms", requestId, System.currentTimeMillis() - requestId));
            LoadMatchesResponse matches = loadMatchesResult.getMatches();
            List<MatchMenuItem> newMatchMenuItems = Lists.newArrayList();
            newMatchMenuItems.addAll(getMatchMenuItems(matches.getMyTurnMatches()));
            newMatchMenuItems.addAll(getMatchMenuItems(matches.getTheirTurnMatches()));

            setMatchMenuItems(newMatchMenuItems);
            updateFromConnectionStatus();
          }
        };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems) {
    matchMenuItems.clear();
    matchMenuItems.addAll(newMatchMenuItems);
    GameData localGameData = gameRepository.getLocalGame();
    if (localGameData != null) {
      matchMenuItems.add(new LocalMatchMenuItem(localGameData));
    }
    matchMenuItems.add(new CreateNewGameMenuItem(getString(R.string.new_game_label)));
    navigationSpinnerAdapter.notifyDataSetChanged();

    String currentMatchId = gameRepository.getCurrentMatchId();
    int selectedIndex = selectMenuItem(currentMatchId);
    if (currentMatchId == null || selectedIndex == 0) {
      handleMatchMenuItemSelection(getCurrentMatchMenuItem());
    }
  }

  @Nullable
  private MatchMenuItem getCurrentMatchMenuItem() {
    Log.d(TAG, "before getSelectedItem(), matchSpinner = " + matchSpinner);
    Log.d(TAG, "MainActivity = " + this);
    return (MatchMenuItem) matchSpinner.getSelectedItem();
  }

  private List<MatchMenuItem> getMatchMenuItems(TurnBasedMatchBuffer matchBuffer) {
    List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    for (int i = 0; i < matchBuffer.getCount(); i++) {
      TurnBasedMatch match = matchBuffer.get(i);
      MatchMenuItem matchMenuItem = new RemoteMatchMenuItem(new MatchDescription(match));
      matchMenuItems.add(matchMenuItem);
      updateAvatars(match);
    }
    return matchMenuItems;
  }

  private void updateAvatars(TurnBasedMatch match) {
    for (Participant participant : match.getParticipants()) {
      Player player = participant.getPlayer();
      avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    }
  }

  protected void signOut() {
    Log.d(TAG, "signOut");
    signInClicked = false;
    Games.signOut(googleApiClient);
    googleApiClient.disconnect();
    updateMatchSpinner();
    updateFromConnectionStatus();
  }

  private GoBlobBaseFragment getCurrentFragment() {
    return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.current_fragment);
  }

  private void displayFragment(GoBlobBaseFragment fragment) {
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

  public void configureGame(boolean isLocal, int boardSize) {
    this.boardSize = boardSize;
    if (isLocal) {
      GameData localGame = gameDatas.createLocalGame(boardSize);
      gameRepository.saveGame(localGame);
      gameRepository.selectGame(localGame.getMatchId());
      updateMatchSpinner();
    } else {
      setWaitingScreenVisible(true);
      Log.d(TAG, "Starting getSelectOpponentsIntent");
      startActivityForResult(TurnBasedMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1, false), RC_SELECT_PLAYER);
    }
  }

  public void displayGameConfigurationScreen(GameData gameData) {
    GameConfigurationFragment gameConfigurationFragment = GameConfigurationFragment.newInstance(gameData);
    displayFragment(gameConfigurationFragment);
  }


  @Override
  public void gameListChanged() {
    updateMatchSpinner();
  }

  @Override
  public void gameSelected(GameData gameData) {
    Log.d(TAG, "gameSelected gameData = " + gameData);
    if (gameData == null) {
      displayFragment(new PlayerChoiceFragment());
      return;
    }
    switch (gameData.getPhase()) {
      case INITIAL:
      case CONFIGURATION:
        displayGameConfigurationScreen(gameData);
        return;
      case IN_GAME:
      case DEAD_STONE_MARKING:
      case FINISHED:
        displayFragment(GameFragment.newInstance(gameData));
        return;
      default:
        throw new RuntimeException("Invalid phase for game: " + gameData);
    }
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

  private void handleSelectPlayersResult(Intent intent) {
    Log.d(TAG, "Select players UI succeeded.");

    // get the invitee list
    final ArrayList<String> invitees = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
    Log.d(TAG, "Invitees: " + invitees);

    // get the automatch criteria
    Bundle autoMatchCriteria = null;
    int minAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
    int maxAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
    if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
      autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
          minAutoMatchPlayers, maxAutoMatchPlayers, 0);
      Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
    }

    // create game
    TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig.builder()
        .addInvitedPlayers(invitees)
        .setVariant(boardSize)
        .setAutoMatchCriteria(autoMatchCriteria).build();

    // kick the match off
    TurnBasedMultiplayer.createMatch(googleApiClient, turnBasedMatchConfig)
        .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
          @Override
          public void onResult(@NonNull TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
            Log.d(TAG, "InitiateMatchResult " + initiateMatchResult);
            if (!initiateMatchResult.getStatus().isSuccess()) {
              return;
            }
            TurnBasedMatch turnBasedMatch = initiateMatchResult.getMatch();
            Log.d(TAG, "Game created...");
            gameRepository.selectGame(turnBasedMatch.getMatchId());
            updateMatchSpinner();
          }
        });
  }

  public void unlockAchievement(String achievementId) {
    Achievements.unlock(googleApiClient, achievementId);
  }

  public void setWaitingScreenVisible(boolean visible) {
    waitingScreen.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public boolean isSignedIn() {
    return googleApiClient.isConnected();
  }

  private void handleMatchMenuItemSelection(MatchMenuItem item) {
    Log.d(TAG, "handleMatchMenuItemSelection: " + item.getMatchId());
    GameStarter gameStarter = new GameStarter() {
      @Override
      public void startNewGame() {
        gameRepository.selectGame(GameDatas.NEW_GAME_MATCH_ID);
        displayFragment(new PlayerChoiceFragment());
      }

      @Override
      public void selectGame(String matchId) {
        gameRepository.selectGame(matchId);
      }

      @Override
      public void showUpdateScreen() {
        displayFragment(UpdateApplicationFragment.newInstance());
      }
    };
    item.start(gameStarter);
  }

  public void endTurn(GameData gameData) {
    gameRepository.saveGame(gameData);
    updateMatchSpinner();
    gameSelected(gameData);
  }

  public class MatchDescription {
    private final long lastUpdateTimestamp;
    private final int turnStatus;
    private final String matchId;
    private final GameData gameData;

    public MatchDescription(TurnBasedMatch match) {
      this.lastUpdateTimestamp = match.getLastUpdatedTimestamp();
      this.turnStatus = match.getTurnStatus();
      this.matchId = match.getMatchId();
      this.gameData = gameRepository.getGameData(match);
    }

    public long getLastUpdateTimestamp() {
      return lastUpdateTimestamp;
    }

    public int getTurnStatus() {
      return turnStatus;
    }

    public String getMatchId() {
      return matchId;
    }

    public GameData getGameData() {
      return gameData;
    }

    public GoPlayer getBlackPlayer() {
      return gameData.getGameConfiguration().getBlack();
    }

    public GoPlayer getWhitePlayer() {
      return gameData.getGameConfiguration().getWhite();
    }
  }
}
