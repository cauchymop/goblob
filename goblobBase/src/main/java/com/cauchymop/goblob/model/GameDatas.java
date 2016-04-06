package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.GameDataOrBuilder;
import com.cauchymop.goblob.proto.PlayGameData.GameType;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData.MatchEndStatus;
import com.cauchymop.goblob.proto.PlayGameData.Position;
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
  public static final int VERSION = 2;
  public static final String PLAYER_ONE_ID = "player1";
  public static final String PLAYER_TWO_ID = "player2";
  public static final String LOCAL_MATCH_ID = "local";
  public static final String NEW_GAME_MATCH_ID = "new game";

  private final Lazy<String> playerOneDefaultName;
  private final String playerTwoDefaultName;
  private final Lazy<String> localGoogleIdentity;

  @Inject
  public GameDatas(@Named("PlayerOneDefaultName") Lazy<String> playerOneDefaultName,
      @Named("PlayerTwoDefaultName") String playerTwoDefaultName,
      @Named("LocalGoogleIdentity") Lazy<String> localGoogleIdentity) {
    this.playerOneDefaultName = playerOneDefaultName;
    this.playerTwoDefaultName = playerTwoDefaultName;
    this.localGoogleIdentity = localGoogleIdentity;
  }

  public PlayGameData.Color getCurrentColor(GameDataOrBuilder gameData) {
    return gameData.getTurn();
  }

  public boolean isLocalPlayer(GameDataOrBuilder gameData, GoPlayer player) {
    return isLocalGame(gameData) || player.getGoogleId().equals(localGoogleIdentity.get());
  }

  public boolean isLocalTurn(GameDataOrBuilder gameData) {
    return isLocalPlayer(gameData, getCurrentPlayer(gameData)) && !(gameData.getPhase() == Phase.FINISHED);
  }

  public PlayGameData.Color getOpponentColor(GoPlayer black, GoPlayer white) {
    String localGoogleId = localGoogleIdentity.get();
    if (black.getGoogleId().equals(localGoogleId)) {
      return PlayGameData.Color.WHITE;
    } else if (white.getGoogleId().equals(localGoogleId)) {
      return PlayGameData.Color.BLACK;
    }
    throw new RuntimeException("Opponent is neither black or white, maybe this is a Connect4 Game...");
  }

  public GoPlayer getCurrentPlayer(GameDataOrBuilder gameData) {
    return getGoPlayer(gameData, getCurrentColor(gameData));
  }

  public GoPlayer getGoPlayer(GameDataOrBuilder gameData, PlayGameData.Color color) {
    return color == PlayGameData.Color.BLACK ? getBlackPlayer(gameData) : getWhitePlayer(gameData);
  }

  public GoPlayer getWinner(GameDataOrBuilder gameData) {
    return getGoPlayer(gameData, gameData.getMatchEndStatus().getScore().getWinner());
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

  public GameData createGameData(String matchId, Phase phase, int size, int handicap, float komi,
      GameType gameType, GoPlayer blackPlayer, GoPlayer whitePlayer) {
    return createGameData(matchId, phase, null, createGameConfiguration(size, handicap, komi, gameType, blackPlayer, whitePlayer),
        ImmutableList.<Move>of(), null);
  }

  public GameData createGameData(String matchId, Phase phase, GameConfiguration gameConfiguration) {
    return createGameData(matchId, phase, null, gameConfiguration);
  }

  public GameData createGameData(String matchId, Phase phase, PlayGameData.Color turn,
      GameConfiguration gameConfiguration) {
    return createGameData(matchId, phase, turn, gameConfiguration, Lists.<Move>newArrayList(), null);
  }

  public GameData createGameData(String matchId, Phase phase, PlayGameData.Color turn, GameConfiguration gameConfiguration,
      Iterable<Move> moves,
      MatchEndStatus matchEndStatus) {
    GameData.Builder builder = GameData.newBuilder()
        .setVersion(VERSION)
        .setMatchId(matchId)
        .setPhase(phase)
        .setGameConfiguration(gameConfiguration)
        .addAllMove(moves);

    if (turn != null) {
      builder.setTurn(turn);
    }

    if (matchEndStatus != null) {
      builder.setMatchEndStatus(matchEndStatus);
    }
    return builder.build();
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
    return createGameData(LOCAL_MATCH_ID, Phase.INITIAL, createGameConfiguration(boardSize, DEFAULT_HANDICAP, DEFAULT_KOMI, GameType.LOCAL, black, white));
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

}
