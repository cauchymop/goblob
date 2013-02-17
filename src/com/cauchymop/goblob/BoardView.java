package com.cauchymop.goblob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("DrawAllocation")
public class BoardView extends View {

    private enum ContentColor {
        Empty, Black, White, BlackTerritory, WhiteTerritory
    }

    private ContentColor[][] board                = null;
    private int              boardSizeInCells     = 5;
    private ContentColor     currentPlayerColor   = ContentColor.Black;
    private Point            lastClickedCellCoord = null;
    private int              boardSizeInPixels;
    private int              marginX;
    private int              marginY;
    private int              cellSizeInPixels;

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
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boardSizeInPixels = Math.min(getWidth(), getHeight());
        marginX = (getWidth() - boardSizeInPixels) / 2;
        marginY = (getHeight() - boardSizeInPixels) / 2;
        cellSizeInPixels = boardSizeInPixels / boardSizeInCells;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();
        int nb_fingers = event.getPointerCount();
        
        if (nb_fingers != 1) {
            return false;
        }
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                int row = (int) ((event.getY() - marginY) / cellSizeInPixels);
                int col = (int) ((event.getX() - marginX) / cellSizeInPixels);
//                Log.i("TOUCH EVENT", "ACTION_DOWN: row:" + row +" col:" + col);
                if (row < 0 || row >= boardSizeInCells || col < 0 || col >= boardSizeInCells) {
                    lastClickedCellCoord = null;
                    return false;
                }
                lastClickedCellCoord = new Point(col, row);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                int row = (int) ((event.getY() - marginY) / cellSizeInPixels);
                int col = (int) ((event.getX() - marginX) / cellSizeInPixels);
//                Log.i("TOUCH EVENT", "ACTION_UP: row:" + row +" col:" + col);
                if (row < 0 || row >= boardSizeInCells || col < 0 || col >= boardSizeInCells) {
                    lastClickedCellCoord = null;
                    return false;
                }

                if (lastClickedCellCoord != null && lastClickedCellCoord.x == col && lastClickedCellCoord.y == row) {
                    board[row][col] = currentPlayerColor;
                    lastClickedCellCoord = null;
                    endTurn();
                    invalidate();
                    return true;
                }
            }

        }

        return false;
    }

    private void endTurn() {
        if (currentPlayerColor == ContentColor.Black) {
            currentPlayerColor = ContentColor.White;
        } else {
            currentPlayerColor = ContentColor.Black;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int canvasBoardSizeInPixels = Math.min(canvas.getWidth(), canvas.getHeight());
        int canvasMarginX = (canvas.getWidth() - canvasBoardSizeInPixels) / 2;
        int canvasMarginY = (canvas.getHeight() - canvasBoardSizeInPixels) / 2;
        int canvasCellSizeInPixels = canvasBoardSizeInPixels / boardSizeInCells;
        RectF r = new RectF();
        for (int row = 0; row < boardSizeInCells; row++) {
            for (int col = 0; col < boardSizeInCells; col++) {
                r.set(canvasMarginX + (canvasCellSizeInPixels * col), canvasMarginY + (canvasCellSizeInPixels * row), canvasMarginX
                                                                                                                      + (canvasCellSizeInPixels * (col + 1)),
                      canvasMarginY + (canvasCellSizeInPixels * (row + 1)));
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
