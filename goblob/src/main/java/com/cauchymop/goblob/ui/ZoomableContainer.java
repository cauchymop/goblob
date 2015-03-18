package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

/**
 */
public class ZoomableContainer extends FrameLayout implements ScaleGestureDetector.OnScaleGestureListener {

  private static final String TAG = "ZoomableView";
  private static final double MIN_MOVE = 10.0;

  protected PointF lastCoord = null;
  private boolean isDrag = false;
  private boolean multiFinger;

  public ZoomableContainer(Context context) {
    super(context);
    init(context);
  }

  public ZoomableContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ZoomableContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
    this.setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.i(TAG, "onTouch " + motionEvent.getX() + "," + motionEvent.getY());
        scaleDetector.onTouchEvent(motionEvent);
        if (motionEvent.getPointerCount() > 1) {
          multiFinger = true;
        }
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
          case MotionEvent.ACTION_DOWN:
            Log.i(TAG, "onTouchEvent ACTION_DOWN: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
            lastCoord = getPoint(motionEvent);
            isDrag = false;
            multiFinger = false;
            break;
          case MotionEvent.ACTION_MOVE:
            if (lastCoord != null && (isDrag || dist(getPoint(motionEvent), lastCoord) > MIN_MOVE)) {
              isDrag = true;
              float dx = motionEvent.getX() - lastCoord.x;
              float dy = motionEvent.getY() - lastCoord.y;
//              Log.i(TAG, "onTouchEvent move: translation = " + getTranslation(getChild()) + " delta = " + new PointF(dx, dy));
              translate(dx, dy);
              lastCoord = getPoint(motionEvent);
            }
            break;
          case MotionEvent.ACTION_UP:
            Log.i(TAG, "onTouchEvent ACTION_UP: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
            if (lastCoord != null && !isDrag && !multiFinger) {
              Log.i(TAG, "onTouchEvent click: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
              lastCoord = null;
              fireClick(motionEvent);
            }
            break;
        }

        return true;
      }
    });
  }

  private void translate(float dx, float dy) {
    ZoomableView child = getChild();
    float newX = child.getTranslationX() + dx;
    float newY = child.getTranslationY() + dy;
    float maxMoveX = (child.getScaleX() - 1) * getChild().getWidth() / 2;
    float maxMoveY = (child.getScaleY() - 1) * getChild().getHeight() / 2;
    if (newX < -maxMoveX) {
      newX = -maxMoveX;
    }
    if (newX > maxMoveX) {
      newX = maxMoveX;
    }
    if (newY < -maxMoveY) {
      newY = -maxMoveY;
    }
    if (newY > maxMoveY) {
      newY = maxMoveY;
    }
    child.setTranslationX(newX);
    child.setTranslationY(newY);
  }

  private double dist(PointF a, PointF b) {
    float dx = a.x - b.x;
    float dy = a.y - b.y;
    return Math.sqrt(dx*dx + dy*dy);
  }

  private PointF getPoint(MotionEvent event) {
    return new PointF(event.getX(), event.getY());
  }

  protected boolean fireClick(MotionEvent event) {
    int halfWidth = getWidth() / 2;
    int halfHeight = getHeight() / 2;
    ZoomableView child = getChild();
    float scale = child.getScaleX();
    float childX = halfWidth + ((event.getX() - halfWidth) - child.getTranslationX()) / scale;
    float childY = halfHeight + ((event.getY() - halfHeight) - child.getTranslationY()) / scale;
    return child.onClick(childX, childY);
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
    Log.i(TAG, "onScaleBegin");
    return true;
  }

  @Override
  public boolean onScale(ScaleGestureDetector scaleDetector) {
    float scaleFactor = scaleDetector.getScaleFactor();
//    Log.i(TAG, "onScale " + getScaleX() + " * " + scaleFactor);
    ZoomableView child = getChild();
    float scale = child.getScaleX() * scaleFactor;
    if (scale < 1) {
      scale = 1;
    }
    child.setScaleX(scale);
    child.setScaleY(scale);
    translate(0, 0);
    return true;
  }

  private ZoomableView getChild() {
    return (ZoomableView) getChildAt(0);
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector scaleDetector) {
    Log.i(TAG, "onScaleEnd");
  }
}
