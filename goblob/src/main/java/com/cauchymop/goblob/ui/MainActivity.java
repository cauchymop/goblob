package com.cauchymop.goblob.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.StoneColor;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchBuffer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.common.collect.ImmutableMap;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Map;

import static com.cauchymop.goblob.model.GoPlayer.PlayerType;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.google.android.gms.games.Games.Achievements;
import static com.google.android.gms.games.Games.Players;
import static com.google.android.gms.games.Games.TurnBasedMultiplayer;
import static com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult;

public class MainActivity extends BaseGameActivity
    implements OnTurnBasedMatchUpdateReceivedListener, ActionBar.OnNavigationListener {

  public static final int REQUEST_ACHIEVEMENTS = 1;
  public static final int SELECT_PLAYER = 2;
  public static final int CHECK_MATCHES = 3;

  private static final String TAG = MainActivity.class.getName();
  private static final String CURRENT_MATCH = "CURRENT_MATCH";

  private int boardSize = 9;
  private AvatarManager avatarManager;
  private TurnBasedMatch turnBasedMatch;
  private GameFragment gameFragment;
  private MatchesAdapter navigationSpinnerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState != null) {
      turnBasedMatch = savedInstanceState.getParcelable(CURRENT_MATCH);
    }

    // Set up the action bar to show a dropdown list.
    final ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

    // Specify a SpinnerAdapter to populate the dropdown list.
    // use getActionBar().getThemedContext() to ensure
    // that the text color is always appropriate for the action bar
    // background rather than the activity background.
    navigationSpinnerAdapter = new MatchesAdapter(actionBar.getThemedContext());

    // Set up the dropdown list navigation in the action bar.
    actionBar.setListNavigationCallbacks(navigationSpinnerAdapter, this);

    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
      displayFragment(new PlayerChoiceFragment(), false);
    }
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
      startActivityForResult(Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
      return true;
    } else if (id == R.id.menu_signout) {
      signOut();
    } else if (id == R.id.menu_signin) {
      beginUserInitiatedSignIn();
    } else if (id == R.id.menu_check_matches) {
      checkMatches();
    }
    return false;
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    super.onActivityResult(requestCode, responseCode, intent);
    if (responseCode != Activity.RESULT_OK) {
      Log.w(TAG, "Select players UI cancelled, " + responseCode);
      return;
    }
    switch (requestCode) {
      case SELECT_PLAYER:
        handleSelectPlayersResult(intent);
        break;
      case CHECK_MATCHES:
        handleMatchSelected(intent);
        break;
    }
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
    TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
    startGame(match);
    loadMatches();
  }

  @Override
  public void onSignInFailed() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignInFailed();
  }

  @Override
  public void onSignInSucceeded() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignInSucceeded();
    loadMatches();
    if (mHelper.getTurnBasedMatch() != null) {
      Log.d(TAG, "Found match");

      // prevent screen from sleeping during handshake
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      startGame(mHelper.getTurnBasedMatch());
    }
    TurnBasedMultiplayer.registerMatchUpdateListener(getApiClient(), this);
  }

  private void loadMatches() {
    PendingResult<TurnBasedMultiplayer.LoadMatchesResult> matchListResult = TurnBasedMultiplayer.loadMatchesByStatus(getApiClient(),
        Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION,
        new int[]{TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN}
    );
    ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> matchListResultCallBack = new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
      @Override
      public void onResult(TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {

        int currentGameIndex = 0;

        navigationSpinnerAdapter.clear();

        // Add Matches
        LoadMatchesResponse matches = loadMatchesResult.getMatches();
        currentGameIndex = addMatchesToMatchesAdapter(matches.getMyTurnMatches(), currentGameIndex);
        currentGameIndex = addMatchesToMatchesAdapter(matches.getTheirTurnMatches(), currentGameIndex);

        // Add Create New Game entry
        MatchMenuItem matchMenuItem = new CreateNewGameMenuItem(getString(R.string.new_game_label));
        navigationSpinnerAdapter.add(matchMenuItem);
        navigationSpinnerAdapter.notifyDataSetChanged();

        // Select current game index
        ActionBar actionBar = getActionBar();
        actionBar.setSelectedNavigationItem(currentGameIndex);
      }
    };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private int addMatchesToMatchesAdapter(TurnBasedMatchBuffer matchBuffer, int currentGameIndex) {
    for (int i = 0; i < matchBuffer.getCount(); i++) {
      TurnBasedMatch match = matchBuffer.get(i);
      if (turnBasedMatch != null && match.getMatchId().equals(turnBasedMatch.getMatchId())) {
        currentGameIndex = navigationSpinnerAdapter.getCount();
      }
      MatchMenuItem matchMenuItem = new RemoteMatchMenuItem(match.getCreationTimestamp(), match.getVariant(), match.getTurnStatus(), match.getMatchId());
      navigationSpinnerAdapter.add(matchMenuItem);
    }
    matchBuffer.close();
    return currentGameIndex;
  }

  @Override
  protected void signOut() {
    super.signOut();
    onSignOut();
  }

  @Override
  protected boolean isSignedIn() {
    return super.isSignedIn();
  }

  private GoBlobBaseFragment getCurrentFragment() {
    return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
  }

  private void displayFragment(GoBlobBaseFragment fragment, boolean addToBackStack) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack
    ft.replace(R.id.fragment_container, fragment);

    // Add the transaction to the back stack if needed.
    if (addToBackStack) {
      ft.addToBackStack(null);
    }

    // Commit the transaction
    ft.commit();
  }

  @Override
  protected void beginUserInitiatedSignIn() {
    super.beginUserInitiatedSignIn();
  }

  public void onSignOut() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignOut();
  }

  public void checkMatches() {
    startActivityForResult(TurnBasedMultiplayer.getInboxIntent(getApiClient()), CHECK_MATCHES);
  }

  public void configureGame(GoPlayer opponentPlayer, int boardSize) {
    this.boardSize = boardSize;
    if (opponentPlayer.getType() == PlayerType.REMOTE) {
      startActivityForResult(TurnBasedMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 1), SELECT_PLAYER);
    } else {
      displayGameConfigurationScreen(opponentPlayer, boardSize);
    }
  }

  public void displayGameConfigurationScreen(GoPlayer opponentPlayer, int boardSize) {
    GameConfigurationFragment gameConfigurationFragment = GameConfigurationFragment.newInstance(opponentPlayer, boardSize);
    displayFragment(gameConfigurationFragment, false);
  }

  private void startGame(String matchId) {
    TurnBasedMultiplayer.loadMatch(getApiClient(), matchId)
        .setResultCallback(new ResultCallback<LoadMatchResult>() {
          @Override
          public void onResult(LoadMatchResult loadMatchResult) {
            startGame(loadMatchResult.getMatch());
          }
        });
  }

  public void startGame(TurnBasedMatch turnBasedMatch) {
    this.turnBasedMatch = turnBasedMatch;
    startGame(createGoGameController(turnBasedMatch));
  }

  public void startGame(GoGameController goGameController) {
    if (gameFragment == null || !gameFragment.isVisible()) {  // TODO: || isDifferentGame()
      gameFragment = GameFragment.newInstance(goGameController);
      displayFragment(gameFragment, false);
    } else {
      gameFragment.setGameController(goGameController);
    }
  }

  public void giveTurn(GoGameController goGameController) {
    String myId = getMyId(turnBasedMatch);
    if (goGameController.getGame().isGameEnd()) {
      takeTurn(goGameController, myId);
      TurnBasedMultiplayer.finishMatch(getApiClient(), turnBasedMatch.getMatchId());
    } else {
      takeTurn(goGameController, getOpponentId(turnBasedMatch, myId));
    }
  }

  private void takeTurn(GoGameController goGameController, String myId) {
    byte[] gameDataBytes = goGameController.getGameData().toByteArray();
    Log.d(TAG, "takeTurn: " + goGameController);
    TurnBasedMultiplayer.takeTurn(getApiClient(), turnBasedMatch.getMatchId(), gameDataBytes, myId);
    // Reloads the list of matches to reflect new state
    // TODO: Handle the list better
    loadMatches();
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
    TurnBasedMultiplayer.createMatch(getApiClient(), turnBasedMatchConfig)
        .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
          @Override
          public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
            Log.d(TAG, "InitiateMatchResult " + initiateMatchResult);
            if (!initiateMatchResult.getStatus().isSuccess()) {
              return;
            }
            turnBasedMatch = initiateMatchResult.getMatch();

            GoGameController goGameController = createGoGameController(turnBasedMatch);
            if (turnBasedMatch.getData() == null) {
              Log.d(TAG, "getData is null, saving a new game");
              takeTurn(goGameController, getMyId(turnBasedMatch));
            }

            Log.d(TAG, "Game created, starting game activity...");
            startGame(goGameController);
            loadMatches();
          }
        });
  }

  private GoGameController createGoGameController(TurnBasedMatch turnBasedMatch) {
    boolean myTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    if (myTurn && turnBasedMatch.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      TurnBasedMultiplayer.finishMatch(getApiClient(), turnBasedMatch.getMatchId());
    }

    String myId = getMyId(turnBasedMatch);
    String opponentId = getOpponentId(turnBasedMatch, myId);

    for (String participantId : turnBasedMatch.getParticipantIds()) {
      Log.i(TAG, String.format(" participant %s: player %s", participantId,
          getPlayerId(turnBasedMatch, participantId)));
    }

    GameData gameData = getGameData(turnBasedMatch, myId, opponentId);

    Map<String, GoPlayer> goPlayers = ImmutableMap.of(
        myId, createGoPlayer(turnBasedMatch, myId, PlayerType.LOCAL),
        opponentId, createGoPlayer(turnBasedMatch, opponentId, PlayerType.REMOTE));

    GoGameController goGameController = new GoGameController(gameData);
    GameConfiguration gameConfiguration = goGameController.getGameConfiguration();
    goGameController.setGoPlayer(StoneColor.Black, goPlayers.get(gameConfiguration.getBlackId()));
    goGameController.setGoPlayer(StoneColor.White, goPlayers.get(gameConfiguration.getWhiteId()));

    return goGameController;
  }

  private String getPlayerId(TurnBasedMatch turnBasedMatch, String participantId) {
    Player player = turnBasedMatch.getParticipant(participantId).getPlayer();
    return player == null ? null : player.getPlayerId();
  }

  private GameData getGameData(TurnBasedMatch turnBasedMatch, String myId, String opponentId) {
    try {
      return turnBasedMatch.getData() == null
          ? GameDatas.createGameData(turnBasedMatch.getVariant(), 0, myId, opponentId)
          : GameData.parseFrom(turnBasedMatch.getData());
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
  }

  private String getOpponentId(TurnBasedMatch turnBasedMatch, String id) {
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(id)) {
        return participantId;
      }
    }
    return null;
  }

  private String getMyId(TurnBasedMatch turnBasedMatch) {
    return turnBasedMatch.getParticipantId(Players.getCurrentPlayerId(getApiClient()));
  }

  private GoPlayer createGoPlayer(TurnBasedMatch turnBasedMatch, String participantId,
      PlayerType playerType) {
    GoPlayer goPlayer;
    if (isParticipantAutoMatch(turnBasedMatch, participantId)) {
      goPlayer = new GoPlayer(playerType, participantId, getString(R.string.opponent_default_name));
    } else {
      Player player = turnBasedMatch.getParticipant(participantId).getPlayer();
      goPlayer = new GoPlayer(playerType, participantId, player.getDisplayName());
      getAvatarManager().setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    }
    return goPlayer;
  }

  private boolean isParticipantAutoMatch(TurnBasedMatch turnBasedMatch, String participantId) {
    return participantId == null || turnBasedMatch.getParticipant(participantId).getPlayer() == null;
  }

  @Override
  public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchReceived");
    startGame(turnBasedMatch);
    loadMatches();
  }

  @Override
  public void onTurnBasedMatchRemoved(String s) {
    Log.d(TAG, "onTurnBasedMatchRemoved: " + s);
    loadMatches();
  }

  public void unlockAchievement(String achievementId) {
    Achievements.unlock(getApiClient(), achievementId);
  }

  public Player getLocalPlayer() {
    return Players.getCurrentPlayer(getApiClient());
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    MatchMenuItem item = navigationSpinnerAdapter.getItem(itemPosition);
    GameStarter gameStarter = new GameStarter() {
      @Override
      public void startNewGame() {
        displayFragment(new PlayerChoiceFragment(), false);
      }

      @Override
      public void startRemoteGame(String match) {
        startGame(match);
      }

      @Override
      public void startLocalGame(GoGameController gameController) {
        startGame(gameController);
      }
    };
    item.start(gameStarter);
    return true;
  }
}
