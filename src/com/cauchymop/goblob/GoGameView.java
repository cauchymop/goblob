package com.cauchymop.goblob;

import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableMap;

@SuppressLint("DrawAllocation")
public class GoGameView extends View implements Game.Listener {

  private int boardSizeInCells = 5;
  private GoGame game;
  private Point lastClickedCellCoord = null;
  private int marginX;
  private int marginY;
  private int cellSizeInPixels;
  private HumanPlayerController currentPlayerController;

  private Map<StoneColor, Paint> colorToPaint = ImmutableMap.of(
      StoneColor.White, createPaint(0xFFFF0000),
      StoneColor.Black, createPaint(0xFF00FF00),
      StoneColor.WhiteTerritory, createPaint(0xFFC08080),
      StoneColor.BlackTerritory, createPaint(0xFF80C080),
      StoneColor.Empty, createPaint(0xFF000000)
  );

  public GoGameView(Context context, GoGame game) {
    super(context, null);
    this.game = game;
    game.setBlackController(getController(game.getBlackPlayer()));
    game.setWhiteController(getController(game.getWhitePlayer()));
    game.addListener(this);
    game.runGame();
  }

  private PlayerController getController(Player player) {
    if (player.getType() == Player.PlayerType.AI) {
      return new AIPlayerController(game);
    }
    if (player.getType() == Player.PlayerType.HUMAN) {
      return new HumanPlayerController(game);
    }
    throw new RuntimeException("Unsupported player type");
  }

  private Paint createPaint(int color) {
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
    cellSizeInPixels = boardSizeInPixels / boardSizeInCells;
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
    if (y < 0 || y >= boardSizeInCells || x < 0 || x >= boardSizeInCells) {
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
          currentPlayerController.play(x, y);
          return true;
        }
      }
    }

    return false;
  }

  private void buzz() {
    try {
      Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
      r.play();
    } catch (Exception e) {
      System.err.println("Exception while buzzing");
      e.printStackTrace();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int boardSizeInPixels = Math.min(canvas.getWidth(), canvas.getHeight());
    int marginX = (canvas.getWidth() - boardSizeInPixels) / 2;
    int marginY = (canvas.getHeight() - boardSizeInPixels) / 2;
    int cellSize = boardSizeInPixels / boardSizeInCells;
    Paint textPaint = createPaint(0xFFC0C0FF);
    textPaint.setTextSize(30);
    RectF r = new RectF();
    for (int x = 0; x < boardSizeInCells; x++) {
      for (int y = 0; y < boardSizeInCells; y++) {
        r.set(marginX + (cellSize * x), marginY + (cellSize * y),
            marginX + (cellSize * (x + 1)), marginY + (cellSize * (y + 1)));
        StoneColor contentColor = game.getColor(x, y);
        Paint paint = colorToPaint.get(contentColor);
        canvas.drawRect(r, paint);
//        canvas.drawText(textScore, r.centerX()-10*textScore.length(), r.centerY()+15, textPaint);
      }
    }
  }

  @Override
  public void gameChanged(Game game) {
    invalidate();
  }

  public void pass() {
    game.pass(Game.MoveType.REAL);
  }

  private class HumanPlayerController extends PlayerController {

    private boolean played;
    private GoGame game;

    public HumanPlayerController(GoGame game) {
      this.game = game;
    }

    private void play(int x, int y) {
      if (game.play(x, y, Game.MoveType.REAL)) {
        this.notifyAll();
      } else {
        buzz();
      }
    }

    @Override
    public void startTurn() {
      // TODO: show that it's my turn.

      played = false;
      currentPlayerController = this;
      synchronized (this) {
        while (!played) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            // Expected.
          }
        }
      }

      // TODO: show that it's not my turn anymore.
      lastClickedCellCoord = null;
    }
  }
}
