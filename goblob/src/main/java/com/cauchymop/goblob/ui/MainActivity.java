package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
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
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchBuffer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static com.cauchymop.goblob.proto.PlayGameData.*;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.google.android.gms.games.Games.Achievements;
import static com.google.android.gms.games.Games.Players;
import static com.google.android.gms.games.Games.TurnBasedMultiplayer;
import static com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult;

public class MainActivity extends ActionBarActivity
    implements OnTurnBasedMatchUpdateReceivedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final int RC_REQUEST_ACHIEVEMENTS = 1;
  private static final int RC_SELECT_PLAYER = 2;
  private static final int RC_CHECK_MATCHES = 3;
  private static final int RC_SIGN_IN = 4;

  private static final String TAG = MainActivity.class.getName();
  private static final String CURRENT_MATCH = "CURRENT_MATCH";

  private int boardSize = 9;
  private AvatarManager avatarManager;
  private TurnBasedMatch turnBasedMatch;
  private MatchesAdapter navigationSpinnerAdapter;
  private List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
  private Spinner spinner;
  private boolean resolvingError;
  private GoogleApiClient googleApiClient;
  private boolean signInClicked;
  private boolean autoStartSignInFlow = true;

  @Inject
  LocalGameRepository localGameRepository;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((GoApplication) getApplication()).inject(this);

    googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
        .addApi(Games.API).addScope(Games.SCOPE_GAMES)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
    setContentView(R.layout.activity_main);

    if (savedInstanceState != null) {
      turnBasedMatch = savedInstanceState.getParcelable(CURRENT_MATCH);
    }

    setUpToolbar();

    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
      displayFragment(new PlayerChoiceFragment());
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (isSignedIn()) {
      googleApiClient.disconnect();
    }
  }

  private void setUpToolbar() {
    // Set up the action bar to show a dropdown list.
    Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
    setSupportActionBar(toolbar);

    ActionBar supportActionBar = getSupportActionBar();
    supportActionBar.setDisplayShowTitleEnabled(false);
    navigationSpinnerAdapter = new MatchesAdapter(supportActionBar.getThemedContext(), matchMenuItems);

    spinner = (Spinner) toolbar.findViewById(R.id.actionbar_spinner);
    spinner.setAdapter(navigationSpinnerAdapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
        MatchMenuItem item = navigationSpinnerAdapter.getItem(position);
        Log.e(TAG, "onItemSelected: " + item.getMatchId());
        handleMatchMenuItemSelection(item);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
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
          return;
        }
        break;
      case RC_CHECK_MATCHES:
        if (responseCode == Activity.RESULT_OK) {
          handleMatchSelected(intent);
          return;
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
    updateMatchSpinner(null);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if (turnBasedMatch != null) {
      outState.putParcelable(CURRENT_MATCH, turnBasedMatch);
    }
    super.onSaveInstanceState(outState);
  }

  private void handleMatchSelected(Intent intent) {
    Log.d(TAG, "handleMatchSelected.");
    final TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
    updateMatchSpinner(match.getMatchId());
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.d(TAG, "onConnected");
    TurnBasedMultiplayer.registerMatchUpdateListener(googleApiClient, this);

    // Retrieve the TurnBasedMatch from the connectionHint
    if (bundle != null) {
      turnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
    }

    updateMatchSpinner(getInitialMatchId());
  }

  private String getInitialMatchId() {
    GoGameController localGame = localGameRepository.getLocalGame();
    return (turnBasedMatch != null) ? turnBasedMatch.getMatchId() : localGame == null ? null : LocalMatchMenuItem.LOCAL_MATCH_ID;
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.d(TAG, "onConnectionSuspended");
    updateFromConnectionStatus();
    googleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
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
    Log.d(TAG, "updateFromConnectionStatus");
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
  private void updateMatchSpinner(@Nullable final String matchId) {
    Log.d(TAG, "updateMatchSpinner: matchId = " + matchId);
    final String previousMatchId = getCurrentMatchId();

    if (!isSignedIn()) {
      setMatchMenuItems(ImmutableList.<MatchMenuItem>of(), matchId, previousMatchId);
      return;
    }

    PendingResult<TurnBasedMultiplayer.LoadMatchesResult> matchListResult =
        TurnBasedMultiplayer.loadMatchesByStatus(googleApiClient,
            Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION,
            new int[]{TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN});
    ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> matchListResultCallBack =
        new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
          @Override
          public void onResult(TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
            LoadMatchesResponse matches = loadMatchesResult.getMatches();
            List<MatchMenuItem> newMatchMenuItems = Lists.newArrayList();
            newMatchMenuItems.addAll(getMatchMenuItems(matches.getMyTurnMatches()));
            newMatchMenuItems.addAll(getMatchMenuItems(matches.getTheirTurnMatches()));

            setMatchMenuItems(newMatchMenuItems, matchId, previousMatchId);
            updateFromConnectionStatus();
          }
        };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems, String matchId,
      String previousMatchId) {
    matchMenuItems.clear();
    matchMenuItems.addAll(newMatchMenuItems);
    GoGameController localGame = localGameRepository.getLocalGame();
    if (localGame != null) {
      matchMenuItems.add(new LocalMatchMenuItem(localGame));
    }
    matchMenuItems.add(new CreateNewGameMenuItem(getString(R.string.new_game_label)));
    navigationSpinnerAdapter.notifyDataSetChanged();

    int selectedIndex = selectMenuItem(matchId == null ? previousMatchId : matchId);
    if (matchId == null || selectedIndex == 0) {
      handleMatchMenuItemSelection(getCurrentMatchMenuItem());
    }
  }

  @Nullable
  private String getCurrentMatchId() {
    MatchMenuItem item = getCurrentMatchMenuItem();
    return getMatchId(item);
  }

  @Nullable
  private String getMatchId(@Nullable MatchMenuItem item) {
    return item == null ? null : item.getMatchId();
  }

  @Nullable
  private MatchMenuItem getCurrentMatchMenuItem() {
    return (MatchMenuItem) spinner.getSelectedItem();
  }

  private void updateMatchSpinner() {
    updateMatchSpinner(null);
  }

  private List<MatchMenuItem> getMatchMenuItems(TurnBasedMatchBuffer matchBuffer) {
    List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    for (int i = 0; i < matchBuffer.getCount(); i++) {
      TurnBasedMatch match = matchBuffer.get(i);
      MatchMenuItem matchMenuItem = new RemoteMatchMenuItem(new MatchDescription(match));
      matchMenuItems.add(matchMenuItem);
      updateAvatars(match);
    }
    matchBuffer.close();
    return matchMenuItems;
  }

  private void updateAvatars(TurnBasedMatch match) {
    for (Participant participant : match.getParticipants()) {
      Player player = participant.getPlayer();
      getAvatarManager().setAvatarUri(player.getDisplayName(), player.getIconImageUri());
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
      GoPlayer player = GameDatas.createLocalGamePlayer(GameDatas.OPPONENT_PARTICIPANT_ID, getString(R.string.opponent_default_name));
      displayGameConfigurationScreen(player, boardSize);
    } else {
      setWaitingScreenVisible(true);
      startActivityForResult(TurnBasedMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1, false), RC_SELECT_PLAYER);
    }
  }

  public void displayGameConfigurationScreen(GoPlayer opponentPlayer, int boardSize) {
    GameConfigurationFragment gameConfigurationFragment = GameConfigurationFragment.newInstance(opponentPlayer, boardSize);
    displayFragment(gameConfigurationFragment);
  }

  private void loadGame(String matchId) {
    TurnBasedMultiplayer.loadMatch(googleApiClient, matchId)
        .setResultCallback(new ResultCallback<LoadMatchResult>() {
          @Override
          public void onResult(LoadMatchResult loadMatchResult) {
            loadGame(loadMatchResult.getMatch());
          }
        });
  }

  public void loadGame(TurnBasedMatch turnBasedMatch) {
    this.turnBasedMatch = turnBasedMatch;
    loadGame(createGoGameController());
  }

  public void loadGame(GoGameController goGameController) {
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    displayFragment(GameFragment.newInstance(goGameController));
  }

  public void startLocalGame(GoGameController goGameController) {
    if (!goGameController.isLocalGame()) {
      throw new RuntimeException("startLocalGame() with non local game");
    }
    localGameRepository.saveLocalGame(goGameController);
    updateMatchSpinner(LocalMatchMenuItem.LOCAL_MATCH_ID);
    loadGame(goGameController);
  }

  /**
   * Selects the given match (or the first one) and return its index.
   */
  private int selectMenuItem(@Nullable String matchId) {
    Log.d(TAG, "selectMenuItem matchId = " + matchId);
    for (int index = 0; index < navigationSpinnerAdapter.getCount(); index++) {
      MatchMenuItem item = navigationSpinnerAdapter.getItem(index);
      if (Objects.equal(item.getMatchId(), matchId)) {
        spinner.setSelection(index);
        return index;
      }
    }
    Log.d(TAG, String.format("selectMenuItem(%s) didn't find anything; selecting first", matchId));
    spinner.setSelection(0);
    return 0;
  }

  public void giveTurn(GoGameController goGameController) {
    Log.d(TAG, "giveTurn: " + goGameController);
    takeTurn(goGameController, getOpponentIdFromCurrentMatch());
    updateMatchSpinner();
  }

  public void keepTurn(GoGameController goGameController) {
    Log.d(TAG, "keepTurn: " + goGameController);
    String myId = getMyIdFromCurrentMatch();
    takeTurn(goGameController, myId);
  }

  public void finishTurn(GoGameController goGameController) {
    Log.d(TAG, "finishTurn: " + goGameController);
    String myId = getMyIdFromCurrentMatch();
    takeTurn(goGameController, myId);
    TurnBasedMultiplayer.finishMatch(googleApiClient, turnBasedMatch.getMatchId());
    updateMatchSpinner();
  }

  private void takeTurn(GoGameController goGameController, String myId) {
    byte[] gameDataBytes = goGameController.getGameData().toByteArray();
    TurnBasedMultiplayer.takeTurn(googleApiClient, turnBasedMatch.getMatchId(), gameDataBytes, myId);
  }

  public AvatarManager getAvatarManager() {
    if (avatarManager == null) {
      avatarManager = new AvatarManager(this);
    }
    return avatarManager;
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
          public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
            Log.d(TAG, "InitiateMatchResult " + initiateMatchResult);
            if (!initiateMatchResult.getStatus().isSuccess()) {
              return;
            }
            turnBasedMatch = initiateMatchResult.getMatch();

            Log.d(TAG, "Game created...");
            updateMatchSpinner(turnBasedMatch.getMatchId());
          }
        });
  }

  private GoGameController createGoGameController() {
    boolean myTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    if (myTurn && turnBasedMatch.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      TurnBasedMultiplayer.finishMatch(googleApiClient, turnBasedMatch.getMatchId());
    }

    GameData gameData = getGameDataFromCurrentGame();

    if (gameData.getMoveCount() == 0) {
      for (String participantId : turnBasedMatch.getParticipantIds()) {
        Log.i(TAG, String.format(" participant %s: player %s", participantId,
            getPlayerId(turnBasedMatch, participantId)));
      }
    }

    GoGameController goGameController = new GoGameController(gameData, getLocalGoogleId());

    if (gameData.getMoveCount() == 0) {
      takeTurn(goGameController, getMyIdFromCurrentMatch());
    }
    return goGameController;
  }

  private String getPlayerId(TurnBasedMatch turnBasedMatch, String participantId) {
    Player player = turnBasedMatch.getParticipant(participantId).getPlayer();
    return player == null ? null : player.getPlayerId();
  }

  private GameData getGameDataFromCurrentGame() {
    return getGameData(turnBasedMatch);
  }

  private GameData getGameData(TurnBasedMatch turnBasedMatch) {
    try {
      if (turnBasedMatch.getData() == null) {
        String myId = getMyId(turnBasedMatch);
        String opponentId = getOpponentId(turnBasedMatch);
        GoPlayer blackPlayer = createGoPlayer(turnBasedMatch, myId);
        GoPlayer whitePlayer = createGoPlayer(turnBasedMatch, opponentId);
        return GameDatas.createGameData(turnBasedMatch.getVariant(), GameDatas.DEFAULT_HANDICAP,
            GameDatas.DEFAULT_KOMI, GameType.REMOTE, blackPlayer, whitePlayer);
      } else {
        GameData gameData = GameData.parseFrom(turnBasedMatch.getData());
        if (!gameData.getGameConfiguration().hasBlack() || !gameData.getGameConfiguration().hasWhite()) {
          String myId = getMyId(turnBasedMatch);
          String opponentId = getOpponentId(turnBasedMatch);
          Map<String, GoPlayer> goPlayers = ImmutableMap.of(
              myId, createGoPlayer(turnBasedMatch, myId),
              opponentId, createGoPlayer(turnBasedMatch, opponentId));
          GameConfiguration gameConfiguration = gameData.getGameConfiguration();

          GoPlayer blackPlayer = goPlayers.get(gameConfiguration.getBlackId());
          GoPlayer whitePlayer = goPlayers.get(gameConfiguration.getWhiteId());

          GameData.Builder gameBuilder = gameData.toBuilder();
          gameBuilder.getGameConfigurationBuilder().setBlack(blackPlayer).setWhite(whitePlayer);
          gameData = gameBuilder.build();
        }
        return gameData;
      }
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
  }

  private String getOpponentIdFromCurrentMatch() {
    return getOpponentId(turnBasedMatch);
  }

  private String getOpponentId(TurnBasedMatch turnBasedMatch) {
    String myId = getMyId(turnBasedMatch);
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(myId)) {
        return participantId;
      }
    }
    return null;
  }

  private String getMyIdFromCurrentMatch() {
    return getMyId(turnBasedMatch);
  }

  private String getMyId(TurnBasedMatch turnBasedMatch) {
    return turnBasedMatch.getParticipantId(getLocalGoogleId());
  }

  public String getLocalGoogleId() {
    return Players.getCurrentPlayerId(googleApiClient);
  }

  private GoPlayer createGoPlayer(TurnBasedMatch match, String participantId) {
    GoPlayer goPlayer;
    Player player = match.getParticipant(participantId).getPlayer();
    goPlayer = GameDatas.createRemoteGamePlayer(participantId, player.getPlayerId(), player.getDisplayName());
    getAvatarManager().setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    return goPlayer;
  }

  @Override
  public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchReceived");
    updateMatchSpinner();
    // While the spinner is updating, we can reload the game, that is already displayed.
    if (sameMatchId(turnBasedMatch, this.turnBasedMatch)) {
      loadGame(turnBasedMatch);
    }
  }

  private boolean sameMatchId(TurnBasedMatch match1, TurnBasedMatch match2) {
    return match2 != null && match2.getMatchId().equals(match1.getMatchId());
  }

  @Override
  public void onTurnBasedMatchRemoved(String s) {
    Log.d(TAG, "onTurnBasedMatchRemoved: " + s);
    updateMatchSpinner();
  }

  public void unlockAchievement(String achievementId) {
    Achievements.unlock(googleApiClient, achievementId);
  }

  public Player getLocalPlayer() {
    return Players.getCurrentPlayer(googleApiClient);
  }


  private void handleMatchMenuItemSelection(MatchMenuItem item) {
    Log.d(TAG, "handleMatchMenuItemSelection: " + item.getMatchId());
    GameStarter gameStarter = new GameStarter() {
      @Override
      public void startNewGame() {
        turnBasedMatch = null;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        displayFragment(new PlayerChoiceFragment());
      }

      @Override
      public void startRemoteGame(String matchId) {
        loadGame(matchId);
      }

      @Override
      public void startLocalGame(GoGameController gameController) {
        loadGame(gameController);
      }

      @Override
      public void showUpdateScreen() {
        displayFragment(UpdateApplicationFragment.newInstance());
      }
    };
    item.start(gameStarter);
  }

  public void setWaitingScreenVisible(boolean visible) {
    findViewById(R.id.waiting_view).setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public boolean isSignedIn() {
    return googleApiClient.isConnected();
  }

  public class MatchDescription {
    private final long creationTimestamp;
    private final long lastUpdateTimestamp;
    private final int turnStatus;
    private final String matchId;
    private final GameData gameData;

    public MatchDescription(TurnBasedMatch match) {
      this.creationTimestamp = match.getCreationTimestamp();
      this.lastUpdateTimestamp = match.getLastUpdatedTimestamp();
      this.turnStatus = match.getTurnStatus();
      this.matchId = match.getMatchId();
      this.gameData = MainActivity.this.getGameData(match);
    }

    public long getCreationTimestamp() {
      return creationTimestamp;
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
