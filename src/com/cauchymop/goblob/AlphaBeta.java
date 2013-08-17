package com.cauchymop.goblob;

/**
 * Class to find the best move for a given game.
 */
public class AlphaBeta implements AI {

  private static final double MAX = 100.0;
  private static final int HEURISTICS_THRESHOLD = 4;

  public int getBestMove(Game originalGame, Heuristic heuristic, int depth) {
    Game localGame = originalGame.copy();
    int posCount = localGame.getPosCount();
    double bestResult = -MAX;
    int bestMove = -1;
    for (int move = 0 ; move < posCount; move++) {
      if (localGame.play(move)) {
        double result = -getAlphaBeta(localGame, heuristic, depth, -MAX, MAX);
        if (result > bestResult) {
          bestResult = result;
          bestMove = move;
        }
        localGame.undo();
      }
    }
    return bestMove;
  }

  public static double getAlphaBeta(Game game, Heuristic heuristic, int depth, double min,
      double max) {
    double score = game.getScore();
    if (depth == 0 || game.isGameEnd() || Math.abs(score) >= 8) {
      return game.getScore();
    }
    double bestScore = min;

    for (int pos = 0 ; pos < game.getPosCount() ; pos++) {
      // TODO: store pos and its heuristics
    }
    // TODO: sort positions by heuristics

    // loop over ordered positions
    for (int pos = 0 ; pos < game.getPosCount() ; pos++) {
      if (game.play(pos)) {
        score = -getAlphaBeta(game, heuristic, depth - 1, -max, -bestScore);
        bestScore = Math.max(score, bestScore);
        game.undo();
        if (bestScore >= max) {
          break;
        }
      }
    }
    double reference = 0;

    // store results
    for (int pos = 0 ; pos < game.getPosCount() ; pos++) {
      if (depth > HEURISTICS_THRESHOLD) {
        heuristic.record(game, depth, reference, pos, score);
      }
    }

    return bestScore;
  }

  public static double[] getMoveValues(Game game, Heuristic heuristic, int depth) {
    int posCount = game.getPosCount();
    double[] result = new double[posCount];
    for (int pos = 0 ; pos < posCount; pos++) {
      if (game.play(pos)) {
        result[pos] = -getAlphaBeta(game, heuristic, depth, -MAX, MAX);
        game.undo();
      } else {
        result[pos] = Double.NaN;
      }
    }
    return result;
  }
}
