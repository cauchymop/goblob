package com.cauchymop.goblob;

import java.util.HashSet;

/**
 * Class to represent the state of a Go board, and the rules of the game to play moves.
 */
public class GoBoard {

    private final int boardSizeInCells;
    private Color board[];

    public GoBoard(int boardSizeInCells) {
        this.boardSizeInCells = boardSizeInCells;
        initEmptyCells();
        initBorderCells();
    }

    /**
     * Plays a move.
     *
     * @param color the {@link Color} being played
     * @param x the x coordinate for the move (0 to {@code boardSizeInCells}-1)
     * @param y the y coordinate for the move (0 to {@code boardSizeInCells}-1)
     * @return whether the move was valid and played
     */
    public boolean play(Color color, int x, int y) {
        int pos = getPos(x, y);
        if (board[pos] != Color.Empty) {
            return false;
        }
        board[pos] = color;
        captureNeighbors(pos);
        if (getLiberties(pos).isEmpty()) {
            board[pos] = Color.Empty;
            return false;
        }
        return true;
    }

    private Color getOpponent(Color color) {
        if (color == Color.Black) {
            return Color.White;
        }
        if (color == Color.White) {
            return Color.Black;
        }
        throw new RuntimeException("Invalid color " + color);
    }

    private void captureNeighbors(int pos) {
        Color opponent = getOpponent(board[pos]);
        HashSet<Integer> captured = new HashSet<Integer>();
        findCapturedNeighbors(getNorth(pos), opponent, captured);
        findCapturedNeighbors(getSouth(pos), opponent, captured);
        findCapturedNeighbors(getEast(pos), opponent, captured);
        findCapturedNeighbors(getWest(pos), opponent, captured);
        for (Integer capturedPosition : captured) {
            board[capturedPosition] = Color.Empty;
        }
    }

    private void findCapturedNeighbors(int pos, Color opponent, HashSet<Integer> captured) {
        if (board[pos] != opponent) {
            return;
        }
        HashSet<Integer> liberties = new HashSet<Integer>();
        HashSet<Integer> stones = new HashSet<Integer>();
        findLiberties(board[pos], pos, stones, liberties);
        if (liberties.isEmpty()) {
            captured.addAll(stones);
        }
    }

    private HashSet getLiberties(int pos) {
        HashSet<Integer> liberties = new HashSet<Integer>();
        HashSet<Integer> stones = new HashSet<Integer>();
        findLiberties(board[pos], pos, stones, liberties);
        return liberties;
    }

    private void findLiberties(Color color, int pos, HashSet<Integer> stones, HashSet<Integer> liberties) {
        if (stones.contains(pos) || liberties.contains(pos)) {
            return;
        }
        if (board[pos] == color) {
            stones.add(pos);
            findLiberties(color, getNorth(pos), stones, liberties);
            findLiberties(color, getSouth(pos), stones, liberties);
            findLiberties(color, getWest(pos), stones, liberties);
            findLiberties(color, getEast(pos), stones, liberties);
        } else if (board[pos] == Color.Empty) {
            liberties.add(pos);
        }
    }

    private int getNorth(int pos) {
        return pos - (boardSizeInCells + 2);
    }

    private int getSouth(int pos) {
        return pos + (boardSizeInCells + 2);
    }

    private int getWest(int pos) {
        return pos - 1;
    }

    private int getEast(int pos) {
        return pos + 1;
    }

    private void initEmptyCells() {
        board = new Color[(boardSizeInCells+2)*(boardSizeInCells+2)];
        for (int x = 0 ; x < boardSizeInCells+2 ; x++) {
            for (int y = 0 ; y < boardSizeInCells ; y++) {
                board[getPos(x, y)] = Color.Empty;
            }
        }
    }

    private void initBorderCells() {
        for (int col = 0 ; col < boardSizeInCells ; col++) {
            board[getPos(0, col)] = Color.Border;
            board[getPos(col, 0)] = Color.Border;
            board[getPos(boardSizeInCells-1, col)] = Color.Border;
            board[getPos(col, boardSizeInCells-1)] = Color.Border;
        }
    }

    private int getPos(int x, int y) {
        return (y + 1) * (boardSizeInCells + 2) + (x + 1);
    }

    private static enum Color {
        Empty,
        Border,
        Black,
        White,
    }
}
