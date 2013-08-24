package com.cauchymop.goblob;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends Activity implements Game.Listener, GoBoardView.Listener {

  public static final String EXTRA_GAME = "Game";
  private GoGame goGame;
  private GoBoardView goBoardView;
  private HumanPlayerController currentPlayerController;
  private TextView titleView;
  private ImageView titleImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_activity);
    FrameLayout container = (FrameLayout) findViewById(R.id.boardViewContainer);
    goGame = getIntent().getParcelableExtra(EXTRA_GAME);
    goGame.setBlackController(getController(goGame.getBlackPlayer()));
    goGame.setWhiteController(getController(goGame.getWhitePlayer()));
    goGame.addListener(this);
    goGame.runGame();
    goBoardView = new GoBoardView(getApplicationContext(), goGame);
    goBoardView.addListener(this);
    container.addView(goBoardView);

    titleView = (TextView) findViewById(R.id.title);
    titleImage = (ImageView) findViewById(R.id.titleImage);
    updateFromCurrentPlayer();
  }

  private void updateFromCurrentPlayer() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Player currentPlayer = getCurrentPlayer();
        titleView.setText(currentPlayer.getName());
        titleImage.setImageBitmap(currentPlayer.getAvatar());
      }
    });
  }

  private Player getCurrentPlayer() {
    final Player currentPlayer;
    switch (goGame.getCurrentColor()) {
      case Black:
        currentPlayer = goGame.getBlackPlayer();
        break;
      case White:
        currentPlayer = goGame.getWhitePlayer();
        break;
      default:
        throw new IllegalStateException("Invalid Player Color: " + goGame.getCurrentColor());
    }
    return currentPlayer;
  }

  private Player getOpponent() {
    final Player opponent;
    switch (goGame.getCurrentColor()) {
      case Black:
        opponent = goGame.getWhitePlayer();
        break;
      case White:
        opponent = goGame.getBlackPlayer();
        break;
      default:
        throw new IllegalStateException("Invalid Player Color: " + goGame.getCurrentColor());
    }
    return opponent;
  }

  private PlayerController getController(Player player) {
    if (player.getType() == Player.PlayerType.AI) {
      return new AIPlayerController(goGame);
    }
    if (player.getType() == Player.PlayerType.HUMAN) {
      return new HumanPlayerController(goGame);
    }
    throw new RuntimeException("Unsupported player type");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_menu, menu);
    return true;
  }

  public void pass(View v) {
    if (v == null || v.getId() != R.id.pass_button) {
      return;
    }
    currentPlayerController.pass();
  }

  @Override
  public void gameChanged(Game game, Bundle info) {
    // Check for End of Game
    if (game.isGameEnd()) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          new AlertDialog.Builder(GameActivity.this)
              .setTitle(getString(R.string.end_of_game_dialog_title))
              .setMessage(getString(R.string.end_of_game_dialog_message))
              .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  setResult(RESULT_OK);
                  finish();
                }
              })
              .create()
              .show();
        }
      });
      return;
    }

    // Refresh UI and current controller
    goBoardView.postInvalidate();
    updateFromCurrentPlayer();

    // Check for Pass from Previous Player
    if (info != null && info.getBoolean(GoGame.CHANGE_PLAYER_PASSED)) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          String message = getString(R.string.opponent_passed_dialog_message, getOpponent().getName());
          new AlertDialog.Builder(GameActivity.this)
              .setTitle(getString(R.string.opponent_passed_dialog_title))
              .setMessage(message)
              .setPositiveButton(getString(R.string.button_pass_label), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  currentPlayerController.pass();
                }
              })
              .setNegativeButton(getString(R.string.button_continue_label), null)
              .create()
              .show();
        }
      });
    }


  }

  @Override
  public void played(int x, int y) {
    currentPlayerController.play(x, y);
  }

  private void setHumanInteractionEnabled(final boolean enabled) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Enable or Disable Pass Button for Local Humans
        final Button pass_button = (Button) findViewById(R.id.pass_button);
        pass_button.setEnabled(enabled);

        final View boardContainer = findViewById(R.id.boardViewContainer);
        goBoardView.setClickable(enabled);
      }
    });
  }

  private class HumanPlayerController extends PlayerController {
    private boolean played;
    private GoGame game;

    public HumanPlayerController(GoGame game) {
      this.game = game;
    }

    private void play(int x, int y) {
      if (!played && game.play(this, x, y)) {
        played = true;
        synchronized (this) {
          this.notifyAll();
        }
      } else {
        buzz();
      }
    }

    private void pass() {
      if (!played && game.pass(this)) {
        played = true;
        synchronized (this) {
          this.notifyAll();
        }
      } else {
        buzz();
      }
    }

    @Override
    public void startTurn() {
      // Enable Interactions for Local Humans
      setHumanInteractionEnabled(true);

      currentPlayerController = this;
      played = false;
      synchronized (this) {
        while (!played) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            // Expected.
          }
        }
      }

      // Disable Interactions for Local Humans
      setHumanInteractionEnabled(false);
    }

    private void buzz() {
      try {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
      } catch (Exception e) {
        System.err.println("Exception while buzzing");
        e.printStackTrace();
      }
    }
  }
}
