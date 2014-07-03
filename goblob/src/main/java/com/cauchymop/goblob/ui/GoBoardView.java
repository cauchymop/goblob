package com.cauchymop.goblob.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.StoneColor;
import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Sets;

import java.util.Set;

@SuppressLint("DrawAllocation")
public class GoBoardView extends View {

  private static final Paint lastMovePaint = createLinePaint(0xFFFF0000, 5);
  private static final Paint linePaint = createLinePaint(0xFF000000, 1);
  private static final Paint whiteFillPaint = createFillPaint(0xFFFFFFFF);
  private static final Paint blackFillPaint = createFillPaint(0xFF000000);

  private static final double STONE_RATIO = 0.95;

  private GoGameController gameController;
  private Point lastClickedCellCoord = null;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;
  private Bitmap whiteStoneBitmap;
  private Bitmap blackStoneBitmap;
  private int boardSize;

  private Set<Listener> listeners = Sets.newHashSet();
  private Rect rect = new Rect();  // For draw() usage.

  public GoBoardView(Context context, GoGameController gameController) {
    super(context, null);
    this.gameController = gameController;
    this.boardSize = gameController.getGame().getBoardSize();
    setClickable(true);
    blackStoneBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_stone);
    whiteStoneBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_stone);
  }

  private static Paint createLinePaint(int color, int width) {
    Paint paint = new Paint();
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(width);
    return paint;
  }

  private static Paint createFillPaint(int color) {
    Paint paint = new Paint();
    paint.setColor(color);
    paint.setStyle(Paint.Style.FILL);
    return paint;
  }

  private static Paint createTextPaint(int color, int size) {
    Paint textPaint = new Paint();
    textPaint.setColor(color);
    textPaint.setTextSize(size);
    return textPaint;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    int boardSizeInPixels = Math.min(getWidth(), getHeight());
    marginX = (getWidth() - boardSizeInPixels) / 2;
    marginY = (getHeight() - boardSizeInPixels) / 2;
    cellSizeInPixels = boardSizeInPixels / boardSize;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!isClickable()) {
      return false;
    }
    final int action = event.getAction();
    int nb_fingers = event.getPointerCount();

    if (nb_fingers != 1) {
      return false;
    }

    int x = (int) ((event.getX() - marginX) / cellSizeInPixels);
    int y = (int) ((event.getY() - marginY) / cellSizeInPixels);
    if (y < 0 || y >= boardSize || x < 0 || x >= boardSize) {
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
          firePlayed(x, y);
          lastClickedCellCoord = null;
          return true;
        }
      }
    }

    return false;
  }

  private void firePlayed(int x, int y) {
    for (Listener listener : listeners) {
      listener.played(x, y);
    }
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int boardSizeInPixels = Math.min(canvas.getWidth(), canvas.getHeight());
    int marginX = (canvas.getWidth() - boardSizeInPixels) / 2;
    int marginY = (canvas.getHeight() - boardSizeInPixels) / 2;
    int startLineX = marginX + cellSizeInPixels / 2;
    int startLineY = marginY + cellSizeInPixels / 2;
    drawBoardLines(canvas, startLineX, startLineY);
    drawBoardContent(canvas, startLineX, startLineY);
    drawEndGameStatus(canvas, startLineX, startLineY);
  }

  private void drawBoardContent(Canvas canvas, int startLineX, int startLineY) {
    int radius = cellSizeInPixels / 2;
    int lastMove = gameController.getGame().getLastMove();
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        int centerX = startLineX + cellSizeInPixels * x;
        int centerY = startLineY + cellSizeInPixels * y;
        drawStone(canvas, radius, gameController.getGame().getColor(x, y), centerX, centerY);
        int pos = gameController.getGame().getPos(x, y);
        // Last move
        if (lastMove == pos) {
          canvas.drawCircle(centerX, centerY, (float) radius, lastMovePaint);
        }
      }
    }
  }

  private void drawEndGameStatus(Canvas canvas, int startLineX, int startLineY) {
    if (gameController.getMode() != GoGameController.Mode.END_GAME_NEGOTIATION) {
      return;
    }
    for (PlayGameData.Position position : gameController.getDeadStones()) {
      int x = position.getX();
      int y = position.getY();
      int centerX = startLineX + cellSizeInPixels * x;
      int centerY = startLineY + cellSizeInPixels * y;
      int markSize = cellSizeInPixels / 4;
      canvas.drawRect(centerX - markSize, centerY - markSize, centerX + markSize, centerY + markSize,
          gameController.getGame().getColor(x, y) == StoneColor.Black ? whiteFillPaint : blackFillPaint);
    }
  }

  private void drawStone(Canvas canvas, int radius, StoneColor contentColor,
      int centerX, int centerY) {
    if (contentColor != null) {
      Bitmap stoneBitmap = (contentColor == StoneColor.Black)
          ? blackStoneBitmap : whiteStoneBitmap;
      rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
      canvas.drawBitmap(stoneBitmap, null, rect, null);
    }
  }

  private void drawBoardLines(Canvas canvas, int startLineX, int startLineY) {
    int lineLength = cellSizeInPixels * (boardSize - 1);
    for (int x = 0; x < boardSize; x++) {
      canvas.drawLine(startLineX, startLineY + cellSizeInPixels * x,
          startLineX + lineLength, startLineY + cellSizeInPixels * x, linePaint);
      canvas.drawLine(startLineX + cellSizeInPixels * x, startLineY,
          startLineX + cellSizeInPixels * x, startLineY + lineLength, linePaint);
    }
  }

  public interface Listener {
    public void played(int x, int y);
  }
}
