package com.cauchymop.goblob.model;

/**
 * Class to find the best move for a given game.
 */
public class AlphaBeta implements AI {

  private static final double MAX = 100.0;

  private double[] scores;

  public int getBestMove(Game originalGame, int depth) {
    Game localGame = originalGame.copy();
    int posCount = localGame.getPosCount();
    scores = new double[posCount];
    double bestResult = -MAX;
    int bestMove = -1;
    for (int move = 0; move < posCount; move++) {
      if (localGame.play(null, move)) {
        scores[move] = -getAlphaBeta(localGame, depth, -MAX, MAX);
        if (scores[move] > bestResult) {
          bestResult = scores[move];
          bestMove = move;
        }
        localGame.undo();
      } else {
        scores[move] = Double.NaN;
      }
    }
    return bestMove;
  }

  public double getAlphaBeta(Game game, int depth, double min, double max) {
    double score = game.getScore();
    if (depth == 0 || game.isGameEnd() || Math.abs(score) >= 8) {
      return game.getScore();
    }
    double bestScore = min;

    for (int pos = 0; pos < game.getPosCount(); pos++) {
      if (game.play(null, pos)) {
        score = -getAlphaBeta(game, depth - 1, -max, -bestScore);
        bestScore = Math.max(score, bestScore);
        game.undo();
        if (bestScore >= max) {
          break;
        }
      }
    }

    return bestScore;
  }

  public double[] getScores() {
    return scores;
  }
}
