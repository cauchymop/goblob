package com.cauchymop.goblob.model;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;

/**
 * A serializer for the moves of a {@link GoGame}.
 */
public class GameMoveSerializer {

  protected static final String TOKEN_SEPARATOR = " ";

  public byte[] serialize(GoGame game) {
    return Joiner.on(TOKEN_SEPARATOR).join(game.getMoveHistory()).getBytes();
  }

  public void deserializeTo(byte[] bytes, GoGame game) {
    if (bytes == null || bytes.length == 0) {
      return;
    }
    List<Integer> importedMoves = Lists.transform(
        ImmutableList.copyOf(new String(bytes).split(TOKEN_SEPARATOR)), Ints.stringConverter());
    List<Integer> existingMoves = game.getMoveHistory();
    int commonMoveLength = getCommonListLength(importedMoves, existingMoves);
    int undoCount = existingMoves.size() - commonMoveLength;
    undo(game, undoCount);
    play(game, importedMoves.subList(commonMoveLength, importedMoves.size()));
  }

  private void play(GoGame game, List<Integer> movesToPlay) {
    for (Integer move : movesToPlay) {
      game.play(move);
    }
  }

  private void undo(GoGame game, int undoCount) {
    for (int undoIndex = 0; undoIndex < undoCount; undoIndex++) {
      game.undo();
    }
  }

  private int getCommonListLength(List<Integer> importedMoves, List<Integer> existingMoves) {
    int minSize = Math.min(importedMoves.size(), existingMoves.size());
    for (int i = 0; i < minSize; i++) {
      if (!importedMoves.get(i).equals(existingMoves.get(i))) {
        return i;
      }
    }
    return minSize;
  }
}
