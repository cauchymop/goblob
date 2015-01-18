package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cauchymop.goblob.model.GoPlayer.PlayerType;
import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.google.android.gms.games.Games.Achievements;
import static com.google.android.gms.games.Games.Players;
import static com.google.android.gms.games.Games.TurnBasedMultiplayer;
import static com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult;

public class MainActivity extends BaseGameActivity
    implements OnTurnBasedMatchUpdateReceivedListener {

  public static final int REQUEST_ACHIEVEMENTS = 1;
  public static final int SELECT_PLAYER = 2;
  public static final int CHECK_MATCHES = 3;

  private static final String TAG = MainActivity.class.getName();
  private static final String CURRENT_MATCH = "CURRENT_MATCH";

  private int boardSize = 9;
  private AvatarManager avatarManager;
  private TurnBasedMatch turnBasedMatch;
  private MatchesAdapter navigationSpinnerAdapter;
  private List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
  private GoGameController localGameController;
  private Spinner spinner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState != null) {
      turnBasedMatch = savedInstanceState.getParcelable(CURRENT_MATCH);
    }

    setUpToolbar();

    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
      displayFragment(new PlayerChoiceFragment());
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
    Log.d(TAG, String.format("onActivityResult requestCode = %d, responseCode = %d", requestCode, responseCode));
    switch (requestCode) {
      case SELECT_PLAYER:
        if (responseCode == Activity.RESULT_OK) {
          handleSelectPlayersResult(intent);
          return;
        }
        break;
      case CHECK_MATCHES:
        if (responseCode == Activity.RESULT_OK) {
          handleMatchSelected(intent);
          return;
        }
        break;
      case REQUEST_ACHIEVEMENTS:
        break;
      default:
        Log.e(TAG, "onActivityResult unexpected requestCode " + requestCode);
    }
    updateMatchSpinner(null, true);
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
    updateMatchSpinner(match.getMatchId(), false);
  }

  @Override
  public void onSignInFailed() {
    Log.d(TAG, "onSignInFailed");
    invalidateOptionsMenu();
    getCurrentFragment().onSignInFailed();
    setWaitingScreenVisible(false);
  }

  @Override
  public void onSignInSucceeded() {
    Log.d(TAG, "onSignInSucceeded");
    invalidateOptionsMenu();
    getCurrentFragment().onSignInSucceeded();
    TurnBasedMultiplayer.registerMatchUpdateListener(getApiClient(), this);
    TurnBasedMatch signedInMatchId = mHelper.getTurnBasedMatch();
    Log.d(TAG, "onSignInSucceeded: signedInMatchId = " + signedInMatchId);
    updateMatchSpinner(signedInMatchId == null ? null : signedInMatchId.getMatchId(), true);
  }

  /**
   * Update asynchronously the spinner with all the current games, and run the given callback.
   */
  private void updateMatchSpinner(@Nullable final String matchId, final boolean dismissWaitingScreen) {
    Log.d(TAG, "updateMatchSpinner: matchId = " + matchId);
    final String previousMatchId = getCurrentMatchId();

    if (!isSignedIn()) {
      setMatchMenuItems(ImmutableList.<MatchMenuItem>of(), matchId, previousMatchId);
      return;
    }

    PendingResult<TurnBasedMultiplayer.LoadMatchesResult> matchListResult =
        TurnBasedMultiplayer.loadMatchesByStatus(getApiClient(),
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
            if (dismissWaitingScreen) {
              setWaitingScreenVisible(false);
            }
          }
        };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems, String matchId,
      String previousMatchId) {
    matchMenuItems.clear();
    matchMenuItems.addAll(newMatchMenuItems);
    if (localGameController != null) {
      matchMenuItems.add(new LocalMatchMenuItem(localGameController));
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
    updateMatchSpinner(null, false);
  }

  private List<MatchMenuItem> getMatchMenuItems(TurnBasedMatchBuffer matchBuffer) {
    List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    for (int i = 0; i < matchBuffer.getCount(); i++) {
      TurnBasedMatch match = matchBuffer.get(i);
      MatchMenuItem matchMenuItem = new RemoteMatchMenuItem(new MatchDescription(match));
      matchMenuItems.add(matchMenuItem);
    }
    matchBuffer.close();
    return matchMenuItems;
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

  @Override
  protected void beginUserInitiatedSignIn() {
    super.beginUserInitiatedSignIn();
  }

  public void onSignOut() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignOut();
    updateMatchSpinner();
  }

  public void checkMatches() {
    startActivityForResult(TurnBasedMultiplayer.getInboxIntent(getApiClient()), CHECK_MATCHES);
  }

  public void configureGame(GoPlayer opponentPlayer, int boardSize) {
    this.boardSize = boardSize;
    if (opponentPlayer.getType() == PlayerType.REMOTE) {
      setWaitingScreenVisible(true);
      startActivityForResult(TurnBasedMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 1), SELECT_PLAYER);
    } else {
      displayGameConfigurationScreen(opponentPlayer, boardSize);
    }
  }

  public void displayGameConfigurationScreen(GoPlayer opponentPlayer, int boardSize) {
    GameConfigurationFragment gameConfigurationFragment = GameConfigurationFragment.newInstance(opponentPlayer, boardSize);
    displayFragment(gameConfigurationFragment);
  }

  private void loadGame(String matchId) {
    TurnBasedMultiplayer.loadMatch(getApiClient(), matchId)
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
    displayFragment(GameFragment.newInstance(goGameController));
  }

  public void startLocalGame(GoGameController goGameController) {
    if (!goGameController.isLocalGame()) {
      throw new RuntimeException("startLocalGame() with non local game");
    }
    localGameController = goGameController;
    updateMatchSpinner(LocalMatchMenuItem.LOCAL_MATCH_ID, false);
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
    String myId = getMyId();
    takeTurn(goGameController, getOpponentId(myId));
    updateMatchSpinner();
  }

  public void keepTurn(GoGameController goGameController) {
    Log.d(TAG, "keepTurn: " + goGameController);
    String myId = getMyId();
    takeTurn(goGameController, myId);
  }

  public void finishTurn(GoGameController goGameController) {
    Log.d(TAG, "finishTurn: " + goGameController);
    String myId = getMyId();
    takeTurn(goGameController, myId);
    TurnBasedMultiplayer.finishMatch(getApiClient(), turnBasedMatch.getMatchId());
    updateMatchSpinner();
  }

  private void takeTurn(GoGameController goGameController, String myId) {
    byte[] gameDataBytes = goGameController.getGameData().toByteArray();
    TurnBasedMultiplayer.takeTurn(getApiClient(), turnBasedMatch.getMatchId(), gameDataBytes, myId);
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

            final GoGameController goGameController = createGoGameController();
            if (turnBasedMatch.getData() == null) {
              Log.d(TAG, "getData is null, saving a new game");
              takeTurn(goGameController, getMyId());
            }

            Log.d(TAG, "Game created, starting game activity...");
            updateMatchSpinner(turnBasedMatch.getMatchId(), true);
          }
        });
  }

  private GoGameController createGoGameController() {
    boolean myTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    if (myTurn && turnBasedMatch.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      TurnBasedMultiplayer.finishMatch(getApiClient(), turnBasedMatch.getMatchId());
    }

    String myId = getMyId();
    String opponentId = getOpponentId(myId);

    GameData gameData = getGameData(myId, opponentId);

    if (gameData.getMoveCount() == 0) {
      for (String participantId : turnBasedMatch.getParticipantIds()) {
        Log.i(TAG, String.format(" participant %s: player %s", participantId,
            getPlayerId(turnBasedMatch, participantId)));
      }
    }

    Map<String, GoPlayer> goPlayers = ImmutableMap.of(
        myId, createGoPlayer(myId, PlayerType.LOCAL),
        opponentId, createGoPlayer(opponentId, PlayerType.REMOTE));

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

  private GameData getGameData(String myId, String opponentId) {
    try {
      if (turnBasedMatch.getData() == null) {
        return GameDatas.createGameData(turnBasedMatch.getVariant(), GameDatas.DEFAULT_HANDICAP,
            GameDatas.DEFAULT_KOMI, myId, opponentId);
      } else {
        return GameData.parseFrom(turnBasedMatch.getData());
      }
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
  }

  private String getOpponentId(String id) {
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(id)) {
        return participantId;
      }
    }
    return null;
  }

  private String getMyId() {
    return turnBasedMatch.getParticipantId(Players.getCurrentPlayerId(getApiClient()));
  }

  private GoPlayer createGoPlayer(String participantId, PlayerType playerType) {
    return createGoPlayer(turnBasedMatch, participantId, playerType);
  }

  private GoPlayer createGoPlayer(TurnBasedMatch match, String participantId, PlayerType playerType) {
    GoPlayer goPlayer;
    if (isParticipantAutoMatch(match, participantId)) {
      goPlayer = new GoPlayer(playerType, participantId, getString(R.string.opponent_default_name));
    } else {
      Player player = match.getParticipant(participantId).getPlayer();
      goPlayer = new GoPlayer(playerType, participantId, player.getDisplayName());
      getAvatarManager().setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    }
    return goPlayer;
  }

  private boolean isParticipantAutoMatch(TurnBasedMatch match, String participantId) {
    return participantId == null || match.getParticipant(participantId).getPlayer() == null;
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
    Achievements.unlock(getApiClient(), achievementId);
  }

  public Player getLocalPlayer() {
    return Players.getCurrentPlayer(getApiClient());
  }


  private void handleMatchMenuItemSelection(MatchMenuItem item) {
    Log.d(TAG, "handleMatchMenuItemSelection: " + item.getMatchId());
    GameStarter gameStarter = new GameStarter() {
      @Override
      public void startNewGame() {
        turnBasedMatch = null;
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
    };
    item.start(gameStarter);
  }

  public void setWaitingScreenVisible(boolean visible) {
    findViewById(R.id.waiting_view).setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public class MatchDescription {
    private final long creationTimestamp;
    private final long lastUpdateTimestamp;
    private final int turnStatus;
    private final String matchId;
    private final GameData gameData;
    private final GoPlayer blackPlayer;
    private final GoPlayer whitePlayer;

    public MatchDescription(TurnBasedMatch match) {
      this.creationTimestamp = match.getCreationTimestamp();
      this.lastUpdateTimestamp = match.getLastUpdatedTimestamp();
      this.turnStatus = match.getTurnStatus();
      this.matchId = match.getMatchId();
      try {
        this.gameData = GameData.parseFrom(match.getData());
      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException("Invalid GameData");
      }

      this.blackPlayer = createGoPlayer(match, gameData.getGameConfiguration().getBlackId(), null);
      this.whitePlayer = createGoPlayer(match, gameData.getGameConfiguration().getWhiteId(), null);
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
      return blackPlayer;
    }

    public GoPlayer getWhitePlayer() {
      return whitePlayer;
    }
  }
}
