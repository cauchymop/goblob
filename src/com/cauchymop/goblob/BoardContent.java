package com.cauchymop.goblob;

/**
 * Class to represent the visible content of the board (stones and territories).
 */
public class BoardContent {

    private final int boardSizeInCells;

    private ContentColor[][] board;

    public BoardContent(int boardSizeInCells) {
        this.boardSizeInCells = boardSizeInCells;
    }

    public void setContentColor(int x, int y, ContentColor currentPlayerColor) {
        board[y][x] = currentPlayerColor;
    }

    public ContentColor getContentColor(int x, int y) {
        return board[y][x];
    }

    private void initBoard() {
        board = new ContentColor[boardSizeInCells][];
        for (int row = 0; row < boardSizeInCells; row++) {
            board[row] = new ContentColor[boardSizeInCells];
            for (int col = 0; col < boardSizeInCells; col++) {
                board[row][col] = ContentColor.Empty;
            }
        }
    }

    public static enum ContentColor {
        Empty, Black, White, BlackTerritory, WhiteTerritory;
    }
}
