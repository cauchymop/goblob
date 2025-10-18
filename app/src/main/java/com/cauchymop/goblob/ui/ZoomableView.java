package com.cauchymop.goblob.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public abstract class ZoomableView extends View {

  public ZoomableView(Context context) {
    super(context);
  }

  public ZoomableView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ZoomableView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public abstract boolean onClick(float x, float y);

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return false;
  }
}
