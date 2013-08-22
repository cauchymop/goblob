package com.cauchymop.goblob;

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

import com.google.common.collect.Sets;

import java.util.Set;

@SuppressLint("DrawAllocation")
public class GoBoardView extends View {

  private static final Paint lineColor = createPaint(0xFF000000);
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

  private static Paint createPaint(int color) {
    Paint p = new Paint();
    p.setColor(color);
    return p;
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
    int lineLength = cellSizeInPixels * (game.getBoardSize() - 1);
    for (int x = 0; x < game.getBoardSize(); x++) {
      canvas.drawLine(startLineX, startLineY + cellSizeInPixels * x,
          startLineX + lineLength, startLineY + cellSizeInPixels * x, lineColor);
      canvas.drawLine(startLineX + cellSizeInPixels * x, startLineY,
          startLineX + cellSizeInPixels * x, startLineY + lineLength, lineColor);
    }
    Rect rect = new Rect();
    for (int x = 0; x < game.getBoardSize(); x++) {
      for (int y = 0; y < game.getBoardSize(); y++) {
        StoneColor contentColor = game.getColor(x, y);
        if (contentColor == StoneColor.Empty) continue;
        Bitmap stoneBitmap = (contentColor == StoneColor.Black)
            ? blackStoneBitmap : whiteStoneBitmap;
        rect.set(marginX + cellSizeInPixels * x, marginY + cellSizeInPixels * y,
            marginX + cellSizeInPixels * (x+1), marginY + cellSizeInPixels * (y + 1));
        canvas.drawBitmap(stoneBitmap, null, rect, null);
      }
    }
  }

  public interface Listener {
    public void played(int x, int y);
  }
}
