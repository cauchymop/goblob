package com.cauchymop.goblob.model;

public class InGameViewModel {

  private final BoardViewModel boardViewModel;

  public InGameViewModel(BoardViewModel boardViewModel) {
    this.boardViewModel = boardViewModel;
  }

  public BoardViewModel getBoardViewModel() {
    return boardViewModel;
  }

}
