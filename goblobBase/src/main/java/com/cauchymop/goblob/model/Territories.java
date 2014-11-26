package com.cauchymop.goblob.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cauchymop.goblob.proto.PlayGameData.Position;

/**
 * Class to compute territories.
 */
public class Territories {

  private ImmutableMap<StoneColor, HashSet<Position>> territories = ImmutableMap.of(
      StoneColor.Black, Sets.<Position>newHashSet(), StoneColor.White, Sets.<Position>newHashSet());

  public Territories(GoBoard board, Collection<Position> deadStones) {
    Set<Position> unknownTerritories = getEmptyPositions(board);
    while (!unknownTerritories.isEmpty()) {
      Position position = Iterables.getFirst(unknownTerritories, null);
      Set<Position> painted = Sets.newHashSet();
      Set<StoneColor> colors = Sets.newHashSet();
      fill(board, deadStones, position, painted, colors);
      if (colors.size() == 1) {
        StoneColor color = Iterables.getFirst(colors, null);
        territories.get(color).addAll(painted);
      }
      unknownTerritories.removeAll(painted);
    }
  }

  public HashSet<Position> getTerritories(StoneColor color) {
    return territories.get(color);
  }

  private void fill(GoBoard board, Collection<Position> deadStones, Position seed,
      Set<Position> painted, Set<StoneColor> colors) {
    List<Position> seeds = Lists.newArrayList(seed);
    while (!seeds.isEmpty()) {
      Position position = seeds.remove(0);
      if (painted.contains(position)) {
        continue;
      }
      int x = position.getX();
      int y = position.getY();
      StoneColor color = board.getColor(x, y);
      if (color == null || deadStones.contains(position)) {
        painted.add(position);
        seeds.addAll(getNeighbors(board, x, y));
      } else {
        colors.add(color);
      }
    }
  }

  private Set<Position> getNeighbors(GoBoard board, int x, int y) {
    Set<Position> neighbors = Sets.newHashSet();
    if (x > 0) {
      neighbors.add(getPosition(x - 1, y));
    }
    if (x < board.getSize() - 1) {
      neighbors.add(getPosition(x + 1, y));
    }
    if (y > 0) {
      neighbors.add(getPosition(x, y-1));
    }
    if (y < board.getSize() - 1) {
      neighbors.add(getPosition(x, y + 1));
    }
    return neighbors;
  }

  private Set<Position> getEmptyPositions(GoBoard board) {
    Set<Position> emptyPositions = Sets.newHashSet();
    int boardSize = board.getSize();
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        if (board.getColor(x, y) == null) {
          Position position = getPosition(x, y);
          emptyPositions.add(position);
        }
      }
    }
    return emptyPositions;
  }

  private Position getPosition(int x, int y) {
    return Position.newBuilder().setX(x).setY(y).build();
  }
}