package com.cauchymop.goblob.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.cauchymop.goblob.proto.PlayGameData.Position;
import static com.cauchymop.goblob.proto.PlayGameData.Score;

/**
 * Class to compute territories.
 */
public class ScoreGenerator {

  private ImmutableMap<Color, HashSet<Position>> territories = ImmutableMap.of(
      Color.BLACK, Sets.<Position>newHashSet(), Color.WHITE, Sets.<Position>newHashSet());
  private float komi;
  private int blackStoneCount;
  private int whiteStoneCount;

  public ScoreGenerator(GoBoard board, Set<Position> deadStones, float komi) {
    this.komi = komi;
    findTerritories(board, deadStones);
    countLiveStones(board, deadStones);
  }

  private void countLiveStones(GoBoard board, Set<Position> deadStones) {
    for (int x=0 ; x<board.getSize() ; x++) {
      for (int y=0 ; y<board.getSize() ; y++) {
        Position position = Position.newBuilder().setX(x).setY(y).build();
        if (deadStones.contains(position)) {
          continue;
        }
        if (board.getColor(x, y) == Color.BLACK) {
          blackStoneCount++;
        }
        if (board.getColor(x, y) == Color.WHITE) {
          whiteStoneCount++;
        }
      }
    }
  }

  private void findTerritories(GoBoard board, Set<Position> deadStones) {
    Set<Position> unknownTerritories = getEmptyPositions(board);
    while (!unknownTerritories.isEmpty()) {
      Position position = Iterables.getFirst(unknownTerritories, null);
      Set<Position> painted = Sets.newHashSet();
      Set<Color> colors = Sets.newHashSet();
      fill(board, deadStones, position, painted, colors);
      if (colors.size() == 1) {
        Color color = Iterables.getFirst(colors, null);
        territories.get(color).addAll(painted);
      }
      unknownTerritories.removeAll(painted);
    }
  }

  public Score getScore() {
    float blackScore = territories.get(Color.BLACK).size() + blackStoneCount;
    float whiteScore = territories.get(Color.WHITE).size() + whiteStoneCount + komi;
    return Score.newBuilder()
        .addAllBlackTerritory(territories.get(Color.BLACK))
        .addAllWhiteTerritory(territories.get(Color.WHITE))
        .setWinner(blackScore > whiteScore ? Color.BLACK : Color.WHITE)
        .setWonBy(Math.abs(blackScore - whiteScore))
        .build();
  }

  private void fill(GoBoard board, Set<Position> deadStones, Position seed,
      Set<Position> painted, Set<Color> colors) {
    List<Position> seeds = Lists.newArrayList(seed);
    while (!seeds.isEmpty()) {
      Position position = seeds.remove(0);
      if (painted.contains(position)) {
        continue;
      }
      int x = position.getX();
      int y = position.getY();
      Color color = board.getColor(x, y);
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
