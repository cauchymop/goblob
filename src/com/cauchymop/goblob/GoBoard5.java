package com.cauchymop.goblob;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.ArrayList;

/**
 * {@link GoBoard} implementation optimized for 5x5.
 */
public class GoBoard5 implements GoBoard {

  private static final int BOARD_SIZE = 5;
  private static final int NUMBER_OF_GROUPS = 27;  // 1 empty, 13 black groups, 13 white groups.
  private static final int NUMBER_OF_POSITIONS = BOARD_SIZE * BOARD_SIZE;
  private static final int BLACK_GROUP_START = 1;
  private static final int WHITE_GROUP_START = 14;

  private static final int[][] neighborPositionsByPosition = getNeighborPositionsByPosition();

  private int blackField;
  private int whiteField;
  private int[] groupByPosition = new int[NUMBER_OF_POSITIONS];
  private int[] stoneFieldByGroup = new int[NUMBER_OF_GROUPS];
  private int[] libertyFieldByGroup = new int[NUMBER_OF_GROUPS];

  public GoBoard5() {
  }

  private GoBoard5(Parcel in) {
    blackField = in.readInt();
    whiteField = in.readInt();
    groupByPosition = in.createIntArray();
    stoneFieldByGroup = in.createIntArray();
    libertyFieldByGroup = in.createIntArray();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(blackField);
    dest.writeInt(whiteField);
    dest.writeIntArray(groupByPosition);
    dest.writeIntArray(stoneFieldByGroup);
    dest.writeIntArray(libertyFieldByGroup);
  }

  public static final Parcelable.Creator<GoBoard5> CREATOR = new Parcelable.Creator<GoBoard5>() {
    public GoBoard5 createFromParcel(Parcel in) {
      return new GoBoard5(in);
    }

    public GoBoard5[] newArray(int size) {
      return new GoBoard5[size];
    }
  };

  @Override
  public void clear() {
    blackField = 0;
    whiteField = 0;
    for (int i=0 ; i<NUMBER_OF_POSITIONS ; i++) {
      groupByPosition[i] = 0;
    }
    for (int i=0 ; i<NUMBER_OF_GROUPS ; i++) {
      stoneFieldByGroup[i] = 0;
    }
  }

  private static int[][] getNeighborPositionsByPosition() {
    int[][] neighborPositionsByPositions = new int[NUMBER_OF_POSITIONS][];
    for (int x = 0 ; x < BOARD_SIZE ; x++) {
      for (int y = 0 ; y < BOARD_SIZE ; y++) {
        ArrayList<Integer> neighbors = Lists.newArrayList();
        if (x>0) neighbors.add(getPos(x-1, y));
        if (y>0) neighbors.add(getPos(x, y-1));
        if (x<BOARD_SIZE-1) neighbors.add(getPos(x+1, y));
        if (y<BOARD_SIZE-1) neighbors.add(getPos(x, y+1));
        neighborPositionsByPositions[getPos(x, y)] = Ints.toArray(neighbors);
      }
    }

    return neighborPositionsByPositions;
  }


  /**
   * Plays a move.
   *
   * @return whether the move was valid (if it was not, this instance can't be used anymore)
   */
  @Override
  public boolean play(StoneColor color, int x, int y) {
    int move = getPos(x, y);
    if (groupByPosition[move] != 0) {
      return false;
    }
    int moveField = 1 << move;

    if (color == StoneColor.Black) {
      blackField |= moveField;
    } else {
      whiteField |= moveField;
    }

    int group = getAvailableGroup(color);
    groupByPosition[move] = group;
    stoneFieldByGroup[group] = moveField;
    libertyFieldByGroup[group] = 0;
    for (int neighborPosition : neighborPositionsByPosition[move]) {
      int neighborField = 1 << neighborPosition;
      if ((getFriendField(color) & neighborField) != 0) {
        int friendGroup = groupByPosition[neighborPosition];
        if (friendGroup != group) {
          stoneFieldByGroup[group] |= stoneFieldByGroup[friendGroup];
          stoneFieldByGroup[friendGroup] = 0;
          libertyFieldByGroup[group] |= libertyFieldByGroup[friendGroup];
          for (int pos = 0 ; pos < NUMBER_OF_POSITIONS ; pos++) {
            if (groupByPosition[pos] == friendGroup) {
              groupByPosition[pos] = group;
            }
          }
        }
      } else if ((getFoeField(color) & neighborField) != 0) {
        int foeGroup = groupByPosition[neighborPosition];
        libertyFieldByGroup[foeGroup] &= ~(moveField);
        if (libertyFieldByGroup[foeGroup] == 0) {
          capture(foeGroup);
        }
      } else {
        libertyFieldByGroup[group] |= neighborField;
      }
    }

    libertyFieldByGroup[group] &= ~moveField;
    if (libertyFieldByGroup[group] == 0) {
      return false;
    }

    return true;
  }

  private int getFriendField(StoneColor color) {
    return (color == StoneColor.Black) ? blackField : whiteField;
  }

  private int getFoeField(StoneColor color) {
    return (color == StoneColor.Black) ? whiteField : blackField;
  }

  private int getAvailableGroup(StoneColor color) {
    int groupStart = (color == StoneColor.Black) ? BLACK_GROUP_START : WHITE_GROUP_START;
    for (int group = groupStart ; ; group++) {
      if (stoneFieldByGroup[group] == 0) return group;
    }
  }

  private void capture(int group) {
    int groupStoneField = stoneFieldByGroup[group];
    StoneColor foeColor = getColorByGroup(group).getOpponent();
    stoneFieldByGroup[group] = 0;
    for (int pos = 0, posField = 1 ; pos < NUMBER_OF_POSITIONS ; pos++, posField<<=1) {
      if ((posField & groupStoneField) == 0) continue;

      // Remove the stone.
      whiteField &= ~(posField);
      blackField &= ~(posField);
      groupByPosition[pos] = 0;

      // Create new liberties for neighbors.
      for (int neighborPosition : neighborPositionsByPosition[pos]) {
        int neighborGroup = groupByPosition[neighborPosition];
        if (getColorByGroup(neighborGroup) == foeColor) {
          libertyFieldByGroup[neighborGroup] |= posField;
        }
      }
    }
  }

  private StoneColor getColorByGroup(int group) {
    if (group == 0) return StoneColor.Empty;
    return (group < WHITE_GROUP_START) ? StoneColor.Black : StoneColor.White;
  }

  @Override
  public double getScore(StoneColor color) {
    int score = Integer.bitCount(blackField) - Integer.bitCount(whiteField);
    return (color == StoneColor.Black) ? score : -score;
  }

  @Override
  public StoneColor getColor(int x, int y) {
    return getColorByGroup(groupByPosition[getPos(x, y)]);
  }

  @Override
  public int getSize() {
    return BOARD_SIZE;
  }

  @Override
  public void copyFrom(GoBoard board) {
    if (!(board instanceof GoBoard5)) {
      throw new UnsupportedOperationException("Can't copy non-GoBoard5 to GoBoard5");
    }
    GoBoard5 other = (GoBoard5) board;
    blackField = other.blackField;
    whiteField = other.whiteField;
    System.arraycopy(other.groupByPosition, 0, groupByPosition, 0, NUMBER_OF_POSITIONS);
    System.arraycopy(other.stoneFieldByGroup, 0, stoneFieldByGroup, 0, NUMBER_OF_GROUPS);
    System.arraycopy(other.libertyFieldByGroup, 0, libertyFieldByGroup, 0, NUMBER_OF_GROUPS);
  }

  private static int getPos(int x, int y) {
    return y * BOARD_SIZE + x;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof GoBoard5)) {
      return false;
    }
    GoBoard5 other = (GoBoard5) object;

    return this.blackField == other.blackField && this.whiteField == other.whiteField;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(blackField, whiteField);
  }
}
