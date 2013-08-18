package com.cauchymop.goblob;

import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

@SuppressLint("DrawAllocation")
public class GoBoardView extends View {

  private static Paint boardColor = createPaint(0xFFD0B060);
  private static Paint blackColor = createPaint(0xFF000000);
  private static Paint lineColor = createPaint(0xFF000000);
  private static Paint whiteColor = createPaint(0xFFFFFFFF);

  private GoGame game;
  private Point lastClickedCellCoord = null;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;

  private Set<Listener> listeners = Sets.newHashSet();

  public GoBoardView(Context context, GoGame game) {
    super(context, null);
    this.game = game;
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
    int marginX = (canvas.getWidth() - boardSizeInPixels) / 2 + cellSizeInPixels / 2;
    int marginY = (canvas.getHeight() - boardSizeInPixels) / 2 + cellSizeInPixels / 2;
    for (int x = 0; x < game.getBoardSize(); x++) {
      canvas.drawLine(marginX, marginY + cellSizeInPixels * x,
          marginX + cellSizeInPixels * (game.getBoardSize()-1), marginY + cellSizeInPixels * x, lineColor);
      canvas.drawLine(marginX + cellSizeInPixels * x, marginY,
          marginX + cellSizeInPixels * x, marginY + cellSizeInPixels * (game.getBoardSize()-1), lineColor);
    }
    for (int x = 0; x < game.getBoardSize(); x++) {
      for (int y = 0; y < game.getBoardSize(); y++) {
        StoneColor contentColor = game.getColor(x, y);
        if (contentColor == StoneColor.Empty) continue;
        Paint color = (contentColor == StoneColor.Black) ? blackColor : whiteColor;
        canvas.drawCircle(marginX + cellSizeInPixels * x,
            marginY + cellSizeInPixels * y, cellSizeInPixels / 2, color);
      }
    }
  }

  public interface Listener {
    public void played(int x, int y);
  }
}
