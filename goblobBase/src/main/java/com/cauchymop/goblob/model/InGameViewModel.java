package com.cauchymop.goblob.model;

public class InGameViewModel {

  private final BoardViewModel boardViewModel;
  private final PlayerViewModel currentPlayerViewModel;

  public InGameViewModel(BoardViewModel boardViewModel, PlayerViewModel currentPlayerViewModel) {
    this.boardViewModel = boardViewModel;
    this.currentPlayerViewModel = currentPlayerViewModel;
  }

  public BoardViewModel getBoardViewModel() {
    return boardViewModel;
  }

  public PlayerViewModel getCurrentPlayerViewModel() {
    return currentPlayerViewModel;
  }
}
