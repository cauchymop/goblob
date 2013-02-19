package com.cauchymop.goblob;

import java.util.HashSet;

/**
 * Class to represent the state of a Go board, and the rules of the game to play moves.
 */
public class GoBoard extends Board {

  public GoBoard(int boardSize) {
    super(boardSize);
  }

  /**
   * Plays a move.
   *
   * @param color the {@link Color} being played
   * @param x the x coordinate for the move (0 to {@code boardSizeInCells}-1)
   * @param y the y coordinate for the move (0 to {@code boardSizeInCells}-1)
   * @return whether the move was valid and played
   */
  public boolean play(Color color, int x, int y) {
    int pos = getPos(x, y);
    if (getColor(pos) != Color.Empty) {
      return false;
    }
    setColor(pos, color);
    captureNeighbors(pos);
    if (getLiberties(pos).isEmpty()) {
      setColor(pos, Color.Empty);
      return false;
    }
    return true;
  }

  private Color getOpponent(Color color) {
    if (color == Color.Black) {
      return Color.White;
    }
    if (color == Color.White) {
      return Color.Black;
    }
    throw new RuntimeException("Invalid color " + color);
  }

  private void captureNeighbors(int pos) {
    Color opponent = getOpponent(getColor(pos));
    HashSet<Integer> captured = new HashSet<Integer>();
    findCapturedNeighbors(getNorth(pos), opponent, captured);
    findCapturedNeighbors(getSouth(pos), opponent, captured);
    findCapturedNeighbors(getEast(pos), opponent, captured);
    findCapturedNeighbors(getWest(pos), opponent, captured);
    for (Integer capturedPosition : captured) {
      setColor(capturedPosition, Color.Empty);
    }
  }

  private void findCapturedNeighbors(int pos, Color opponent, HashSet<Integer> captured) {
    if (getColor(pos) != opponent) {
      return;
    }
    HashSet<Integer> liberties = new HashSet<Integer>();
    HashSet<Integer> stones = new HashSet<Integer>();
    getGroupInfo(getColor(pos), pos, stones, liberties);
    if (liberties.isEmpty()) {
      captured.addAll(stones);
    }
  }

  private HashSet<Integer> getLiberties(int pos) {
    HashSet<Integer> liberties = new HashSet<Integer>();
    HashSet<Integer> stones = new HashSet<Integer>();
    getGroupInfo(getColor(pos), pos, stones, liberties);
    return liberties;
  }

  private void getGroupInfo(Color color, int pos, HashSet<Integer> stones, HashSet<Integer> liberties) {
    if (stones.contains(pos) || liberties.contains(pos)) {
      return;
    }
    if (getColor(pos) == color) {
      stones.add(pos);
      getGroupInfo(color, getNorth(pos), stones, liberties);
      getGroupInfo(color, getSouth(pos), stones, liberties);
      getGroupInfo(color, getWest(pos), stones, liberties);
      getGroupInfo(color, getEast(pos), stones, liberties);
    } else if (getColor(pos) == Color.Empty) {
      liberties.add(pos);
    }
  }
}
