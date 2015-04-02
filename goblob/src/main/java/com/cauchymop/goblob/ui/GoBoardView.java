package com.cauchymop.goblob.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

@SuppressLint("DrawAllocation")
public class GoBoardView extends ZoomableView {

  private static final Paint lastMovePaint = createLinePaint(0xFFFF0000, 5);
  private static final Paint linePaint = createLinePaint(0xFF000000, 2);
  private static final Paint whiteFillPaint = createFillPaint(0xFFFFFFFF);
  private static final Paint blackFillPaint = createFillPaint(0xFF000000);

  private static final double STONE_RATIO = 0.95;
  public static final float HOSHI_SIZE = .1F;

  private GoGameController gameController;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;
  private Bitmap whiteStoneBitmap;
  private Bitmap blackStoneBitmap;
  private int boardSize;

  private Set<Listener> listeners = Sets.newHashSet();
  private Rect rect = new Rect();  // For draw() usage.

  public GoBoardView(Context context, GoGameController gameController) {
    super(context);
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
    linePaint.setStrokeWidth(cellSizeInPixels / 25);
  }

  @Override
  public boolean onClick(float x, float y) {
    int xPos = (int) ((x - marginX) / cellSizeInPixels);
    int yPos = (int) ((y - marginY) / cellSizeInPixels);
    firePlayed(xPos, yPos);
    return true;
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
    drawHoshis(canvas, startLineX, startLineY);
    drawBoardContent(canvas, startLineX, startLineY);
    drawEndGameStatus(canvas, startLineX, startLineY);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int minSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    setMeasuredDimension(minSize, minSize);
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
    drawTerritories(canvas, startLineX, startLineY);
  }

  private void drawTerritories(Canvas canvas, int startLineX, int startLineY) {
    drawDeadStones(canvas, startLineX, startLineY);
    drawTerritories(canvas, startLineX, startLineY, gameController.getScore().getBlackTerritoryList(), blackFillPaint);
    drawTerritories(canvas, startLineX, startLineY, gameController.getScore().getWhiteTerritoryList(), whiteFillPaint);
  }

  private void drawDeadStones(Canvas canvas, int startLineX, int startLineY) {
    for (PlayGameData.Position position : gameController.getDeadStones()) {
      int x = position.getX();
      int y = position.getY();
      int centerX = startLineX + cellSizeInPixels * x;
      int centerY = startLineY + cellSizeInPixels * y;
      int markSize = cellSizeInPixels / 6;
      canvas.drawRect(centerX - markSize, centerY - markSize, centerX + markSize, centerY + markSize,
          gameController.getGame().getColor(x, y) == PlayGameData.Color.BLACK ? whiteFillPaint : blackFillPaint);
    }
  }

  private void drawTerritories(Canvas canvas, int startLineX, int startLineY,
      List<PlayGameData.Position> territoryList, Paint paint) {
    for (PlayGameData.Position position : territoryList) {
      int x = position.getX();
      int y = position.getY();
      int centerX = startLineX + cellSizeInPixels * x;
      int centerY = startLineY + cellSizeInPixels * y;
      int markSize = cellSizeInPixels / 6;
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

  public interface Listener {
    public void played(int x, int y);
  }
}
