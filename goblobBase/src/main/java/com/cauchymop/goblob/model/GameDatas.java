package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.GameDataOrBuilder;
import com.cauchymop.goblob.proto.PlayGameData.GameType;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData.Position;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.Move;

/**
 * Helper class to build {@link PlayGameData} related messages.
 */
@Singleton
public class GameDatas {

  public static final String NEW_GAME_MATCH_ID = "new game";
  public static final int VERSION = 3;  // Adding sequence_number.

  private static final float DEFAULT_KOMI = 7.5f;
  private static final int DEFAULT_HANDICAP = 0;
  private static final int DEFAULT_BOARD_SIZE = 9;

  @Inject
  public GameDatas() {
  }

  public boolean needsApplicationUpdate(GameData gameData) {
    return gameData.getVersion() > GameDatas.VERSION;
  }

  public PlayGameData.Color getCurrentColor(GameDataOrBuilder gameData) {
    return gameData.getTurn();
  }

  public boolean isLocalTurn(GameDataOrBuilder gameData) {
    return getCurrentPlayer(gameData).getIsLocal() && !(gameData.getPhase() == Phase.FINISHED);
  }

  public PlayGameData.Color getOpponentColor(GameConfiguration gameConfiguration) {
    PlayGameData.Color localColor = getLocalColor(gameConfiguration);
    return getOppositeColor(localColor);
  }

  public PlayGameData.Color getOppositeColor(PlayGameData.Color color) {
    return (color == PlayGameData.Color.BLACK) ? PlayGameData.Color.WHITE : PlayGameData.Color.BLACK;
  }

  public PlayGameData.Color getLocalColor(GameConfiguration gameConfiguration) {
    if (gameConfiguration.getGameType() == GameType.LOCAL) {
      return PlayGameData.Color.BLACK;
    }
    GoPlayer black = gameConfiguration.getBlack();
    GoPlayer white = gameConfiguration.getWhite();

    if (black.getIsLocal()) {
      return PlayGameData.Color.BLACK;
    } else if (white.getIsLocal()) {
      return PlayGameData.Color.WHITE;
    } else {
      throw new RuntimeException("Local Player is neither black or white, maybe this is a Connect4 Game...!");
    }
  }

  public GoPlayer getCurrentPlayer(GameDataOrBuilder gameData) {
    return getGoPlayer(gameData, getCurrentColor(gameData));
  }

  public GoPlayer getGoPlayer(GameDataOrBuilder gameData, PlayGameData.Color color) {
    return color == PlayGameData.Color.BLACK ? getBlackPlayer(gameData) : getWhitePlayer(gameData);
  }

  public Move createPassMove() {
    return Move.newBuilder().setType(Move.MoveType.PASS).build();
  }

  public Move createMove(int x, int y) {
    return Move.newBuilder()
        .setType(Move.MoveType.MOVE)
        .setPosition(createPosition(x, y))
        .build();
  }

  public Position createPosition(int x, int y) {
    return Position.newBuilder()
        .setX(x)
        .setY(y)
        .build();
  }

  public GameData createNewGameData(String matchId,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer) {
    GameConfiguration gameConfiguration = createGameConfiguration(DEFAULT_BOARD_SIZE, DEFAULT_HANDICAP, DEFAULT_KOMI, gameType, blackPlayer, whitePlayer);
    GameData.Builder builder = GameData.newBuilder()
        .setVersion(VERSION)
        .setMatchId(matchId)
        .setPhase(Phase.INITIAL)
        .setGameConfiguration(gameConfiguration)
        .addAllMove(ImmutableList.of());
    builder.setTurn(getLocalColor(gameConfiguration));
    return builder.build();
  }


  private GameConfiguration createGameConfiguration(int size, int handicap, float komi,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer) {
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
        .build();
  }

  public GoPlayer createGamePlayer(String id, String name, boolean isLocal) {
    return GoPlayer.newBuilder()
        .setId(id)
        .setName(name)
        .setIsLocal(isLocal)
        .build();
  }

  public boolean isLocalGame(GameDataOrBuilder gameData) {
    return gameData.getGameConfiguration().getGameType() == GameType.LOCAL;
  }

  private GoPlayer getWhitePlayer(GameDataOrBuilder gameData) {
    return gameData.getGameConfiguration().getWhite();
  }

  private GoPlayer getBlackPlayer(GameDataOrBuilder gameData) {
    return gameData.getGameConfiguration().getBlack();
  }

  public boolean isRemoteGame(GameData gameData) {
    return !isLocalGame(gameData);
  }

  public PlayGameData.Color computeInGameTurn(GameConfiguration gameConfiguration, int moveCount) {
    boolean hasHandicap = gameConfiguration.getHandicap() > 0;
    boolean isBlackTurn = moveCount % 2 == (hasHandicap ? 1 : 0);
    return isBlackTurn ? PlayGameData.Color.BLACK : PlayGameData.Color.WHITE;
  }
}
