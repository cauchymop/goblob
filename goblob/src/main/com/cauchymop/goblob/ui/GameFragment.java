package com.cauchymop.goblob.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AIPlayerController;
import com.cauchymop.goblob.model.Game;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.Player;
import com.cauchymop.goblob.model.PlayerController;
import com.cauchymop.goblob.model.StoneColor;
import com.google.android.gms.games.GamesClient;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements Game.Listener,
    GoBoardView.Listener {

  public static final String EXTRA_GAME = "Game";
  private GoGame goGame;
  private GoBoardView goBoardView;
  private HumanPlayerController currentPlayerController;

  public static GameFragment newInstance(GoGame game) {
    GameFragment instance = new GameFragment();

    Bundle args = new Bundle();
    args.putParcelable(EXTRA_GAME, game);
    instance.setArguments(args);

    return instance;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_game, container, false);
    FrameLayout boardViewContainer = (FrameLayout) v.findViewById(R.id.boardViewContainer);
    goGame = getArguments().getParcelable(EXTRA_GAME);
    goGame.setBlackController(getController(goGame.getBlackPlayer()));
    goGame.setWhiteController(getController(goGame.getWhitePlayer()));
    goGame.addListener(this);
    goGame.runGame();
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGame);
    goBoardView.addListener(this);
    boardViewContainer.addView(goBoardView);

    Button passButton = (Button) v.findViewById(R.id.pass_button);
    passButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        currentPlayerController.pass();
      }
    });

    return v;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    updateFromCurrentPlayer();
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
  }

  @Override
  public void onSignInFailed() {
    super.onSignInFailed();
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
  }

  private void updateFromCurrentPlayer() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateTitleArea();
        updateMessageArea();
      }
    });
  }

  private void updateTitleArea() {
    TextView titleView = (TextView) getView().findViewById(R.id.title);
    ImageView titleImage = (ImageView) getView().findViewById(R.id.titleImage);
    ImageView avatarImage = (ImageView) getView().findViewById(R.id.avatarImage);
    final GoPlayer currentPlayer = goGame.getCurrentPlayer();
    titleView.setText(currentPlayer.getName());
    titleImage.setImageResource(currentPlayer.getStoneColor() == StoneColor.White ? R.drawable.white_stone : R.drawable.black_stone);
    avatarImage.setImageBitmap(currentPlayer.getAvatar());
  }

  /**
   * Display a message if needed (other player has passed...), clean the message area otherwise.
   */
  private void updateMessageArea() {
    final String message;
    if (goGame.isLastMovePass()) {
      message = getString(R.string.opponent_passed_message, goGame.getOpponent().getName());
    } else {
      message = null;
    }

    TextView messageView = (TextView) getView().findViewById(R.id.message_textview);
    messageView.setText(message);
  }

  private PlayerController getController(Player player) {
    if (player.getType() == Player.PlayerType.AI) {
      return new AIPlayerController(goGame);
    }
    if (player.getType() == Player.PlayerType.HUMAN_LOCAL) {
      return new HumanPlayerController(goGame);
    }
    throw new RuntimeException("Unsupported player type");
  }

  @Override
  public void gameChanged(Game game) {
    if (game.isGameEnd()) {
      handleEndOfGame();
      return;
    }

    // Refresh UI and current controller
    goBoardView.postInvalidate();
    updateFromCurrentPlayer();
  }

  private void handleEndOfGame() {
    updateAchievements();
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(getGoBlobActivity())
            .setTitle(getString(R.string.end_of_game_dialog_title))
            .setMessage(getString(R.string.end_of_game_dialog_message))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
//                setResult(RESULT_OK);
//                finish();
              }
            })
            .create()
            .show();
      }
    });
  }

  private void updateAchievements() {
    final GamesClient gamesClient = getGoBlobActivity().getGamesClient();
    gamesClient.unlockAchievement(getString(R.string.achievements_gamers));
    switch (goGame.getBoardSize()) {
      case 9:
        gamesClient.unlockAchievement(getString(R.string.achievements_9x9));
        break;
      case 13:
        gamesClient.unlockAchievement(getString(R.string.achievements_13x13));
        break;
      case 19:
        gamesClient.unlockAchievement(getString(R.string.achievements_19x19));
        break;
    }
    switch (goGame.getOpponent().getType()) {
      case AI:
        gamesClient.unlockAchievement(getString(R.string.achievements_ai));
        break;
      case HUMAN_LOCAL:
        gamesClient.unlockAchievement(getString(R.string.achievements_human));
        break;
    }
  }

  @Override
  public void played(int x, int y) {
    currentPlayerController.play(x, y);
  }

  private void setHumanInteractionEnabled(final boolean enabled) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Enable or Disable Pass Button for Local Humans
        final Button pass_button = (Button) getView().findViewById(R.id.pass_button);
        pass_button.setEnabled(enabled);

        final View boardContainer = getView().findViewById(R.id.boardViewContainer);
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
        Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
        r.play();
      } catch (Exception e) {
        System.err.println("Exception while buzzing");
        e.printStackTrace();
      }
    }
  }
}
