package com.cauchymop.goblob.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
 */
public class GoGame extends Game implements Parcelable {

  public static final int NO_MOVE = -1;
  public static final Parcelable.Creator<GoGame> CREATOR = new Parcelable.Creator<GoGame>() {
    public GoGame createFromParcel(Parcel in) {
      return new GoGame(in);
    }

    public GoGame[] newArray(int size) {
      return new GoGame[size];
    }
  };

  private static final String TAG = GoGame.class.getName();
  private final GoPlayer blackPlayer;
  private final GoPlayer whitePlayer;
  private int boardSize;
  private GoBoard board;
  private PlayerController blackController;
  private PlayerController whiteController;
  private StoneColor currentColor;
  private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
  private ArrayList<Integer> moveHistory = Lists.newArrayList();
  // Instance pool management.
  private GoBoard[] boardPool = new GoBoard[10];
  private int boardPoolSize = 0;

  public GoGame(int boardSize, GoPlayer blackPlayer, GoPlayer whitePlayer) {
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

  private PlayerController getCurrentController() {
    if (currentColor == StoneColor.Black) {
      return blackController;
    } else {
      return whiteController;
    }
  }

  private PlayerController getOpponentController() {
    if (currentColor == StoneColor.Black) {
      return whiteController;
    } else {
      return blackController;
    }
  }

  private GoBoard getNewBoard() {
    if (boardPoolSize == 0) {
      return boardSize == 5 ? new GoBoard5() : new GenericGoBoard(boardSize);
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
    return play(controller, getPassValue());
  }

  public boolean play(PlayerController controller, int x, int y) {
    return play(controller, getMove(x, y));
  }

  @Override
  public boolean play(PlayerController controller, int move) {
    if (controller != getCurrentController()) {
      return false;
    }

    GoBoard newBoard = getNewBoard();
    newBoard.copyFrom(board);

    if (move == getPassValue()) {
      applyMove(newBoard, move);
      return true;
    }

    int x = move % boardSize;
    int y = move / boardSize;
    if (newBoard.play(currentColor, x, y) && !boardHistory.contains(newBoard)) {
      applyMove(newBoard, move);
      return true;
    }

    recycleBoard(newBoard);
    return false;
  }

  private synchronized void applyMove(GoBoard newBoard, int move) {
    boardHistory.add(newBoard);
    moveHistory.add(move);
    board = newBoard;
    currentColor = currentColor.getOpponent();
    if (getOpponentController() != null) {
      getOpponentController().opponentPlayed(move);
    }
    fireGameChanged();
  }

  @Override
  public synchronized void undo() {
    currentColor = currentColor.getOpponent();
    recycleBoard(boardHistory.remove(boardHistory.size() - 1));
    moveHistory.remove(moveHistory.size() - 1);
    board = boardHistory.get(boardHistory.size() - 1);
  }

  @Override
  public synchronized Game copy() {
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

  public int getMove(int x, int y) {
    return y * getBoardSize() + x;
  }

  @Override
  public boolean isGameEnd() {
    if (moveHistory.size() < 2) {
      return false;
    }
    int lastMove = moveHistory.get(moveHistory.size() - 1);
    int previousMove = moveHistory.get(moveHistory.size() - 2);
    int passMove = getPassValue();
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

  public GoPlayer getBlackPlayer() {
    return blackPlayer;
  }

  public GoPlayer getWhitePlayer() {
    return whitePlayer;
  }

  public StoneColor getCurrentColor() {
    return currentColor;
  }

  public int getBoardSize() {
    return boardSize;
  }

  public double[] getScores() {
    PlayerController lastController = whiteController;
    if (!(lastController instanceof AIPlayerController)) {
      lastController = blackController;
      if (!(lastController instanceof AIPlayerController)) {
        return null;
      }
    }
    AIPlayerController aiController = (AIPlayerController) lastController;
    return aiController.getAi().getScores();
  }

  public GoPlayer getCurrentPlayer() {
    return getGoPlayer(getCurrentColor());
  }

  public GoPlayer getOpponent() {
    return getGoPlayer(getCurrentColor().getOpponent());
  }

  private GoPlayer getGoPlayer(StoneColor color) {
    switch (color) {
      case Black:
        return getBlackPlayer();
      case White:
        return getWhitePlayer();
      default:
        throw new IllegalStateException("Invalid Player Color: " + getCurrentColor());
    }
  }

  public boolean isLastMovePass() {
    return getLastMove() == getPassValue();
  }

  public int getPassValue() {
    return boardSize * boardSize;
  }

  public int getLastMove() {
    return (moveHistory.isEmpty()) ? NO_MOVE : moveHistory.get(moveHistory.size() - 1);
  }
}
