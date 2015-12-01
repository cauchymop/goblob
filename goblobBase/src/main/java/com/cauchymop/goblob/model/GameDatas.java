package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData.GameType;
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

  @Inject
  @Named("PlayerOneDefaultName")
  Lazy<String> playerOneDefaultName;

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

  public GameData createGameData(String matchId, int size, int handicap, float komi,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer) {
    return createGameData(matchId, createGameConfiguration(size, handicap, komi, gameType, blackPlayer, whitePlayer, true),
        ImmutableList.<Move>of(), null);
  }

  public GameData createGameData(String matchId, GameConfiguration gameConfiguration) {
    return createGameData(matchId, gameConfiguration, Lists.<Move>newArrayList(), null);
  }

  public GameData createGameData(String matchId, GameConfiguration gameConfiguration, Iterable<Move> moves,
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

  public GameConfiguration createLocalGameConfiguration(int boardSize) {
    GoPlayer black = createGamePlayer(GameDatas.PLAYER_ONE_ID, playerOneDefaultName.get());
    GoPlayer white = createGamePlayer(GameDatas.PLAYER_TWO_ID, playerTwoDefaultName);
    return createGameConfiguration(boardSize, DEFAULT_HANDICAP, DEFAULT_KOMI, GameType.LOCAL, black, white, true);
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
