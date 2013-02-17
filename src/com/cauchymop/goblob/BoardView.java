package com.cauchymop.goblob;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BoardView extends View {

    private enum ContentColor {
        Empty, Black, White, BlackTerritory, WhiteTerritory
    }

    private ContentColor[][] board = null;
    private int              boardSizeInCells = 5;

    
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBoard();
    }
    
    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBoard();
    }

    private void initBoard() {
        board = new ContentColor[boardSizeInCells][];
        for (int row = 0; row < boardSizeInCells; row++) {
            board[row] = new ContentColor[boardSizeInCells];
            for (int col = 0; col < boardSizeInCells; col++) {
                board[row][col] = ContentColor.Empty;
            }
        }
        board[1][2] = ContentColor.White;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int boardSizeInPixels = Math.min(canvas.getWidth(), canvas.getHeight());
        int marginX = (canvas.getWidth() - boardSizeInPixels) / 2;
        int marginY = (canvas.getHeight() - boardSizeInPixels) / 2;
        int cellSizeInPixels = boardSizeInPixels / boardSizeInCells;
        RectF r = new RectF();
        for (int row = 0; row < boardSizeInCells; row++) {
            for (int col = 0; col < boardSizeInCells; col++) {
                r.set(marginX + (cellSizeInPixels * col),
                      marginY + (cellSizeInPixels * row),
                      marginX + (cellSizeInPixels * (col + 1)),
                      marginY + (cellSizeInPixels * (row + 1)));
                Paint paint = getPaintFromContentColor(board[row][col]);
                canvas.drawRect(r, paint);
            }
        }
    }

    private Paint getPaintFromContentColor(ContentColor contentColor) {
        Paint paint = new Paint();
        switch (contentColor) {
            case White:
                paint.setColor(Color.RED);
                break;
            case Black:
                paint.setColor(Color.GREEN);
                break;
            case WhiteTerritory:
                paint.setColor(Color.MAGENTA);
                break;
            case BlackTerritory:
                paint.setColor(Color.CYAN);
                break;
            case Empty:
                paint.setColor(Color.GRAY);
                break;

            default:
                break;
        }
        return paint;
    }
}
