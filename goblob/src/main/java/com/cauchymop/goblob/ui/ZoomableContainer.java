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
  private MotionEvent.PointerCoords pointerCoord = new MotionEvent.PointerCoords();
  private int lastPointerCount = 0;

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
        PointF centerPoint = getCenterPoint(motionEvent);
        if (motionEvent.getPointerCount() != lastPointerCount) {
          Log.i(TAG, "xxx !=");
          lastCoord = centerPoint;
          isDrag = false;
        }
        lastPointerCount = motionEvent.getPointerCount();
        if (lastPointerCount > 1) {
          Log.i(TAG, "xxx multi");
          multiFinger = true;
        }
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
          case MotionEvent.ACTION_DOWN:
//            Log.i(TAG, "onTouchEvent ACTION_DOWN: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
            break;
          case MotionEvent.ACTION_MOVE:
            Log.i(TAG, "xxx move?");
            if (isDrag || dist(centerPoint, lastCoord) > MIN_MOVE) {
              Log.i(TAG, "xxx move.");
              isDrag = true;
              float dx = centerPoint.x - lastCoord.x;
              float dy = centerPoint.y - lastCoord.y;
//              Log.i(TAG, String.format("onTouchEvent mov: tr=%f,%f d=%f,%f",
//                  getChild().getTranslationX(), getChild().getTranslationY(), dx, dy));
              translate(dx, dy);
              lastCoord = centerPoint;
            }
            break;
          case MotionEvent.ACTION_UP:
//            Log.i(TAG, "onTouchEvent ACTION_UP: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
            if (!isDrag && !multiFinger) {
//              Log.i(TAG, "onTouchEvent click: x:" + motionEvent.getX() + " y:" + motionEvent.getY());
              fireClick(motionEvent);
            }
            Log.i(TAG, "xxx up ");
            multiFinger = false;
            lastPointerCount = 0;
            break;
        }

        return true;
      }
    });
  }

  private PointF getCenterPoint(MotionEvent motionEvent) {
    int nbFingers = motionEvent.getPointerCount();
    if (nbFingers == 1) {
      return getPoint(motionEvent);
    } else if (nbFingers >= 2) {
      motionEvent.getPointerCoords(0, pointerCoord);
      float x1 = pointerCoord.x;
      float y1 = pointerCoord.y;
      motionEvent.getPointerCoords(1, pointerCoord);
      float x2 = pointerCoord.x;
      float y2 = pointerCoord.y;
      return new PointF((x1+x2)/2, (y1+y2)/2);
    }
    return null;
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
    ZoomableView child = getChild();
//    Log.i(TAG, String.format("onScale %f*%f - (translate x:%f y:%f)", child.getScaleX(), scaleFactor, child.getTranslationX(), child.getTranslationY()));
    float scale = child.getScaleX() * scaleFactor;
    if (scale < 1) {
      scale = 1;
    }
    child.setScaleX(scale);
    child.setScaleY(scale);

    float newX = child.getTranslationX() * scaleFactor;
    float newY = child.getTranslationY() * scaleFactor;
    child.setTranslationX(newX);
    child.setTranslationY(newY);

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
