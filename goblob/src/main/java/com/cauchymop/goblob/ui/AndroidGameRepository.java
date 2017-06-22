package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.GameList;
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
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;

import static com.google.android.gms.games.Games.TurnBasedMultiplayer;

/**
 * Class to persist games.
 */
@Singleton
public class AndroidGameRepository extends GameRepository implements OnTurnBasedMatchUpdateReceivedListener {

  private static final String TAG = AndroidGameRepository.class.getName();
  private static final int CACHE_CHANGED_MESSAGE = 1;
  private static final long CACHE_CHANGED_DELAY = 100;
  private static final String IGNORED_VALUE = "";

  private final SharedPreferences prefs;
  private final GoogleApiClient googleApiClient;

  private AvatarManager avatarManager;

  private final Handler cacheRefreshHandler = new CacheRefreshHandler(this);

  @Inject
  public AndroidGameRepository(SharedPreferences prefs, GameDatas gameDatas,
      GoogleApiClient googleApiClient, AvatarManager avatarManager, Analytics analytics,
      @Named("PlayerOneDefaultName") Lazy<String> playerOneDefaultName,
      @Named("PlayerTwoDefaultName") String playerTwoDefaultName) {
    super(analytics, playerOneDefaultName, playerTwoDefaultName, gameDatas);
    this.prefs = prefs;
    this.googleApiClient = googleApiClient;
    this.avatarManager = avatarManager;
    gameCache = loadGameList();
    loadLegacyLocalGame();
    fireGameListChanged();
  }

  @Override
  protected void forceCacheRefresh() {
    requestCacheRefresh(true);
  }
  @Override
  protected void requestCacheRefresh(boolean immediate) {
    log("CacheRefreshHandler forceCacheRefresh()");
    cacheRefreshHandler.removeMessages(CACHE_CHANGED_MESSAGE);
    if (immediate) {
      cacheRefreshHandler.handleMessage(cacheRefreshHandler.obtainMessage(CACHE_CHANGED_MESSAGE));
    } else {
      cacheRefreshHandler.sendEmptyMessageDelayed(CACHE_CHANGED_MESSAGE, CACHE_CHANGED_DELAY);
    }
  }

  private void persistCache() {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(GAMES, TextFormat.printToString(gameCache));
    editor.apply();
  }

  @Override
  public boolean publishRemoteGameState(GameData gameData) {
    if (googleApiClient.isConnected()) {
      Log.d(TAG, "publishRemoteGameState: " + gameData);
      String turnParticipantId = gameDatas.getCurrentPlayer(gameData).getId();
      byte[] gameDataBytes = gameData.toByteArray();
      Log.d(TAG, "takeTurn " + turnParticipantId);
      TurnBasedMultiplayer.takeTurn(googleApiClient, gameData.getMatchId(), gameDataBytes, turnParticipantId);
      if (gameData.getPhase() == Phase.FINISHED) {
        TurnBasedMultiplayer.finishMatch(googleApiClient, gameData.getMatchId());
        fireGameSelected(gameData);
      }
      return true;
    } else {
      gameCache.putUnpublished(gameData.getMatchId(), IGNORED_VALUE);
      return false;
    }
  }

  @Override
  protected void log(String message) {
    Log.d(AndroidGameRepository.TAG, message);
  }

  private void loadLegacyLocalGame() {
    String gameDataString = prefs.getString(GAME_DATA, null);
    if (gameDataString == null) {
      return;
    }
    log("loadLegacyLocalGame");
    GameData.Builder gameDataBuilder = GameData.newBuilder();
    try {
      TextFormat.merge(gameDataString, gameDataBuilder);
    } catch (TextFormat.ParseException e) {
      Log.e(TAG, "Error parsing local GameData: " + e.getMessage());
    }
    GameData localGame = gameDataBuilder.build();
    saveToCache(localGame);
    prefs.edit().remove(GAME_DATA).apply();
  }

  private GameList.Builder loadGameList() {
    log("loadGameList");
    String gameListString = prefs.getString(GAMES, "");
    GameList.Builder gameListBuilder = GameList.newBuilder();
    try {
      TextFormat.merge(gameListString, gameListBuilder);
    } catch (TextFormat.ParseException e) {
      Log.e(TAG, "Error parsing local GameList: " + e.getMessage());
    }
    log("loadGameList: " + gameListBuilder.getGames().size() + " games loaded.");
    return gameListBuilder;
  }

  public void refreshRemoteGameListFromServer() {
    Log.d(TAG, "refreshRemoteGameListFromServer");
    final long requestId = System.currentTimeMillis();

    PendingResult<com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchesResult> matchListResult =
        TurnBasedMultiplayer.loadMatchesByStatus(googleApiClient,
            Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION,
            new int[]{TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN});
    ResultCallback<TurnBasedMultiplayer.LoadMatchesResult> matchListResultCallBack =
        new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
          @Override
          public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
            Log.d(TAG, String.format("matchResult: requestId = %d, latency = %d ms", requestId, System.currentTimeMillis() - requestId));
            LoadMatchesResponse matches = loadMatchesResult.getMatches();

            ImmutableList<TurnBasedMatch> allMatches = ImmutableList.<TurnBasedMatch>builder()
                .addAll(denullify(matches.getMyTurnMatches()))
                .addAll(denullify(matches.getTheirTurnMatches()))
                .addAll(denullify(matches.getCompletedMatches()))
                .build();

            Set<GameData> games = new HashSet<>();
            for (TurnBasedMatch match : allMatches) {
              updateAvatars(match);
              GameData gameData = getGameData(match);
              if (gameData != null) {
                games.add(gameData);
              }
            }
            if (clearRemoteGamesIfAbsent(games)) {
              forceCacheRefresh();
            }
            for (GameData game : games) {
              saveToCache(game);
            }
          }
        };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private boolean clearRemoteGamesIfAbsent(final Set<GameData> games) {
    return Iterables.removeIf(gameCache.getMutableGames().values(), new Predicate<GameData>() {
      @Override
      public boolean apply(GameData gameData) {
        return gameDatas.isRemoteGame(gameData) && !games.contains(gameData);
      }
    });
  }

  private void updateAvatars(TurnBasedMatch match) {
    for (Participant participant : match.getParticipants()) {
      Player player = participant.getPlayer();
      avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    }
  }

  private Iterable<TurnBasedMatch> denullify(@Nullable TurnBasedMatchBuffer nullableIterable) {
    return nullableIterable == null ? ImmutableList.of() : nullableIterable;
  }

  public GameData getGameData(@NonNull TurnBasedMatch turnBasedMatch) {
    byte[] data = turnBasedMatch.getData();
    if (data == null) {
      // When a crash happens during game creation, the TurnBasedMatch contains a null game
      // that can't be recovered, and would prevent starting the app.
      return null;
    }

    handleMatchStatusComplete(turnBasedMatch);
    try {
      GameData gameData = GameData.parseFrom(data);
      gameData = handleBackwardCompatibility(turnBasedMatch, gameData);
      return gameData;
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void handleMatchStatusComplete(@NonNull TurnBasedMatch turnBasedMatch) {
    boolean myTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    if (myTurn && turnBasedMatch.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      TurnBasedMultiplayer.finishMatch(googleApiClient, turnBasedMatch.getMatchId());
    }
  }

  private GameData createNewGameData(TurnBasedMatch turnBasedMatch) {
    String myId = getMyId(turnBasedMatch);
    String opponentId = getOpponentId(turnBasedMatch);
    PlayGameData.GoPlayer blackPlayer = createGoPlayer(turnBasedMatch, myId, true);
    PlayGameData.GoPlayer whitePlayer = createGoPlayer(turnBasedMatch, opponentId, false);
    GameData gameData = gameDatas.createNewGameData(turnBasedMatch.getMatchId(),
        PlayGameData.GameType.REMOTE, blackPlayer, whitePlayer);
    analytics.gameCreated(gameData);

    commitGameChanges(gameData);
    return gameData;
  }

  @NonNull
  private GameData handleBackwardCompatibility(TurnBasedMatch turnBasedMatch,
      GameData initialGameData) {
    GameData.Builder gameData = initialGameData.toBuilder();

    // No players
    if (!gameData.getGameConfiguration().hasBlack() || !gameData.getGameConfiguration().hasWhite()) {
      String myId = getMyId(turnBasedMatch);
      String opponentId = getOpponentId(turnBasedMatch);
      Map<String, PlayGameData.GoPlayer> goPlayers = ImmutableMap.of(
          myId, createGoPlayer(turnBasedMatch, myId, true),
          opponentId, createGoPlayer(turnBasedMatch, opponentId, false));
      PlayGameData.GameConfiguration gameConfiguration = gameData.getGameConfiguration();

      PlayGameData.GoPlayer blackPlayer = goPlayers.get(gameConfiguration.getBlackId());
      PlayGameData.GoPlayer whitePlayer = goPlayers.get(gameConfiguration.getWhiteId());

      gameData.getGameConfigurationBuilder().setBlack(blackPlayer).setWhite(whitePlayer);
    }

    // No match Id
    if (Strings.isNullOrEmpty(gameData.getMatchId())) {
      gameData.setMatchId(turnBasedMatch.getMatchId());
    }

    // No phase (version < 2)
    if (gameData.getPhase() == Phase.UNKNOWN) {
      final Phase result;
      if (gameData.hasMatchEndStatus()) {
        if (gameData.getMatchEndStatus().getGameFinished()) {
          result = Phase.FINISHED;
        } else {
          result = Phase.DEAD_STONE_MARKING;
        }
      } else {
        result = Phase.IN_GAME;
      }
      gameData.setPhase(result);
    }

    // No turn (version < 2)
    if (!gameData.hasTurn()) {
      if (gameData.hasMatchEndStatus()) {
        gameData.setTurn(gameData.getMatchEndStatus().getTurn());
      } else {
        PlayGameData.Color currentTurn = gameDatas.computeInGameTurn(gameData.getGameConfiguration(), gameData.getMoveCount());
        gameData.setTurn(currentTurn);
      }
    }

    return fillLocalStates(turnBasedMatch, gameData).build();
  }

  private GameData.Builder fillLocalStates(TurnBasedMatch turnBasedMatch, GameData.Builder gameData) {
    boolean isMyTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    boolean turnIsBlack = gameData.getTurn() == PlayGameData.Color.BLACK;
    boolean iAmBlack = isMyTurn && turnIsBlack || (!isMyTurn && !turnIsBlack);
    PlayGameData.GameConfiguration.Builder gameConfiguration = gameData.getGameConfigurationBuilder();
    PlayGameData.GoPlayer.Builder blackPlayer = gameConfiguration.getBlackBuilder();
    PlayGameData.GoPlayer.Builder whitePlayer = gameConfiguration.getWhiteBuilder();
    blackPlayer.setIsLocal(iAmBlack);
    whitePlayer.setIsLocal(!iAmBlack);
    Log.d(TAG, String.format("black: %s", blackPlayer));
    Log.d(TAG, String.format("white %s", whitePlayer));
    return gameData;
  }

  @NonNull
  private String getOpponentId(TurnBasedMatch turnBasedMatch) {
    String myId = getMyId(turnBasedMatch);
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(myId)) {
        return participantId;
      }
    }
    throw new RuntimeException("Our TurnBasedMatch should contain 2 players!");
  }

  private String getMyId(TurnBasedMatch turnBasedMatch) {
    return turnBasedMatch.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));
  }

  private PlayGameData.GoPlayer createGoPlayer(TurnBasedMatch match, String participantId,
      boolean isLocal) {
    PlayGameData.GoPlayer goPlayer;
    Player player = match.getParticipant(participantId).getPlayer();
    goPlayer = gameDatas.createGamePlayer(participantId, player.getDisplayName(), isLocal);
    avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    return goPlayer;
  }

  @Override
  public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchReceived");
    GameData gameData = getGameData(turnBasedMatch);
    if (gameData != null) {
      saveToCache(gameData);
    }
  }

  @Override
  public void onTurnBasedMatchRemoved(String matchId) {
    Log.d(TAG, "onTurnBasedMatchRemoved: " + matchId);
    removeFromCache(matchId);
  }

  public void handlePlayersSelected(Intent intent) {
    Log.d(TAG, "handlePlayersSelected");

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
        .setVariant(TurnBasedMatch.MATCH_VARIANT_DEFAULT)
        .setAutoMatchCriteria(autoMatchCriteria).build();

    // kick the match off
    TurnBasedMultiplayer.createMatch(googleApiClient, turnBasedMatchConfig)
        .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
          @Override
          public void onResult(
              @NonNull TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
            Log.d(TAG, "InitiateMatchResult " + initiateMatchResult);
            if (!initiateMatchResult.getStatus().isSuccess()) {
              return;
            }
            TurnBasedMatch turnBasedMatch = initiateMatchResult.getMatch();
            createNewGameData(turnBasedMatch);

            Log.d(TAG, "Game created...");
            selectGame(turnBasedMatch.getMatchId());
          }
        });
  }

  public void handleCheckMatchesResult(int responseCode, Intent intent) {
    Log.d(TAG, "handleCheckMatchesResult");
    refreshRemoteGameListFromServer();
    if (responseCode == Activity.RESULT_OK) {
      final TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
      selectGame(match.getMatchId());
    }
  }

  private static class CacheRefreshHandler extends Handler {

    private final AndroidGameRepository androidGameRepository;

    public CacheRefreshHandler(AndroidGameRepository androidGameRepository) {
      this.androidGameRepository = androidGameRepository;
    }

    @Override
    public void handleMessage(Message msg) {
      Log.d(TAG, "CacheRefreshHandler handleMessage");
      androidGameRepository.persistCache();
      androidGameRepository.fireGameListChanged();
    }
  }
}
