package com.cauchymop.goblob;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Class to get an appropriate {@link Heuristic} for a given game.
 */
public class HeuristicProvider {

  private static Map<Class, Heuristic> heuristics = ImmutableMap.<Class, Heuristic>builder()
      .put(GoGame.class, new GoHeuristic())
      .build();

  public static Heuristic getHeuristic(Game game) {
    return heuristics.get(game.getClass());
  }
}
