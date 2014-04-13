package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A serializer for the moves of a {@link GoGame}.
 */
public class GameMoveSerializer {

  public byte[] getDataFromGame(GoGame game) {
    PlayGameData.GameData gameData = PlayGameData.GameData.newBuilder()
        .addAllMove(game.getMoveHistory())
        .build();
    return gameData.toByteArray();
  }

  public GoGame getGameFromData(byte[] bytes, int boardSize) {
    GoGame goGame = new GoGame(boardSize);
    if (bytes == null || bytes.length == 0) {
      return goGame;
    }
    PlayGameData.GameData gameData = null;
    try {
      gameData = PlayGameData.GameData.parseFrom(bytes);
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
    for (int move : gameData.getMoveList()) {
      goGame.play(move);
    }
    return goGame;
  }
}
