package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameType;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.Move;

/**
 * Helper class to build {@link PlayGameData} related messages.
 */
public class GameDatas {

  public static final float DEFAULT_KOMI = 7.5f;
  public static final int DEFAULT_HANDICAP = 0;
  public static final int VERSION = 1;
  public static final String PLAYER_ONE_ID = "player1";
  public static final String PLAYER_TWO_ID = "player2";
  public static final String LOCAL_MATCH_ID = "local";

  private final Lazy<String> playerOneDefaultName;
  private final String playerTwoDefaultName;
  private final Lazy<String> localGoogleIdentity;

  public enum Mode {
    START_GAME_NEGOTIATION,
    IN_GAME,
    END_GAME_NEGOTIATION
  }

  @Inject
  public GameDatas(@Named("PlayerOneDefaultName") Lazy<String> playerOneDefaultName,
      @Named("PlayerTwoDefaultName") String playerTwoDefaultName,
      @Named("LocalGoogleIdentity") Lazy<String> localGoogleIdentity) {
    this.playerOneDefaultName = playerOneDefaultName;
    this.playerTwoDefaultName = playerTwoDefaultName;
    this.localGoogleIdentity = localGoogleIdentity;
  }

  public String getRemotePlayerId(GameData gameData) {
    GoPlayer blackPlayer = getBlackPlayer(gameData);
    GoPlayer whitePlayer = getWhitePlayer(gameData);
    return isLocalPlayer(gameData, blackPlayer) ? blackPlayer.getId() : whitePlayer.getId();
  }

  public String getLocalPlayerId(GameData gameData) {
    GoPlayer blackPlayer = getBlackPlayer(gameData);
    GoPlayer whitePlayer = getWhitePlayer(gameData);
    return isLocalPlayer(gameData, blackPlayer) ? whitePlayer.getId() : blackPlayer.getId();
  }

  public PlayGameData.Color getCurrentColor(GameData gameData) {
    return gameData.getTurn();
  }

  public boolean isLocalPlayer(GameData gameData, GoPlayer player) {
    return isLocalGame(gameData) || player.getGoogleId().equals(localGoogleIdentity.get());
  }

  public boolean isLocalTurn(GameData gameData) {
    return isLocalPlayer(gameData, getCurrentPlayer(gameData)) && !isGameFinished(gameData);
  }

  public GoPlayer getCurrentPlayer(GameData gameData) {
    return getGoPlayer(gameData, getCurrentColor(gameData));
  }

  public GoPlayer getGoPlayer(GameData gameData, PlayGameData.Color color) {
    return color == PlayGameData.Color.BLACK ? getBlackPlayer(gameData) : getWhitePlayer(gameData);
  }

  public boolean isGameFinished(GameData gameData) {
    return gameData.getMatchEndStatus().getGameFinished();
  }

  public GoPlayer getWinner(GameData gameData) {
    return getGoPlayer(gameData, gameData.getMatchEndStatus().getScore().getWinner());
  }

  public Move createPassMove() {
    return Move.newBuilder().setType(Move.MoveType.PASS).build();
  }

  public Move createMove(int x, int y) {
    return Move.newBuilder()
        .setType(Move.MoveType.MOVE)
        .setPosition(PlayGameData.Position.newBuilder()
            .setX(x)
            .setY(y))
        .build();
  }

  public GameData createGameData(String matchId, int size, int handicap, float komi,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer, boolean accepted) {
    return createGameData(matchId, createGameConfiguration(size, handicap, komi, gameType, blackPlayer, whitePlayer, accepted),
        ImmutableList.<Move>of(), null);
  }

  public GameData createGameData(String matchId, GameConfiguration gameConfiguration) {
    return createGameData(matchId, gameConfiguration, Lists.<Move>newArrayList(), null);
  }

  public GameData createGameData(String matchId, GameConfiguration gameConfiguration,
      Iterable<Move> moves,
      MatchEndStatus matchEndStatus) {
    GameData.Builder builder = GameData.newBuilder()
        .setVersion(VERSION)
        .setMatchId(matchId)
        .setGameConfiguration(gameConfiguration)
        .addAllMove(moves);
    if (matchEndStatus != null) {
      builder.setMatchEndStatus(matchEndStatus);
    }
    return builder.build();
  }


  public GameConfiguration createGameConfiguration(int size, int handicap, float komi,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer, boolean accepted) {
    return GameConfiguration.newBuilder()
        .setBoardSize(size)
        .setHandicap(handicap)
        .setKomi(komi)
        .setBlackId(blackPlayer.getId())
        .setWhiteId(whitePlayer.getId())
        .setBlack(blackPlayer)
        .setWhite(whitePlayer)
        .setGameType(gameType)
        .setScoreType(GameConfiguration.ScoreType.JAPANESE)
        .setAccepted(accepted)
        .build();
  }

  public GoPlayer createGamePlayer(String id, String name) {
    return GoPlayer.newBuilder()
        .setId(id)
        .setName(name)
        .build();
  }

  public GoPlayer createGamePlayer(String id, String name, String googleId) {
    return GoPlayer.newBuilder()
        .setId(id)
        .setGoogleId(googleId)
        .setName(name)
        .build();
  }

  public GameData createLocalGame(int boardSize) {
    GoPlayer black = createGamePlayer(GameDatas.PLAYER_ONE_ID, playerOneDefaultName.get());
    GoPlayer white = createGamePlayer(GameDatas.PLAYER_TWO_ID, playerTwoDefaultName);
    return createGameData(LOCAL_MATCH_ID, createGameConfiguration(boardSize, DEFAULT_HANDICAP, DEFAULT_KOMI, GameType.LOCAL, black, white, true));
  }

  public boolean isLocalGame(GameData gameData) {
    return gameData.getGameConfiguration().getGameType() == GameType.LOCAL;
  }

  public Mode getMode(GameData gameData) {
    if (!gameData.getGameConfiguration().getAccepted()) {
      return Mode.START_GAME_NEGOTIATION;
    }
    if (gameData.hasMatchEndStatus()) {
      return Mode.END_GAME_NEGOTIATION;
    }
    return Mode.IN_GAME;
  }

  private GoPlayer getWhitePlayer(GameData gameData) {
    return gameData.getGameConfiguration().getWhite();
  }

  private GoPlayer getBlackPlayer(GameData gameData) {
    return gameData.getGameConfiguration().getBlack();
  }
}
