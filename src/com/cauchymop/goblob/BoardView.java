package com.cauchymop.goblob;

import java.util.Map;

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

import com.cauchymop.goblob.BoardContent.ContentColor;
import com.google.common.collect.ImmutableMap;

@SuppressLint("DrawAllocation")
public class BoardView extends View {

  private int boardSizeInCells = 5;
  private BoardContent board = new BoardContent(new GoBoard(boardSizeInCells));
  private ContentColor currentPlayerColor = ContentColor.Black;
  private Point lastClickedCellCoord = null;
  private int boardSizeInPixels;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;

  private Map<BoardContent.ContentColor, Paint> colorToPaint = ImmutableMap.of(
      ContentColor.White, createPaint(Color.RED),
      ContentColor.Black, createPaint(Color.GREEN),
      ContentColor.WhiteTerritory, createPaint(Color.MAGENTA),
      ContentColor.BlackTerritory, createPaint(Color.CYAN),
      ContentColor.Empty, createPaint(Color.GRAY)
  );

  /**
   * @param color
   * @return
   */
  public Paint createPaint(int color) {
    Paint p = new Paint();
    p.setColor(color);
    return p;
  }

  public BoardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BoardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
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

    int x = (int) ((event.getX() - marginX) / cellSizeInPixels);
    int y = (int) ((event.getY() - marginY) / cellSizeInPixels);
    if (y < 0 || y >= boardSizeInCells || x < 0 || x >= boardSizeInCells) {
      lastClickedCellCoord = null;
      return false;
    }
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        // Log.i("TOUCH EVENT", "ACTION_DOWN: row:" + row +" col:" + col);
        lastClickedCellCoord = new Point(x, y);
        return true;
      }

      case MotionEvent.ACTION_UP: {
        // Log.i("TOUCH EVENT", "ACTION_UP: row:" + row +" col:" + col);
        if (lastClickedCellCoord != null && lastClickedCellCoord.x == x
            && lastClickedCellCoord.y == y) {
          board.setContentColor(x, y, currentPlayerColor);
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
    for (int x = 0; x < boardSizeInCells; x++) {
      for (int y = 0; y < boardSizeInCells; y++) {
        r.set(canvasMarginX + (canvasCellSizeInPixels * x),
            canvasMarginY + (canvasCellSizeInPixels * y),
            canvasMarginX + (canvasCellSizeInPixels * (x + 1)),
            canvasMarginY + (canvasCellSizeInPixels * (y + 1)));
        ContentColor contentColor = board.getContentColor(x, y);
        // Log.i("", x + "," + y + ": " + contentColor);
        Paint paint = colorToPaint.get(contentColor);
        canvas.drawRect(r, paint);
      }
    }
  }

}
