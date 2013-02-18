package com.cauchymop.goblob;

/**
 * Class to represent the visible content of the board (stones and territories).
 */
public class BoardContent extends Board {

  private final GoBoard goBoard;

  public BoardContent(GoBoard goBoard) {
    super(goBoard.getBoardSize());
    this.goBoard = goBoard;
    initBoard();
  }

  private void initBoard() {
    for (int x = 0; x < getBoardSize(); x++) {
      for (int y = 0; y < getBoardSize(); y++) {
        setColor(getPos(x, y), Color.Empty);
      }
    }
  }

  public boolean play(Color color, int x, int y) {
    if (!goBoard.play(color, x, y)) {
      return false;
    }
    updateFromGoBoard();
    updateTerritories();
    return true;
  }

  private void updateTerritories() {
    // TODO: scan board and update territories.
  }

  private void updateFromGoBoard() {
    for (int x = 0 ; x < getBoardSize() ; x++) {
      for (int y = 0 ; y < getBoardSize() ; y++) {
        setColor(getPos(x, y), goBoard.getColor(x, y));
      }
    }
  }
}
