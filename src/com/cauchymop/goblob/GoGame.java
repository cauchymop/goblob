package com.cauchymop.goblob;

import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame {

  private List<Board> history = Lists.newArrayList();
  private Board board;
  private StoneColor currentColor;

  public GoGame(int boardSize) {
    board = new Board(boardSize);
    this.currentColor = StoneColor.Black;
  }

  public Board getBoard() {
    return board;
  }

  /**
   * Plays a move.
   *
   * @param x the x coordinate for the move (0 to {@code boardSizeInCells}-1)
   * @param y the y coordinate for the move (0 to {@code boardSizeInCells}-1)
   * @return whether the move was valid and played
   */
  public boolean play(int x, int y) {
    int pos = board.getPos(x, y);
    if (board.getColor(pos) != StoneColor.Empty) {
      return false;
    }
    board.setColor(pos, currentColor);
    captureNeighbors(pos);
    if (getLiberties(pos).isEmpty()) {
      board.setColor(pos, StoneColor.Empty);
      return false;
    }
    history.add(board);
    board = new Board(board);
    currentColor = currentColor.getOpponent();
    return true;
  }

  private void captureNeighbors(int pos) {
    StoneColor opponent = currentColor.getOpponent();
    HashSet<Integer> captured = new HashSet<Integer>();
    findCapturedNeighbors(board.getNorth(pos), opponent, captured);
    findCapturedNeighbors(board.getSouth(pos), opponent, captured);
    findCapturedNeighbors(board.getEast(pos), opponent, captured);
    findCapturedNeighbors(board.getWest(pos), opponent, captured);
    for (Integer capturedPosition : captured) {
      board.setColor(capturedPosition, StoneColor.Empty);
    }
  }

  private void findCapturedNeighbors(int pos, StoneColor opponent, HashSet<Integer> captured) {
    if (board.getColor(pos) != opponent) {
      return;
    }
    HashSet<Integer> liberties = new HashSet<Integer>();
    HashSet<Integer> stones = new HashSet<Integer>();
    getGroupInfo(board.getColor(pos), pos, stones, liberties);
    if (liberties.isEmpty()) {
      captured.addAll(stones);
    }
  }

  private HashSet<Integer> getLiberties(int pos) {
    HashSet<Integer> liberties = new HashSet<Integer>();
    HashSet<Integer> stones = new HashSet<Integer>();
    getGroupInfo(board.getColor(pos), pos, stones, liberties);
    return liberties;
  }

  private void getGroupInfo(StoneColor color, int pos, HashSet<Integer> stones, HashSet<Integer> liberties) {
    if (stones.contains(pos) || liberties.contains(pos)) {
      return;
    }
    if (board.getColor(pos) == color) {
      stones.add(pos);
      getGroupInfo(color, board.getNorth(pos), stones, liberties);
      getGroupInfo(color, board.getSouth(pos), stones, liberties);
      getGroupInfo(color, board.getWest(pos), stones, liberties);
      getGroupInfo(color, board.getEast(pos), stones, liberties);
    } else if (board.getColor(pos) == StoneColor.Empty) {
      liberties.add(pos);
    }
  }
}
