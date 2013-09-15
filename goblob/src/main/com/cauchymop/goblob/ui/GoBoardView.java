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
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.StoneColor;
import com.google.common.collect.Sets;

import java.util.Set;

@SuppressLint("DrawAllocation")
public class GoBoardView extends View {

  private static final Paint lastMovePaint = createLinePaint(0xFFFF0000, 5);
  private static final Paint linePaint = createLinePaint(0xFF000000, 1);
  private static final Paint textPaint = createTextPaint(0xFFFF0000, 20);

  private static final double STONE_RATIO = 0.95;

  private GoGame game;
  private Point lastClickedCellCoord = null;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;
  private Bitmap whiteStoneBitmap;
  private Bitmap blackStoneBitmap;

  private Set<Listener> listeners = Sets.newHashSet();

  public GoBoardView(Context context, GoGame game) {
    super(context, null);
    this.game = game;
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
    cellSizeInPixels = boardSizeInPixels / game.getBoardSize();
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
    if (y < 0 || y >= game.getBoardSize() || x < 0 || x >= game.getBoardSize()) {
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
          firePlayed(game.getMove(x, y));
          lastClickedCellCoord = null;
          return true;
        }
      }
    }

    return false;
  }

  private void firePlayed(int move) {
    for (Listener listener : listeners) {
      listener.played(move);
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
    int lineLength = cellSizeInPixels * (game.getBoardSize() - 1);
    for (int x = 0; x < game.getBoardSize(); x++) {
      canvas.drawLine(startLineX, startLineY + cellSizeInPixels * x,
          startLineX + lineLength, startLineY + cellSizeInPixels * x, linePaint);
      canvas.drawLine(startLineX + cellSizeInPixels * x, startLineY,
          startLineX + cellSizeInPixels * x, startLineY + lineLength, linePaint);
    }
    double[] scores = game.getScores();
    Rect rect = new Rect();
    int lastMove = game.getLastMove();
    for (int x = 0; x < game.getBoardSize(); x++) {
      for (int y = 0; y < game.getBoardSize(); y++) {
        StoneColor contentColor = game.getColor(x, y);
        if (contentColor != StoneColor.Empty) {
          Bitmap stoneBitmap = (contentColor == StoneColor.Black)
              ? blackStoneBitmap : whiteStoneBitmap;
          rect.set(marginX + cellSizeInPixels * x, marginY + cellSizeInPixels * y,
              marginX + cellSizeInPixels * (x + 1), marginY + cellSizeInPixels * (y + 1));
          canvas.drawBitmap(stoneBitmap, null, rect, null);
          if (lastMove == game.getMove(x, y)) {
            canvas.drawCircle(startLineX + cellSizeInPixels * x, startLineY + cellSizeInPixels * y,
                (float) (cellSizeInPixels / 2), lastMovePaint);
          }
        }
        int pos = y * game.getBoardSize() + x;
        if (scores != null && !Double.isNaN(scores[pos])) {
          double score = scores[pos];
          canvas.drawText(Double.toString(score), startLineX + cellSizeInPixels * x,
              startLineY + cellSizeInPixels * y, textPaint);
        }
      }
    }
  }

  public interface Listener {
    public void played(int move);
  }
}
