package com.cauchymop.goblob;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame extends Game implements Parcelable {

  private static final String TAG = GoGame.class.getName();
  
  private int boardSize;
  private GoBoard board;
  private final Player blackPlayer;
  private final Player whitePlayer;
  private PlayerController blackController;
  private PlayerController whiteController;
  private StoneColor currentColor;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();

  // Instance pool management.
  private GoBoard[] boardPool = new GoBoard[10];
  private int boardPoolSize = 0;

  public GoGame(int boardSize, Player blackPlayer, Player whitePlayer) {
    this.boardSize = boardSize;
    currentColor = StoneColor.Black;
    board = getNewBoard();
    boardHistory.add(board);
    this.blackPlayer = blackPlayer;
    this.whitePlayer = whitePlayer;
  }

  private GoGame(Parcel in) {
    boardSize = in.readInt();
    board = in.readParcelable(GoBoard.class.getClassLoader());
    blackPlayer = in.readParcelable(Player.class.getClassLoader());
    whitePlayer = in.readParcelable(Player.class.getClassLoader());
    currentColor = StoneColor.values()[in.readInt()];
    boardHistory = in.readArrayList(GoBoard.class.getClassLoader());
    moveHistory = in.readArrayList(Integer.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(boardSize);
    dest.writeParcelable(board, 0);
    dest.writeParcelable(blackPlayer, 0);
    dest.writeParcelable(whitePlayer, 0);
    dest.writeInt(currentColor.ordinal());
    dest.writeList(boardHistory);
    dest.writeList(moveHistory);
  }

  public static final Parcelable.Creator<GoGame> CREATOR = new Parcelable.Creator<GoGame>() {
    public GoGame createFromParcel(Parcel in) {
      return new GoGame(in);
    }

    public GoGame[] newArray(int size) {
      return new GoGame[size];
    }
  };

  public void runGame() {
    Thread thread = new Thread("Game") {

      @Override
      public void run() {
        while (!isGameEnd()) {
          Log.d(TAG, currentColor + ".startTurn()");
          getCurrentController().startTurn();
        }
      }
    };
    thread.start();
  }

  @Override
  protected PlayerController getCurrentController() {
    if (currentColor == StoneColor.Black) {
      return blackController;
    } else {
      return whiteController;
    }
  }

  private GoBoard getNewBoard() {
    if (boardPoolSize == 0) {
      return new GenericGoBoard(boardSize);
    }
    boardPoolSize--;
    GoBoard board = boardPool[boardPoolSize];
    board.clear();
    return board;
  }

  private void recycleBoard(GoBoard board) {
    boardPool[boardPoolSize] = board;
    boardPoolSize++;
  }

  public boolean pass(PlayerController controller) {
    if (controller != getCurrentController()) {
      return false;
    }
    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);
    boardHistory.add(newBoard);
    moveHistory.add(boardSize*boardSize);
    board = newBoard;
    currentColor = currentColor.getOpponent();
    fireGameChanged();
    return true;
  }

  public boolean play(PlayerController controller, int x, int y) {
    if (controller != getCurrentController()) {
      return false;
    }
    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);
    if (newBoard.play(currentColor, x, y) && !boardHistory.contains(newBoard)) {
      boardHistory.add(newBoard);
      moveHistory.add(y*boardSize + x);
      board = newBoard;
      currentColor = currentColor.getOpponent();
      fireGameChanged();
      return true;
    }
    recycleBoard(newBoard);
    return false;
  }

  @Override
  public void undo() {
    currentColor = currentColor.getOpponent();
    recycleBoard(boardHistory.remove(boardHistory.size() - 1));
    moveHistory.remove(moveHistory.size() - 1);
    board = boardHistory.get(boardHistory.size() - 1);
  }

  @Override
  public Game copy() {
    GoGame copy = new GoGame(boardSize, blackPlayer, whitePlayer);
    for (Integer move : moveHistory) {
      copy.play(copy.getCurrentController(), move);
    }
    return copy;
  }

  @Override
  public int getPosCount() {
    return boardSize * boardSize + 1;
  }

  @Override
  public boolean play(PlayerController controller, int pos) {
    if (pos == boardSize * boardSize) {
      pass(controller);
      return true;
    }
    return play(controller, pos % boardSize, pos / boardSize);
  }

  @Override
  public boolean isGameEnd() {
    if (moveHistory.size() < 2) {
      return false;
    }
    int lastMove = moveHistory.get(moveHistory.size() - 1);
    int previousMove = moveHistory.get(moveHistory.size() - 2);
    int passMove = boardSize * boardSize;
    return lastMove == passMove && previousMove == passMove;
  }

  @Override
  public double getScore() {
    return board.getScore(currentColor);
  }

  public StoneColor getColor(int x, int y) {
    return board.getColor(x, y);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public void setBlackController(PlayerController blackController) {
    Log.d(TAG, "setBlackController: " + blackController);
    this.blackController = blackController;
  }

  public void setWhiteController(PlayerController whiteController) {
    Log.d(TAG, "setWhiteController: " + whiteController);
    this.whiteController = whiteController;
  }

  public Player getBlackPlayer() {
    return blackPlayer;
  }

  public Player getWhitePlayer() {
    return whitePlayer;
  }

  public StoneColor getCurrentColor() {
    return currentColor;
  }

  public int getBoardSize() {
    return boardSize;
  }
}
