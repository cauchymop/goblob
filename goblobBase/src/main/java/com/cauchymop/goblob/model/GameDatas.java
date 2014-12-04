package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

import static com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;
import static com.cauchymop.goblob.proto.PlayGameData.Move;

/**
 * Helper class to build {@link PlayGameData} related messages.
 */
public class GameDatas {

  public static final float DEFAULT_KOMI = 7.5f;
  public static final int DEFAULT_HANDICAP = 0;

  public static Move createPassMove() {
    return Move.newBuilder().setType(Move.MoveType.PASS).build();
  }

  public static Move createMove(int x, int y) {
    return Move.newBuilder()
        .setType(Move.MoveType.MOVE)
        .setPosition(PlayGameData.Position.newBuilder()
            .setX(x)
            .setY(y))
        .build();
  }

  public static GameData createGameData(int size, int handicap, float komi, String blackId,
      String whiteId) {
    return GameData.newBuilder()
        .setGameConfiguration(createGameConfiguration(size, handicap, komi, blackId, whiteId))
        .build();
  }

  public static GameConfiguration createGameConfiguration(int size, int handicap, float komi,
      String blackId, String whiteId) {
    return GameConfiguration.newBuilder()
        .setBoardSize(size)
        .setHandicap(handicap)
        .setKomi(komi)
        .setBlackId(blackId)
        .setWhiteId(whiteId)
        .setScoreType(GameConfiguration.ScoreType.JAPANESE)
        .build();
  }
}
