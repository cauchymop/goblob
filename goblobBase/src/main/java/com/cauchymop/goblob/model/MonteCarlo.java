package com.cauchymop.goblob.model;

import com.google.common.collect.Lists;

import java.util.Random;
import java.util.Set;

/**
 * Implementation of Monte-Carlo Tree Search.
 */
public class MonteCarlo {

  private static final double EPSILON = 1e-6;
  private static final double KOMI = 0.5;
  public static final double PASS_MALUS = 0.9;
  public static final double EYEFILLING_MALUS = 1.9;

  private static Random random = new Random(0);

  public static int getBestMove(GoGame game, int iterations) {
    int nbPos = game.getBoardSize() * game.getBoardSize() + 1;
    // Root has the opponent color, so that the first move has the current color.
    TreeNode root = new TreeNode(nbPos, game.getCurrentColor().getOpponent());
    for(int i = 0 ; i<iterations ; i++) {
//      System.err.println("Iteration " + i);
      root.runAndRestore(game);
    }
    printScores(game, root);
    return root.selectBestNode(game).move;
  }

  private static void printScores(GoGame game, TreeNode root) {
    for (int i=0 ; i<game.getPassValue() ; i++) {
      System.err.print(String.format("%3d ", (int)root.children[i].nVisits));
      if (i%game.getBoardSize() == game.getBoardSize()-1) System.err.println("");
    }
  }

  private static double rollOut(GoGame game) {
    while(true) {
//      System.err.println(TextBoard.toString(game.getBoard()));
      if (game.isGameEnd()) {
//        System.err.print(TextBoard.toString(game.getBoard()));
        double gameValue = getGameValue(game);
//        System.err.println(String.format("Value = %f\n", gameValue));
        return gameValue;
      }
      Set<Integer> possibleMoves = game.getNonEyeFillingMoves();
//      System.err.println("Moves: " + possibleMoves);
      while(true) {
        if (possibleMoves.isEmpty()) {
          game.play(game.getPassValue());
          break;
        }
        int move = Lists.newArrayList(possibleMoves).get(random.nextInt(possibleMoves.size()));
        if (game.play(move)) {
//          System.err.println("Rollout played " + move);
          break;
        }
        possibleMoves.remove(move);
      }
    }
  }

  private static double getGameValue(GoGame game) {
    double score = game.getBoard().getScore() - KOMI;
    return score > 0 ? 1 : -1;
  }

  private static class TreeNode {
    private TreeNode[] children;
    private final StoneColor stoneColor;
    private int move;
    private final int nbPos;
    private double nVisits;
    private double totValue;
    private boolean isInvalid;

    private TreeNode(int nbPos, StoneColor stoneColor) {
      this.nbPos = nbPos;
      this.stoneColor = stoneColor;
    }

    private boolean alreadyVisited() {
      return nVisits > 0;
    }

    private void updateStats(double value) {
      nVisits++;
      totValue += stoneColor == StoneColor.Black ? value : -value;
    }

    private boolean isLeaf() {
      return children == null;
    }

    private void expand() {
      children = new TreeNode[nbPos];
      for (int i=0; i< nbPos; i++) {
        children[i] = new TreeNode(nbPos, stoneColor.getOpponent());
        children[i].move = i;
      }
    }

    private TreeNode selectBestNode(GoGame game) {
      TreeNode selected = null;
      double bestValue = -Double.MAX_VALUE;
      for (TreeNode node : children) {
        if (node.isInvalid) {
          continue;
        }
        double uctValue = random.nextDouble() * EPSILON;
        if (game.getMoveHistory().size() == 0 && node.nVisits < 4) {
          uctValue += 100;
        }
        if (node.nVisits == 1) {
          uctValue += 100;
        }
        if (node.nVisits > 0) {
          uctValue += node.totValue / node.nVisits
              + Math.sqrt(Math.log(nVisits+1) / node.nVisits);
        }
        if (isPass(node.move) && !isPass(game.getLastMove())) {
          uctValue -= PASS_MALUS;
        }
        if (!isPass(node.move) && game.getBoard().isEyeFilling(node.move, game.getCurrentColor())) {
          uctValue -= EYEFILLING_MALUS;
        }
        if (uctValue > bestValue) {
          selected = node;
          bestValue = uctValue;
        }
      }
      return selected;
    }

    private boolean isPass(int move) {
      return move == nbPos - 1;
    }

    public double run(GoGame game) {
      double value;

      if(game.isGameEnd()) {
        return rollOut(game);
      }

      TreeNode node = selectAndPlay(game);
//      System.err.println("Run played " + node.move);
      if (node.alreadyVisited()) {
        value = node.run(game);
      } else {
        value = rollOut(game);
      }
      node.updateStats(value);

      return value;
    }

    private TreeNode selectAndPlay(GoGame game) {
      if (isLeaf()) {
        expand();
      }
      TreeNode node;
      while(true) {
        node = selectBestNode(game);
        if(game.play(node.move)) {
          return node;
        } else {
          node.isInvalid = true;
        }
      }
    }

    public void runAndRestore(GoGame game) {
      int originalGamePosition = game.getMoveHistory().size();
      run(game);
      while(game.getMoveHistory().size() > originalGamePosition) {
        game.undo();
      }
    }
  }
}
