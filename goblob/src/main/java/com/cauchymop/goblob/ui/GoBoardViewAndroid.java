package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.GoBoardView;
import com.cauchymop.goblob.viewmodel.BoardViewModel;
import com.google.common.collect.Lists;

import java.util.List;

public class GoBoardViewAndroid extends ZoomableView implements GoBoardView {

  private static final Paint lastMovePaint = createLinePaint(0xFFFF0000, 5);
  private static final Paint linePaint = createLinePaint(0xFF000000, 2);
  private static final Paint whiteFillPaint = createFillPaint(0xFFFFFFFF);
  private static final Paint blackFillPaint = createFillPaint(0xFF000000);

  private static final double STONE_RATIO = 0.95;
  public static final float HOSHI_SIZE = .1F;

  private BoardViewModel boardViewModel = new BoardViewModel(9,new PlayGameData.Color[9][9], new PlayGameData.Color[9][9], -1, -1, false);
  private BoardEventListener boardEventListener;

  private int cellSizeInPixels;
  private Bitmap whiteStoneBitmap;
  private Bitmap blackStoneBitmap;

  private Rect rect = new Rect();  // For draw() usage.

  public GoBoardViewAndroid(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public GoBoardViewAndroid(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public GoBoardViewAndroid(Context context) {
    super(context);
    init(context);
  }

  public void init(Context context) {
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
    if (boardViewModel == null) {
      return;
    }
    int boardSizeInPixels = Math.min(getWidth(), getHeight());
    cellSizeInPixels = boardSizeInPixels / boardViewModel.getBoardSize();
    linePaint.setStrokeWidth(cellSizeInPixels / 25);
  }

  @Override
  public boolean onClick(float x, float y) {
    if (!isClickable()) {
      return false;
    }
    int xPos = (int) (x / cellSizeInPixels);
    int yPos = (int) (y / cellSizeInPixels);
    fireClicked(xPos, yPos);
    return true;
  }

  private void fireClicked(int x, int y) {
    if (boardEventListener != null) {
      boardEventListener.onIntersectionSelected(x, y);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (boardViewModel == null) {
      return;
    }

    int boardSizeInPixels = Math.min(canvas.getWidth(), canvas.getHeight());
    int startLineX = cellSizeInPixels / 2;
    int startLineY = cellSizeInPixels / 2;
    drawBoardLines(canvas, startLineX, startLineY);
    drawHoshis(canvas, startLineX, startLineY);
    drawBoardContent(canvas, startLineX, startLineY);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int minSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    setMeasuredDimension(minSize, minSize);
  }

  private void drawBoardContent(Canvas canvas, int startLineX, int startLineY) {
    int radius = cellSizeInPixels / 2;
    int boardSize = boardViewModel.getBoardSize();
    for (int x = 0; x < boardSize; x++) {
      for (int y = 0; y < boardSize; y++) {
        int centerX = startLineX + cellSizeInPixels * x;
        int centerY = startLineY + cellSizeInPixels * y;
        drawStone(canvas, radius, boardViewModel.getColor(x, y), centerX, centerY);
        drawTerritory(canvas, startLineX, startLineY, boardViewModel.getTerritory(x, y), centerX, centerY);
        if (boardViewModel.isLastMove(x, y)) {
          canvas.drawCircle(centerX, centerY, (float) radius, lastMovePaint);
        }
      }
    }
  }

  private void drawTerritory(Canvas canvas, int startLineX, int startLineY, PlayGameData.Color contentColor,
      int centerX, int centerY) {
    if (contentColor != null) {
      int markSize = cellSizeInPixels / 6;
      Paint paint = (contentColor == PlayGameData.Color.BLACK) ? blackFillPaint : whiteFillPaint;
      canvas.drawRect(centerX - markSize, centerY - markSize,
          centerX + markSize, centerY + markSize, paint);
    }
  }

  private void drawStone(Canvas canvas, int radius, PlayGameData.Color contentColor,
      int centerX, int centerY) {
    if (contentColor != null) {
      Bitmap stoneBitmap = (contentColor == PlayGameData.Color.BLACK)
          ? blackStoneBitmap : whiteStoneBitmap;
      rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
      canvas.drawBitmap(stoneBitmap, null, rect, null);
    }
  }

  private void drawBoardLines(Canvas canvas, int startLineX, int startLineY) {
    int boardSize = boardViewModel.getBoardSize();
    int lineLength = cellSizeInPixels * (boardSize - 1);
    for (int x = 0; x < boardSize; x++) {
      canvas.drawLine(startLineX, startLineY + cellSizeInPixels * x,
          startLineX + lineLength, startLineY + cellSizeInPixels * x, linePaint);
      canvas.drawLine(startLineX + cellSizeInPixels * x, startLineY,
          startLineX + cellSizeInPixels * x, startLineY + lineLength, linePaint);
    }
  }

  private void drawHoshis(Canvas canvas, int startLineX, int startLineY) {
    for (Point point : getHoshiCoords()) {
      canvas.drawCircle(startLineX + cellSizeInPixels * point.x,
          startLineY + cellSizeInPixels * point.y,
          cellSizeInPixels * HOSHI_SIZE, blackFillPaint);
    }
  }

  private List<Point> getHoshiCoords() {
    List<Point> res = Lists.newArrayList();
    int boardSize = boardViewModel.getBoardSize();
    if (boardSize >= 9 && boardSize <= 12) {
      int far = boardSize - 3;
      res.add(new Point(2, 2));
      res.add(new Point(far, 2));
      res.add(new Point(2, far));
      res.add(new Point(far, far));
    }
    if (boardSize >= 13) {
      int far = boardSize - 4;
      res.add(new Point(3, 3));
      res.add(new Point(far, 3));
      res.add(new Point(3, far));
      res.add(new Point(far, far));
    }
    if (boardSize >= 17 && boardSize % 2 == 1) {
      int far = boardSize - 4;
      int center = (boardSize - 1) / 2;
      res.add(new Point(3, center));
      res.add(new Point(far, center));
      res.add(new Point(center, 3));
      res.add(new Point(center, far));
      res.add(new Point(center, center));
    }
    return res;
  }

  public void setBoardEventListener(BoardEventListener boardEventListener) {
    this.boardEventListener = boardEventListener;
  }

  @Override
  public void setBoard(BoardViewModel boardViewModel) {
    this.boardViewModel = boardViewModel;
    setClickable(boardViewModel.isInteractive());
    invalidate();
  }
}
