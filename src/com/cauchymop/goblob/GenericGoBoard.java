package com.cauchymop.goblob;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Generic {@link GoBoard} implementation that works for any size.
 */
public class GenericGoBoard implements GoBoard {

  private static final int BLACK_GROUP_START = 1;

  private int[][] neighborPositionsByPosition;
  private int size;
  private int numberOfPositions;
  private int numberOfGroups;
  private int whiteGroupStart;
  private BitSet blackField;
  private BitSet whiteField;
  private int[] groupByPosition;
  private BitSet[] stoneFieldByGroup;
  private BitSet[] libertyFieldByGroup;

  public GenericGoBoard(int size) {
    this.size = size;
    numberOfPositions = size * size;
    // Worst case, every other intersection is occupied by a stone of the same color.
    int numberOfGroupsPerColor = numberOfPositions / 2 + 1;
    // One set per color, plus zero which means empty
    numberOfGroups = 2 * numberOfGroupsPerColor + 1;
    whiteGroupStart = BLACK_GROUP_START + numberOfGroupsPerColor;
    neighborPositionsByPosition = getNeighborPositionsByPosition();
    blackField = new BitSet(numberOfPositions);
    whiteField = new BitSet(numberOfPositions);
    groupByPosition = new int[numberOfPositions];
    stoneFieldByGroup = new BitSet[numberOfGroups];
    libertyFieldByGroup = new BitSet[numberOfGroups];
    for (int index = 0 ; index < numberOfGroups ; index++) {
      stoneFieldByGroup[index] = new BitSet();
      libertyFieldByGroup[index] = new BitSet();
    }
  }

  private GenericGoBoard(Parcel in) {
    this(in.readInt());
    groupByPosition = in.createIntArray();
    populateFromGroups();
  }

  private void populateFromGroups() {
    for (int pos = 0 ; pos < numberOfPositions ; pos++) {
      int group = groupByPosition[pos];
      if (group == 0) continue;
      if (group < whiteGroupStart) {
        blackField.set(pos);
      } else {
        whiteField.set(pos);
      }
      stoneFieldByGroup[group].set(pos);
      for (int neighbor : neighborPositionsByPosition[pos]) {
        if (groupByPosition[neighbor] == 0) {
          libertyFieldByGroup[group].set(neighbor);
        }
      }
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(size);
    dest.writeIntArray(groupByPosition);
  }

  public static final Parcelable.Creator<GenericGoBoard> CREATOR = new Parcelable.Creator<GenericGoBoard>() {
    public GenericGoBoard createFromParcel(Parcel in) {
      return new GenericGoBoard(in);
    }

    public GenericGoBoard[] newArray(int size) {
      return new GenericGoBoard[size];
    }
  };

  @Override
  public void clear() {
    blackField.clear();
    whiteField.clear();
    for (int i=0 ; i< numberOfPositions; i++) {
      groupByPosition[i] = 0;
    }
    for (int i=0 ; i< numberOfGroups; i++) {
      stoneFieldByGroup[i].clear();
    }
  }

  private int[][] getNeighborPositionsByPosition() {
    int[][] neighborPositionsByPositions = new int[numberOfPositions][];
    for (int x = 0 ; x < size; x++) {
      for (int y = 0 ; y < size; y++) {
        ArrayList<Integer> neighbors = Lists.newArrayList();
        if (x>0) neighbors.add(getPos(x-1, y));
        if (y>0) neighbors.add(getPos(x, y-1));
        if (x< size -1) neighbors.add(getPos(x+1, y));
        if (y< size -1) neighbors.add(getPos(x, y+1));
        neighborPositionsByPositions[getPos(x, y)] = Ints.toArray(neighbors);
      }
    }
    return neighborPositionsByPositions;
  }


  /**
   * Plays a move.
   *
   * @return whether the move was valid and played (if it was not, this instance can't be used anymore)
   */
  @Override
  public boolean play(StoneColor color, int x, int y) {
    int move = getPos(x, y);
    if (groupByPosition[move] != 0) {
      return false;
    }

    BitSet friendField = getField(color);
    BitSet foeField = getField(color.getOpponent());
    friendField.set(move);

    int group = getAvailableGroup(color);
    groupByPosition[move] = group;
    stoneFieldByGroup[group].set(move);
    libertyFieldByGroup[group].clear();
    for (int neighbor : neighborPositionsByPosition[move]) {
      if (friendField.get(neighbor)) {
        int friendGroup = groupByPosition[neighbor];
        if (friendGroup != group) {
          stoneFieldByGroup[group].or(stoneFieldByGroup[friendGroup]);
          stoneFieldByGroup[friendGroup].clear();
          libertyFieldByGroup[group].or(libertyFieldByGroup[friendGroup]);
          for (int pos = 0 ; pos < numberOfPositions; pos++) {
            if (groupByPosition[pos] == friendGroup) {
              groupByPosition[pos] = group;
            }
          }
        }
      } else if (foeField.get(neighbor)) {
        int foeGroup = groupByPosition[neighbor];
        libertyFieldByGroup[foeGroup].clear(move);
        if (libertyFieldByGroup[foeGroup].isEmpty()) {
          capture(foeGroup);
        }
      } else {
        libertyFieldByGroup[group].set(neighbor);
      }
    }

    libertyFieldByGroup[group].clear(move);
    if (libertyFieldByGroup[group].isEmpty()) {
      return false;
    }

    return true;
  }

  private BitSet getField(StoneColor color) {
    return (color == StoneColor.Black) ? blackField : whiteField;
  }

  private int getAvailableGroup(StoneColor color) {
    int groupStart = (color == StoneColor.Black) ? BLACK_GROUP_START : whiteGroupStart;
    for (int group = groupStart ; ; group++) {
      if (stoneFieldByGroup[group].isEmpty()) return group;
    }
  }

  private void capture(int group) {
    StoneColor foeColor = getColorByGroup(group).getOpponent();
    for (int pos = 0 ; pos < numberOfPositions; pos++) {
      if (!stoneFieldByGroup[group].get(pos)) continue;

      // Remove the stone.
      whiteField.clear(pos);
      blackField.clear(pos);
      groupByPosition[pos] = 0;
      stoneFieldByGroup[group].clear(pos);

      // Create new liberties for neighbors.
      for (int neighbor : neighborPositionsByPosition[pos]) {
        int neighborGroup = groupByPosition[neighbor];
        if (getColorByGroup(neighborGroup) == foeColor) {
          libertyFieldByGroup[neighborGroup].set(pos);
        }
      }
    }
  }

  private StoneColor getColorByGroup(int group) {
    if (group == 0) return StoneColor.Empty;
    return (group < whiteGroupStart) ? StoneColor.Black : StoneColor.White;
  }

  @Override
  public double getScore(StoneColor color) {
    int score = blackField.cardinality() - whiteField.cardinality();
    return (color == StoneColor.Black) ? score : -score;
  }

  @Override
  public StoneColor getColor(int x, int y) {
    return getColorByGroup(groupByPosition[getPos(x, y)]);
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public void copyFrom(GoBoard board) {
    if (!(board instanceof GenericGoBoard)) {
      throw new UnsupportedOperationException("Can't copy non-GenericGoBoard to GenericGoBoard");
    }
    GenericGoBoard other = (GenericGoBoard) board;
    System.arraycopy(other.groupByPosition, 0, groupByPosition, 0, numberOfPositions);
    populateFromGroups();
  }

  private int getPos(int x, int y) {
    return y * size + x;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof GenericGoBoard)) {
      return false;
    }
    GenericGoBoard other = (GenericGoBoard) object;

    return blackField.equals(other.blackField) && whiteField.equals(other.whiteField);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(blackField, whiteField);
  }
}
