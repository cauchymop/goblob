package com.cauchymop.goblob.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.GamesClient;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements Game.Listener,
    GoBoardView.Listener {

  private static final String TAG = GoBlobBaseFragment.class.getName();

  private GoGame goGame;
  private GoBoardView goBoardView;
  private HumanPlayerController currentPlayerController;

  public static GameFragment newInstance() {
    return new GameFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (goGame != null) {
      initBoardView(goGame);
    }
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

  public void setGoGame(GoGame goGame) {
    this.goGame = goGame;
    if ( getView() != null) {
      initBoardView(goGame);
      // Disable Interactions for Local Humans
      setHumanInteractionEnabled(false);
    }
  }

  private void initBoardView(GoGame goGame) {
    FrameLayout boardViewContainer = (FrameLayout) getView().findViewById(R.id.boardViewContainer);
    boardViewContainer.removeAllViews();
    goGame.setBlackController(getController(goGame.getBlackPlayer()));
    goGame.setWhiteController(getController(goGame.getWhitePlayer()));
    goGame.addListener(this);
    goGame.runGame();
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGame);
    goBoardView.addListener(this);
    boardViewContainer.addView(goBoardView);

    Button passButton = (Button) getView().findViewById(R.id.pass_button);
    passButton.setVisibility(View.VISIBLE);
    passButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        currentPlayerController.pass();
      }
    });

    updateFromCurrentPlayer();
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
    final GoPlayer currentPlayer = goGame.getCurrentPlayer();
    titleView.setText(currentPlayer.getName());
    titleImage.setImageResource(currentPlayer.getStoneColor() == StoneColor.White ? R.drawable.white_stone : R.drawable.black_stone);
    updateAvatar(currentPlayer);
  }

  private void updateAvatar(GoPlayer currentPlayer) {
    ImageView avatarImage = (ImageView) getView().findViewById(R.id.avatarImage);
    final Bitmap avatar = currentPlayer.getAvatar();
    avatarImage.setImageBitmap(avatar);
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
    switch (player.getType()) {
      case AI:
        return new AIPlayerController(goGame);
      case HUMAN_LOCAL:
        return new LocalHumanPlayerController(goGame);
      case HUMAN_REMOTE_FRIEND:
        return new RemoteHumanPlayerController(goGame, getGoBlobActivity().getMessageManager());
      case HUMAN_REMOTE_RANDOM:
      default:
        return getPlayerController();
    }
  }

  private PlayerController getPlayerController() {
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

    if (goGame.getCurrentPlayer().getType() == Player.PlayerType.HUMAN_REMOTE_FRIEND) {
      getGoBlobActivity().getMessageManager().sendMove(goGame.getLastMove());
    }
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
  public void played(int move) {
    currentPlayerController.play(move);
  }

  private void setHumanInteractionEnabled(final boolean enabled) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Enable or Disable Pass Button for Local Humans
        final Button pass_button = (Button) getView().findViewById(R.id.pass_button);
        pass_button.setEnabled(enabled);
        goBoardView.setClickable(enabled);
      }
    });
  }

  private class LocalHumanPlayerController extends HumanPlayerController {

    public LocalHumanPlayerController(GoGame game) {
      super(game);
    }

    @Override
    public void startTurn() {
      // Enable Interactions for Local Humans
      setHumanInteractionEnabled(true);

      super.startTurn();

      // Disable Interactions for Local Humans
      setHumanInteractionEnabled(false);
    }

    @Override
    protected void handleInvalidMove() {
      super.handleInvalidMove();
      buzz();
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

  private class RemoteHumanPlayerController extends HumanPlayerController
      implements MessageManager.MovePlayedListener {
    public RemoteHumanPlayerController(GoGame goGame, MessageManager messageManager) {
      super(goGame);
      messageManager.addMovePlayedListener(this);
    }
  }

  public class HumanPlayerController extends PlayerController {
    protected GoGame game;
    private boolean played;

    public HumanPlayerController(GoGame game) {
      this.game = game;
    }

    @Override
    public void startTurn() {
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
    }

    public void play(int move) {
      if (!played && game.play(this, move)) {
        played = true;
        synchronized (this) {
          this.notifyAll();
        }
      } else {
        handleInvalidMove();
      }
    }

    public void pass() {
      if (!played && game.pass(this)) {
        played = true;
        synchronized (this) {
          this.notifyAll();
        }
      } else {
        handleInvalidMove();
      }
    }

    protected void handleInvalidMove() {
    }
  }
}
