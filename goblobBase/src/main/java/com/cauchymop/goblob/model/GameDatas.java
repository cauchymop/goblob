package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.google.common.collect.ImmutableList;

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

  public static GameData createGameData(int size, int handicap, float komi, GoPlayer blackPlayer,
      GoPlayer whitePlayer) {
    return createGameData(createGameConfiguration(size, handicap, komi, blackPlayer, whitePlayer),
        ImmutableList.<Move>of(), null);
  }

  public static GameData createGameData(GameConfiguration gameConfiguration, Iterable<Move> moves,
      MatchEndStatus matchEndStatus) {
    GameData.Builder builder = GameData.newBuilder()
        .setVersion(VERSION)
        .setGameConfiguration(gameConfiguration)
        .addAllMove(moves);
    if (matchEndStatus != null) {
      builder.setMatchEndStatus(matchEndStatus);
    }
    return builder.build();
  }

  public static GameConfiguration createGameConfiguration(int size, int handicap, float komi,
      GoPlayer blackPlayer, GoPlayer whitePlayer) {
    return GameConfiguration.newBuilder()
        .setBoardSize(size)
        .setHandicap(handicap)
        .setKomi(komi)
        .setBlackId(blackPlayer.getId())
        .setWhiteId(whitePlayer.getId())
        .setBlack(blackPlayer)
        .setWhite(whitePlayer)
        .setScoreType(GameConfiguration.ScoreType.JAPANESE)
        .build();
  }

  public static GoPlayer createPlayer(PlayGameData.PlayerType type, String id, String name) {
    return GoPlayer.newBuilder()
        .setType(type)
        .setId(id)
        .setName(name)
        .build();
  }
}
