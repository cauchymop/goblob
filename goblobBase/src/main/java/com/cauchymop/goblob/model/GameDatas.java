package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData.GameType;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Named;

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

  @Inject
  @Named("PlayerOneDefaultName")
  String playerOneDefaultName;

  @Inject
  @Named("PlayerTwoDefaultName")
  String playerTwoDefaultName;

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

  public GameData createGameData(int size, int handicap, float komi, GameType gameType,
      GoPlayer blackPlayer,
      GoPlayer whitePlayer) {
    return createGameData(createGameConfiguration(size, handicap, komi, gameType, blackPlayer, whitePlayer),
        ImmutableList.<Move>of(), null);
  }

  public GameData createGameData(GameConfiguration gameConfiguration, Iterable<Move> moves,
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

  public GameData createGameData(GameConfiguration gameConfiguration) {
    return GameData.newBuilder()
        .setVersion(VERSION)
        .setGameConfiguration(gameConfiguration)
        .build();
  }

  public GameConfiguration createGameConfiguration(int size, int handicap, float komi,
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

  public GameConfiguration createLocalGameConfiguration(int boardSize) {
    GoPlayer black = createGamePlayer(GameDatas.PLAYER_ONE_ID, playerOneDefaultName);
    GoPlayer white = createGamePlayer(GameDatas.PLAYER_TWO_ID, playerTwoDefaultName);
    GameConfiguration localGameConfiguration = GameConfiguration.newBuilder()
        .setHandicap(DEFAULT_HANDICAP)
        .setKomi(DEFAULT_KOMI)
        .setScoreType(GameConfiguration.ScoreType.CHINESE)
        .setBoardSize(boardSize)
        .setBlack(black)
        .setWhite(white)
        .build();
    return localGameConfiguration;
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
}
