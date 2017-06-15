package com.cauchymop.goblob.viewmodel;

public class InGameViewModel {

  private final BoardViewModel boardViewModel;
  private final PlayerViewModel currentPlayerViewModel;
  private final boolean passActionAvailable;
  private final boolean doneActionAvailable;
  private final String message;
  private final boolean undoActionAvailable;
  private final boolean redoActionAvailable;
  private final boolean resignActionAvailable;

  public InGameViewModel(BoardViewModel boardViewModel, PlayerViewModel currentPlayerViewModel,
      boolean passActionAvailable, boolean doneActionAvailable, String message,
      boolean undoActionAvailable, boolean redoActionAvailable, boolean resignActionAvailable) {
    this.boardViewModel = boardViewModel;
    this.currentPlayerViewModel = currentPlayerViewModel;
    this.passActionAvailable = passActionAvailable;
    this.doneActionAvailable = doneActionAvailable;
    this.message = message;
    this.undoActionAvailable = undoActionAvailable;
    this.redoActionAvailable = redoActionAvailable;
    this.resignActionAvailable = resignActionAvailable;
  }

  public BoardViewModel getBoardViewModel() {
    return boardViewModel;
  }

  public PlayerViewModel getCurrentPlayerViewModel() {
    return currentPlayerViewModel;
  }

  public boolean isPassActionAvailable() {
    return passActionAvailable;
  }

  public boolean isDoneActionAvailable() {
    return doneActionAvailable;
  }

  public String getMessage() {
    return message;
  }

  public boolean isUndoActionAvailable() {
    return undoActionAvailable;
  }

  public boolean isRedoActionAvailable() {
    return redoActionAvailable;
  }

  public boolean isResignActionAvailable() {
    return resignActionAvailable;
  }
}
