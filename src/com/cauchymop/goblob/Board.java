package com.cauchymop.goblob;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Class to represent a board with colors on it.
 */
public class Board {

  private StoneColor board[];
  private int boardSize;

  public Board(int boardSize) {
    this.boardSize = boardSize;
    board = new StoneColor[(boardSize + 2) * (boardSize + 2)];
    initEmptyCells();
    initBorderCells();
  }

  public Board(Board other) {
    boardSize = other.boardSize;
    board = new StoneColor[(boardSize + 2) * (boardSize + 2)];
    System.arraycopy(other.board, 0, board, 0, board.length);
  }

  private void initEmptyCells() {
    for (int x = 0 ; x < getBoardSize() ; x++) {
      for (int y = 0 ; y < getBoardSize() ; y++) {
        setColor(x, y, StoneColor.Empty);
      }
    }
  }

  private void initBorderCells() {
    for (int col = -1 ; col < getBoardSize() + 1 ; col++) {
      setColor(-1, col, StoneColor.Border);
      setColor(col, -1, StoneColor.Border);
      setColor(getBoardSize(), col, StoneColor.Border);
      setColor(col, getBoardSize(), StoneColor.Border);
    }
  }

  protected int getBoardSize() {
    return boardSize;
  }

  protected int getPos(int x, int y) {
    return (y + 1) * (boardSize + 2) + (x + 1);
  }

  public StoneColor getColor(int position) {
    return board[position];
  }

  public StoneColor getColor(int x, int y) {
    return board[getPos(x, y)];
  }

  public StoneColor setColor(int position, StoneColor color) {
    return board[position] = color;
  }

  public StoneColor setColor(int x, int y, StoneColor color) {
    return board[getPos(x, y)] = color;
  }

  public int getNorth(int pos) {
    return pos - (boardSize + 2);
  }

  public int getSouth(int pos) {
    return pos + (boardSize + 2);
  }

  public int getWest(int pos) {
    return pos - 1;
  }

  public int getEast(int pos) {
    return pos + 1;
  }

  public void updateTerritories() {
    Set<Integer> exploredPositions = Sets.newHashSet();
    for (int y = 0; y < boardSize; y++) {
      for (int x = 0; x < boardSize; x++) {
        int pos = getPos(x, y);
        if (getColor(pos) == StoneColor.Empty && !exploredPositions.contains(pos)) {
          Set<Integer> territory = Sets.newHashSet();
          Set<StoneColor> neighbors = Sets.newHashSet();
          getTerritoryInfo(pos, territory, neighbors);
          if (neighbors.size() == 1) {
            StoneColor territoryColor = getTerritoryColor(Iterables.getOnlyElement(neighbors));
            for (Integer position : territory) {
              setColor(position, territoryColor);
            }
          }
        }
      }
    }
  }

  public void clearTerritories() {
    for (int y = 0; y < boardSize; y++) {
      for (int x = 0; x < boardSize; x++) {
        StoneColor color = getColor(x, y);
        if (color == StoneColor.WhiteTerritory || color == StoneColor.BlackTerritory) {
          setColor(x, y, StoneColor.Empty);
        }
      }
    }
  }

  private StoneColor getTerritoryColor(StoneColor color) {
    if (color == StoneColor.White) {
      return StoneColor.WhiteTerritory;
    } else {
      return StoneColor.BlackTerritory;
    }
  }

  private void getTerritoryInfo(int pos, Set<Integer> territory, Set<StoneColor> neighbors) {
    StoneColor color = getColor(pos);
    if (color == StoneColor.Empty && !territory.contains(pos)) {
      territory.add(pos);
      getTerritoryInfo(getNorth(pos), territory, neighbors);
      getTerritoryInfo(getSouth(pos), territory, neighbors);
      getTerritoryInfo(getEast(pos), territory, neighbors);
      getTerritoryInfo(getWest(pos), territory, neighbors);
    } else if (color == StoneColor.Black || color == StoneColor.White) {
      neighbors.add(color);
    }
  }
}
