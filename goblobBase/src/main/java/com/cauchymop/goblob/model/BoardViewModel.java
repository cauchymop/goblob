package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

public class BoardViewModel {

  private final int boardSize;
  private PlayGameData.Color[][] stones;
  private PlayGameData.Color[][] territories;
  private int lastMoveX;
  private int lastMoveY;
  private boolean isInteractive;

  public BoardViewModel(int boardSize, PlayGameData.Color[][] stones,
      PlayGameData.Color[][] territories, int lastMoveX, int LastMoveY, boolean isInteractive) {
    this.boardSize = boardSize;
    this.stones = stones;
    this.territories = territories;
    this.lastMoveX = lastMoveX;
    this.lastMoveY = LastMoveY;
    this.isInteractive = isInteractive;
  }

  public int getBoardSize() {
    return boardSize;
  }

  public PlayGameData.Color getColor(int x, int y) {
    return stones[y][x];
  }

  public PlayGameData.Color getTerritory(int x, int y) {
    return territories[y][x];
  }

  public boolean isLastMove(int x, int y) {
    return (x == lastMoveX) && (y == lastMoveY);
  }

  public boolean isInteractive() {
    return isInteractive;
  }
}
