package com.cauchymop.goblob;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Class to represent the visible content of the board (stones and territories).
 */
public class BoardContent extends Board {

  private final GoBoard goBoard;

  public BoardContent(GoBoard goBoard) {
    super(goBoard.getBoardSize());
    this.goBoard = goBoard;
    initBoard();
  }

  private void initBoard() {
    for (int x = 0; x < getBoardSize(); x++) {
      for (int y = 0; y < getBoardSize(); y++) {
        setColor(getPos(x, y), Color.Empty);
      }
    }
  }

  public boolean play(Color color, int x, int y) {
    if (!goBoard.play(color, x, y)) {
      return false;
    }
    updateFromGoBoard();
    updateTerritories();
    return true;
  }

  private void updateTerritories() {
    Set<Integer> exploredPositions = Sets.newHashSet();
    for (int y = 0; y < getBoardSize(); y++) {
      for (int x = 0; x < getBoardSize(); x++) {
        int pos = getPos(x, y);
        if (getColor(pos) == Color.Empty && !exploredPositions.contains(pos)) {
          Set<Integer> territory = Sets.newHashSet();
          Set<Color> neighbors = Sets.newHashSet();
          getTerritoryInfo(pos, territory, neighbors);
          if (neighbors.size() == 1) {
            Color territoryColor = getTerritoryColor(Iterables.getOnlyElement(neighbors));
            for (Integer position : territory) {
              setColor(position, territoryColor);
            }
          }
        }
      }
    }
  }

  private Color getTerritoryColor(Color color) {
    if (color == Color.White) {
      return Color.WhiteTerritory;
    } else {
      return Color.BlackTerritory;
    }
  }

  private void getTerritoryInfo(int pos, Set<Integer> territory, Set<Color> neighbors) {
    Color color = getColor(pos);
    if (color == Color.Empty && !territory.contains(pos)) {
      territory.add(pos);
      getTerritoryInfo(getNorth(pos), territory, neighbors);
      getTerritoryInfo(getSouth(pos), territory, neighbors);
      getTerritoryInfo(getEast(pos), territory, neighbors);
      getTerritoryInfo(getWest(pos), territory, neighbors);
    } else if (color == Color.Black || color == Color.White) {
      neighbors.add(color);
    }
  }

  private void updateFromGoBoard() {
    for (int x = 0 ; x < getBoardSize() ; x++) {
      for (int y = 0 ; y < getBoardSize() ; y++) {
        setColor(getPos(x, y), goBoard.getColor(x, y));
      }
    }
  }
}
