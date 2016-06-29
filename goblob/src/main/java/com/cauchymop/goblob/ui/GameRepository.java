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

import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
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
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;

import static com.google.android.gms.games.Games.TurnBasedMultiplayer;

/**
 * Class to persist games.
 */
@Singleton
public class GameRepository implements OnTurnBasedMatchUpdateReceivedListener {

  private static final String TAG = GameRepository.class.getName();
  private static final String LOCAL_MATCH_ID = "local";
  private static final String PLAYER_ONE_ID = "player1";
  private static final String PLAYER_TWO_ID = "player2";
  private static final String GAME_DATA = "gameData";
  private static final String GAMES = "games";
  private static final int CACHE_CHANGED_MESSAGE = 1;
  private static final long CACHE_CHANGED_DELAY = 100;


  private final SharedPreferences prefs;
  private final GameDatas gameDatas;
  private final GoogleApiClient googleApiClient;
  private final Lazy<String> playerOneDefaultName;
  private final String playerTwoDefaultName;

  private String currentMatchId;

  private GameList.Builder gameCache;

  private AvatarManager avatarManager;
  private String localUniqueId;
  private List<GameRepositoryListener> listeners = Lists.newArrayList();

  private final Predicate<GameData> isLocalTurnPredicate = new Predicate<GameData>() {
    @Override
    public boolean apply(GameData gameData) {
      return gameDatas.isLocalTurn(gameData);
    }
  };
  private Predicate<GameData> isRemoteGamePredicate = new Predicate<GameData>() {
    @Override
    public boolean apply(GameData gameData) {
      return gameDatas.isRemoteGame(gameData);
    }
  };

  private final Handler cacheRefreshHandler = new CacheRefreshHandler(this);


  @Inject
  public GameRepository(SharedPreferences prefs, GameDatas gameDatas,
      GoogleApiClient googleApiClient, AvatarManager avatarManager,
      @Named("PlayerOneDefaultName") Lazy<String> playerOneDefaultName,
      @Named("PlayerTwoDefaultName") String playerTwoDefaultName,
      @Named("LocalUniqueId") String localUniqueId) {
    this.prefs = prefs;
    this.gameDatas = gameDatas;
    this.googleApiClient = googleApiClient;
    this.avatarManager = avatarManager;
    this.playerOneDefaultName = playerOneDefaultName;
    this.playerTwoDefaultName = playerTwoDefaultName;
    this.localUniqueId = localUniqueId;
    gameCache = loadGameList();
    loadLegacyLocalGame();
    fireGameListChanged();
  }

  public void saveGame(GameData gameData) {
    GameData existingGameData = gameCache.getGames().get(gameData.getMatchId());
    if (existingGameData == null || gameData.getSequenceNumber() > existingGameData.getSequenceNumber()) {
      Log.d(TAG, "Updating GameData with a new sequence number");
      saveToCache(gameData);
      if (gameDatas.isRemoteGame(gameData)) {
        publishRemoteGameState(gameData);
      }
    } else if (gameData.getSequenceNumber() == existingGameData.getSequenceNumber()) {
      throw new RuntimeException("Can't happen: sequence numbers are equal");
    } else {
      Log.d(TAG, "Ignoring GameData with an old sequence number");
    }
  }

  private void postCacheRefresh() {
    Log.d(TAG, "CacheRefreshHandler postCacheRefresh()");
    cacheRefreshHandler.removeMessages(CACHE_CHANGED_MESSAGE);
    cacheRefreshHandler.sendEmptyMessageDelayed(CACHE_CHANGED_MESSAGE, CACHE_CHANGED_DELAY);
  }

  private void saveToCache(@NonNull GameData gameData) {
    Log.d(TAG, "saveToCache " + gameData.getMatchId());
    GameData existingGame = gameCache.getGames().get(gameData.getMatchId());
    if (existingGame == null || !existingGame.equals(gameData)) {
      gameCache.getMutableGames().put(gameData.getMatchId(), gameData);
      postCacheRefresh();
      fireGameChanged(gameData);
    }
  }

  private void persistCache() {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(GAMES, TextFormat.printToString(gameCache));
    editor.apply();
  }

  public void publishRemoteGameState(GameData gameData) {
    Log.d(TAG, "publishRemoteGameState: " + gameData);
    String turnParticipantId = gameDatas.getCurrentPlayer(gameData).getId();
    byte[] gameDataBytes = gameData.toByteArray();
    Log.d(TAG, "takeTurn " + turnParticipantId);
    TurnBasedMultiplayer.takeTurn(googleApiClient, gameData.getMatchId(), gameDataBytes, turnParticipantId);
    if (gameData.getPhase() == Phase.FINISHED) {
      TurnBasedMultiplayer.finishMatch(googleApiClient, gameData.getMatchId());
      fireGameSelected(gameData);
    }
  }

  private void loadLegacyLocalGame() {
    String gameDataString = prefs.getString(GAME_DATA, null);
    if (gameDataString == null) {
      return;
    }
    Log.i(TAG, "loadLegacyLocalGame");
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
    Log.i(TAG, "loadGameList");
    String gameListString = prefs.getString(GAMES, "");
    GameList.Builder gameListBuilder = GameList.newBuilder();
    try {
      TextFormat.merge(gameListString, gameListBuilder);
    } catch (TextFormat.ParseException e) {
      Log.e(TAG, "Error parsing local GameList: " + e.getMessage());
    }
    Log.i(TAG, "loadGameList: " + gameListBuilder.getGames().size() + " games loaded.");
    return gameListBuilder;
  }

  public void refreshRemoteGameListFromServer() {
    Log.e(TAG, "refreshRemoteGameListFromServer");
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
            clearRemoteGames();
            ImmutableList<TurnBasedMatch> allMatches = ImmutableList.<TurnBasedMatch>builder()
                .addAll(denullify(matches.getCompletedMatches()))
                .addAll(denullify(matches.getMyTurnMatches()))
                .addAll(denullify(matches.getTheirTurnMatches()))
                .build();
            for (TurnBasedMatch match : allMatches) {
              updateAvatars(match);
              GameData gameData = getGameData(match);
              saveToCache(gameData);
            }
            postCacheRefresh();
          }
        };
    matchListResult.setResultCallback(matchListResultCallBack);
  }

  private void clearRemoteGames() {
    Iterables.removeIf(gameCache.getMutableGames().values(), isRemoteGamePredicate);
  }

  private void updateAvatars(TurnBasedMatch match) {
    for (Participant participant : match.getParticipants()) {
      Player player = participant.getPlayer();
      avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    }
  }

  private Iterable<TurnBasedMatch> denullify(@Nullable TurnBasedMatchBuffer nullableIterable) {
    return nullableIterable == null ? ImmutableList.<TurnBasedMatch>of() : nullableIterable;
  }

  public GameData getGameData(@NonNull TurnBasedMatch turnBasedMatch) {
    handleMatchStatusComplete(turnBasedMatch);
    try {
      GameData gameData = GameData.parseFrom(turnBasedMatch.getData());
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
    PlayGameData.GoPlayer blackPlayer = createGoPlayer(turnBasedMatch, myId);
    PlayGameData.GoPlayer whitePlayer = createGoPlayer(turnBasedMatch, opponentId);
    GameData gameData = gameDatas.createNewGameData(turnBasedMatch.getMatchId(),
        PlayGameData.GameType.REMOTE, blackPlayer, whitePlayer);
    gameData = fillLocalUniqueId(turnBasedMatch, gameData.toBuilder()).build();
    saveGame(gameData);
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
          myId, createGoPlayer(turnBasedMatch, myId),
          opponentId, createGoPlayer(turnBasedMatch, opponentId));
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

    return fillLocalUniqueId(turnBasedMatch, gameData).build();
  }

  private GameData.Builder fillLocalUniqueId(TurnBasedMatch turnBasedMatch, GameData.Builder gameData) {
    boolean isMyTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    boolean turnIsBlack = gameData.getTurn() == PlayGameData.Color.BLACK;
    boolean iAmBlack = isMyTurn && turnIsBlack || (!isMyTurn && !turnIsBlack);
    PlayGameData.GoPlayer.Builder player = iAmBlack
        ? gameData.getGameConfigurationBuilder().getBlackBuilder()
        : gameData.getGameConfigurationBuilder().getWhiteBuilder();
    player.setLocalUniqueId(localUniqueId);

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
    return turnBasedMatch.getParticipantId(localUniqueId);
  }

  private PlayGameData.GoPlayer createGoPlayer(TurnBasedMatch match, String participantId) {
    PlayGameData.GoPlayer goPlayer;
    Player player = match.getParticipant(participantId).getPlayer();
    goPlayer = gameDatas.createGamePlayer(participantId, player.getDisplayName(), player.getPlayerId());
    avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    return goPlayer;
  }

  @Override
  public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchReceived");
    GameData gameData = getGameData(turnBasedMatch);
    saveToCache(gameData);
  }

  @Override
  public void onTurnBasedMatchRemoved(String matchId) {
    Log.d(TAG, "onTurnBasedMatchRemoved: " + matchId);
    removeFromCache(matchId);
  }

  private void removeFromCache(String matchId) {
    Log.d(TAG, "removeFromCache " + matchId);
    gameCache.getMutableGames().remove(matchId);
    postCacheRefresh();
  }

  public void selectGame(@NonNull String matchId) {
    Log.d(TAG, "selectGame matchId = " + matchId);
    currentMatchId = matchId;
    if (matchId.equals(GameDatas.NEW_GAME_MATCH_ID)) {
      fireGameSelected(null);
    } else {
      fireGameSelected(gameCache.getGames().get(matchId));
    }
  }

  public void handleSelectPlayersResult(int responseCode, Intent intent) {
    Log.d(TAG, "handleSelectPlayersResult");
    if (responseCode != Activity.RESULT_OK) {
      return;
    }
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

  public void addGameRepositoryListener(GameRepositoryListener listener) {
    listeners.add(listener);
  }

  public void removeGameRepositoryListener(GameRepositoryListener listener) {
    listeners.remove(listener);
  }

  private void fireGameListChanged() {
    for (GameRepositoryListener listener : listeners) {
      listener.gameListChanged();
    }
  }

  private void fireGameChanged(GameData gameData) {
    for (GameRepositoryListener listener : listeners) {
      listener.gameChanged(gameData);
    }
  }

  private void fireGameSelected(GameData gameData) {
    for (GameRepositoryListener listener : listeners) {
      listener.gameSelected(gameData);
    }
  }

  public String getCurrentMatchId() {
    return currentMatchId;
  }

  public Iterable<GameData> getMyTurnGames() {
    return Iterables.filter(gameCache.getGames().values(), isLocalTurnPredicate);
  }

  public Iterable<GameData> getTheirTurnGames() {
    return Iterables.filter(gameCache.getGames().values(), Predicates.not(isLocalTurnPredicate));
  }

  public GameData createNewLocalGame() {
    PlayGameData.GoPlayer black = gameDatas.createGamePlayer(PLAYER_ONE_ID, playerOneDefaultName.get());
    PlayGameData.GoPlayer white = gameDatas.createGamePlayer(PLAYER_TWO_ID, playerTwoDefaultName);
    removeFromCache(LOCAL_MATCH_ID);
    GameData localGame = gameDatas.createNewGameData(LOCAL_MATCH_ID, PlayGameData.GameType.LOCAL, black, white);
    saveGame(localGame);
    return localGame;
  }

  public interface GameRepositoryListener {
    void gameListChanged();

    void gameChanged(GameData gameData);

    void gameSelected(GameData gameData);
  }

  private static class CacheRefreshHandler extends Handler {

    private final GameRepository gameRepository;

    public CacheRefreshHandler(GameRepository gameRepository) {
      this.gameRepository = gameRepository;
    }

    @Override
    public void handleMessage(Message msg) {
      Log.d(TAG, "CacheRefreshHandler handleMessage");
      gameRepository.persistCache();
      gameRepository.fireGameListChanged();
    }
  }
}
