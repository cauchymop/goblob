package com.cauchymop.goblob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.os.Bundle;

import com.google.common.collect.Lists;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game
 * to play moves.
 */
public class GoGame {

  public static final String EXTRA_CURRENT_BOARD = "CURRENT_BOARD";
  public static final String EXTRA_CURRENT_COLOR_NAME = "CURRENT_COLOR_NAME";
  public static final String EXTRA_BOARD_HISTORY = "BOARD_HISTORY";
  public static final int DEFAULT_SIZE = 5;

  private List<Board> history = Lists.newArrayList();
  private Board board;
  private StoneColor currentColor;

  public GoGame(int boardSize) {
    board = new Board(boardSize);
    this.currentColor = StoneColor.Black;
  }

  public GoGame(Bundle savedGame) {
    if (savedGame != null) {

      // Get Current Board
      String currentTextBoard = savedGame.getString(EXTRA_CURRENT_BOARD);
      if (currentTextBoard != null) {
        try {
          this.board = TextBoard.fromString(currentTextBoard);
        } catch (InvalidTextBoardException e) {
          e.printStackTrace();
        }
      }

      // Get Current Color
      String currentColorName = savedGame.getString(EXTRA_CURRENT_COLOR_NAME);
      this.currentColor = StoneColor.valueOf(currentColorName);

      ArrayList<String> textHistory = savedGame.getStringArrayList(EXTRA_BOARD_HISTORY);
      if (textHistory != null) {
        try {
          Iterator<String> it = textHistory.iterator();
          while (it.hasNext()) {
            String textBoard = (String) it.next();
            history.add(TextBoard.fromString(textBoard));
          }
        } catch (InvalidTextBoardException e) {
          e.printStackTrace();
          // If one board is invalid, we clear the full history
          history.clear();
        }
      }
    }

    // Falls back to an empty board with default size in case the savedGame
    // is null or does not contains enough information
    if (this.board == null || this.currentColor == null) {
      this.board = new Board(DEFAULT_SIZE);
      this.currentColor = StoneColor.Black;
    }
  }

  public Board getBoard() {
    return board;
  }

  /**
   * Plays a move.
   * 
   * @param x
   *          the x coordinate for the move (0 to {@code boardSizeInCells}-1)
   * @param y
   *          the y coordinate for the move (0 to {@code boardSizeInCells}-1)
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

  private void getGroupInfo(StoneColor color, int pos, HashSet<Integer> stones,
      HashSet<Integer> liberties) {
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

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(EXTRA_CURRENT_BOARD, TextBoard.toString(board));
    bundle.putString(EXTRA_CURRENT_COLOR_NAME, currentColor.name());
    ArrayList<String> boards = new ArrayList<String>();
    Iterator<Board> iterator = history.iterator();
    while (iterator.hasNext()) {
      Board b = (Board) iterator.next();
      boards.add(TextBoard.toString(b));
    }
    bundle.putStringArrayList(EXTRA_BOARD_HISTORY, boards);
    return bundle;
  }
}
