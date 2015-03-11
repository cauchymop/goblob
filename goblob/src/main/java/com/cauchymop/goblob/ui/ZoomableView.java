package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 */
public class ZoomableView extends View {

  protected PointF lastClickedCellCoord = null;

  public ZoomableView(Context context) {
    super(context);
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

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        Log.i("TOUCH EVENT", "ACTION_DOWN: x:" + event.getX() + " y:" + event.getY());
        lastClickedCellCoord = getPoint(event);
        return true;
      }

      case MotionEvent.ACTION_UP: {
        Log.i("TOUCH EVENT", "ACTION_UP: x:" + event.getX() + " y:" + event.getY());
        if (lastClickedCellCoord != null && dist(lastClickedCellCoord, getPoint(event)) < getMaxClickRadius()) {
          return onClick(event);
        }
      }
    }

    return false;
  }

  private double dist(PointF a, PointF b) {
    float dx = a.x - b.x;
    float dy = a.y - b.y;
    return Math.sqrt(dx*dx + dy*dy);
  }

  private float getMaxClickRadius() {
    return 10;
  }

  private PointF getPoint(MotionEvent event) {
    return new PointF(event.getX(), event.getY());
  }

  protected boolean onClick(MotionEvent event) {
    return true;
  }
}
