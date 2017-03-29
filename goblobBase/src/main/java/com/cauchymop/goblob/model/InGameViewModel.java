package com.cauchymop.goblob.model;

public class InGameViewModel {

  private final BoardViewModel boardViewModel;
  private final PlayerViewModel currentPlayerViewModel;
  private final boolean passActionAvailable;
  private final boolean doneActionAvailable;
  private final String message;

  public InGameViewModel(BoardViewModel boardViewModel, PlayerViewModel currentPlayerViewModel,
      boolean passActionAvailable, boolean doneActionAvailable, String message) {
    this.boardViewModel = boardViewModel;
    this.currentPlayerViewModel = currentPlayerViewModel;
    this.passActionAvailable = passActionAvailable;
    this.doneActionAvailable = doneActionAvailable;
    this.message = message;
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

}
