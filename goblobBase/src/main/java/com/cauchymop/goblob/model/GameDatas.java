package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

/**
 * Helper class to build {@link PlayGameData} related messages.
 */
public class GameDatas {

  public static PlayGameData.Move createPassMove() {
    return PlayGameData.Move.newBuilder().setType(PlayGameData.Move.MoveType.PASS).build();
  }

  public static PlayGameData.Move createMove(int x, int y) {
    return PlayGameData.Move.newBuilder()
        .setType(PlayGameData.Move.MoveType.MOVE)
        .setPosition(PlayGameData.Position.newBuilder()
            .setX(x)
            .setY(y))
        .build();
  }
}
