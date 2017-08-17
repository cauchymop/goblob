package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Named;

import dagger.Lazy;

public abstract class GameRepository {


  protected static final String GAME_DATA = "gameData";
  protected static final String GAMES = "games";
  private static final String LOCAL_MATCH_ID = "local";
  private static final String PLAYER_ONE_ID = "player1";
  private static final String PLAYER_TWO_ID = "player2";
  protected final GameDatas gameDatas;
  protected final Lazy<String> playerOneDefaultName;
  protected final String playerTwoDefaultName;
  private final Predicate<PlayGameData.GameData> isLocalTurnPredicate = new Predicate<PlayGameData.GameData>() {
    @Override
    public boolean apply(PlayGameData.GameData gameData) {
      return gameDatas.isLocalTurn(gameData);
    }
  };
  protected Analytics analytics;
  protected PlayGameData.GameList.Builder gameCache;
  private String currentMatchId;
  private List<GameRepositoryListener> listeners = Lists.newArrayList();

  public GameRepository(
      Analytics analytics,
      @Named("PlayerOneDefaultName") Lazy<String> playerOneDefaultName,
      @Named("PlayerTwoDefaultName") String playerTwoDefaultName, GameDatas gameDatas) {
    this.analytics = analytics;
    this.playerOneDefaultName = playerOneDefaultName;
    this.playerTwoDefaultName = playerTwoDefaultName;
    this.gameDatas = gameDatas;
  }

  public void commitGameChanges(PlayGameData.GameData gameData) {
    saveToCache(gameData);
    if (gameDatas.isRemoteGame(gameData)) {
      publishRemoteGameState(gameData);
    }
    requestCacheRefresh(false);
  }

  protected abstract void forceCacheRefresh();

  protected abstract void requestCacheRefresh(boolean immediate);

  protected void saveToCache(PlayGameData.GameData gameData) {
    log("saveToCache " + gameData.getMatchId());
    PlayGameData.GameData existingGame = gameCache.getGames().get(gameData.getMatchId());
    log(" -> existingGame found = " + (existingGame != null));
    if (existingGame == null || gameData.getSequenceNumber() > existingGame.getSequenceNumber()) {
      gameCache.getMutableGames().put(gameData.getMatchId(), gameData);
      forceCacheRefresh();
      fireGameChanged(gameData);
    } else {
      log(String.format("Ignoring GameData with an old or same sequence number (%s when existing is %s)", gameData.getSequenceNumber(), existingGame.getSequenceNumber() ));
    }
  }

  public void publishUnpublishedGames() {
    for (String matchId : ImmutableSet.copyOf(gameCache.getUnpublishedMap().keySet())) {
      PlayGameData.GameData gameData = gameCache.getGamesMap().get(matchId);
      // The match can be absent if the user changed.
      if (gameData != null && publishRemoteGameState(gameData)) {
        gameCache.removeUnpublished(gameData.getMatchId());
      }
    }
  }

  public abstract boolean publishRemoteGameState(PlayGameData.GameData gameData);

  protected void removeFromCache(String matchId) {
    log("removeFromCache " + matchId);
    gameCache.getMutableGames().remove(matchId);
    forceCacheRefresh();
  }

  public void selectGame(String matchId) {
    log("selectGame matchId = " + matchId);
    if (Objects.equal(currentMatchId, matchId)) {
      return;
    }
    currentMatchId = matchId;
    if (matchId.equals(GameDatas.NEW_GAME_MATCH_ID)) {
      fireGameSelected(null);
    } else {
      fireGameSelected(gameCache.getGames().get(matchId));
    }
  }

  public void addGameRepositoryListener(GameRepositoryListener listener) {
    listeners.add(listener);
    listener.gameChanged(getCurrentGame());
    listener.gameListChanged();
  }

  public void removeGameRepositoryListener(GameRepositoryListener listener) {
    listeners.remove(listener);
  }

  protected void fireGameListChanged() {
    for (GameRepositoryListener listener : listeners) {
      listener.gameListChanged();
    }
  }

  private void fireGameChanged(PlayGameData.GameData gameData) {
    for (GameRepositoryListener listener : listeners) {
      listener.gameChanged(gameData);
    }
  }

  protected void fireGameSelected(PlayGameData.GameData gameData) {
    for (GameRepositoryListener listener : listeners) {
      listener.gameSelected(gameData);
    }
  }

  protected abstract void log(String message);

  public String getCurrentMatchId() {
    return currentMatchId;
  }

  public Iterable<PlayGameData.GameData> getMyTurnGames() {
    return Iterables.filter(gameCache.getGames().values(), isLocalTurnPredicate);
  }

  public Iterable<PlayGameData.GameData> getTheirTurnGames() {
    return Iterables.filter(gameCache.getGames().values(), Predicates.not(isLocalTurnPredicate));
  }

  public PlayGameData.GameData createNewLocalGame() {
    PlayGameData.GoPlayer black = gameDatas.createGamePlayer(PLAYER_ONE_ID, playerOneDefaultName.get(), true);
    PlayGameData.GoPlayer white = gameDatas.createGamePlayer(PLAYER_TWO_ID, playerTwoDefaultName, true);
    removeFromCache(LOCAL_MATCH_ID);
    PlayGameData.GameData localGame = gameDatas.createNewGameData(LOCAL_MATCH_ID, PlayGameData.GameType.LOCAL, black, white);
    analytics.gameCreated(localGame);
    commitGameChanges(localGame);
    return localGame;
  }

  public PlayGameData.GameData getCurrentGame() {
    return gameCache.getGamesMap().get(getCurrentMatchId());
  }

  public interface GameRepositoryListener {
    void gameListChanged();

    void gameChanged(PlayGameData.GameData gameData);

    void gameSelected(PlayGameData.GameData gameData);
  }
}
