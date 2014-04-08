package com.cauchymop.goblob.ui;

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
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.PlayerController;
import com.cauchymop.goblob.model.StoneColor;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements GoGame.Listener,
    GoBoardView.Listener {

  private static final String TAG = GoBlobBaseFragment.class.getName();
  private static final String EXTRA_GO_GAME = "GO_GAME";

  private GoGame goGame;
  private GoBoardView goBoardView;
  private HumanPlayerController currentPlayerController;

  public static GameFragment newInstance(GoGame goGame) {
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GO_GAME, goGame);
    fragment.setArguments(args);
    return fragment;
  }

  public void setGame(GoGame gogame) {
    this.goGame = gogame;
    initBoardView();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate: " + getArguments());
    if (getArguments() != null && getArguments().containsKey(EXTRA_GO_GAME) && this.goGame == null) {
      this.goGame = (GoGame) getArguments().getSerializable(EXTRA_GO_GAME);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initBoardView();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(TAG, "onDestroyView");
    cleanBoardView();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (goGame != null) {
      goGame.pause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (goGame != null) {
      goGame.resume();
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

  private void initBoardView() {
    if (goGame == null) {
      return;
    }

    FrameLayout boardViewContainer = (FrameLayout) getView().findViewById(R.id.boardViewContainer);
    boardViewContainer.removeAllViews();
    goGame.setBlackController(getController(goGame.getGoPlayer(StoneColor.Black)));
    goGame.setWhiteController(getController(goGame.getGoPlayer(StoneColor.White)));
    goGame.addListener(this);
    goGame.runGame();
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGame);
    goBoardView.addListener(this);

    // Disable Interactions for Local Humans
    setHumanInteractionEnabled(false);

    boardViewContainer.addView(goBoardView);

    Button passButton = (Button) getView().findViewById(R.id.pass_button);
    passButton.setVisibility(View.VISIBLE);

    passButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        currentPlayerController.pass();
      }
    });

    updateFromGameState();
  }

  private void cleanBoardView() {
    if (goBoardView != null) {
      goBoardView.removeListener(this);
    }

    if (goGame != null) {
      goGame.removeListener(this);
    }
  }

  private void updateFromGameState() {
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
    titleImage.setImageResource(goGame.getCurrentColor() == StoneColor.White ? R.drawable.white_stone : R.drawable.black_stone);
    updateAvatar(currentPlayer);
  }

  private void updateAvatar(GoPlayer currentPlayer) {
    ImageView avatarImage = (ImageView) getView().findViewById(R.id.avatarImage);
    final Bitmap avatar = getGoBlobActivity().getAvatarManager().getAvatar(currentPlayer);
    avatarImage.setImageBitmap(avatar);
  }

  /**
   * Display a message if needed (other player has passed...), clean the message area otherwise.
   */
  private void updateMessageArea() {
    final String message;
    if (goGame.isGameEnd()) {
      message = getString(R.string.end_of_game_message);
    } else if (goGame.isLastMovePass()) {
      message = getString(R.string.opponent_passed_message, goGame.getOpponent().getName());
    } else {
      message = null;
    }

    TextView messageView = (TextView) getView().findViewById(R.id.message_textview);
    messageView.setText(message);
  }

  private PlayerController getController(GoPlayer player) {
    switch (player.getType()) {
      case LOCAL:
        return new LocalHumanPlayerController(goGame);
      case REMOTE:
        return new HumanPlayerController(goGame);
      default:
        throw new RuntimeException("Invalid PlayerControler type");
    }
  }

  @Override
  public void gameChanged(GoGame game) {
    if (game.getCurrentPlayer().getType() == GoPlayer.PlayerType.REMOTE) {
      getGoBlobActivity().giveTurn(game);
    }

    // Refresh UI and current controller
    goBoardView.postInvalidate();
    updateFromGameState();
    updateAchievements();
  }

  private void updateAchievements() {
    getGoBlobActivity().unlockAchievement(getString(R.string.achievements_gamers));
    switch (goGame.getBoardSize()) {
      case 9:
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_9x9));
        break;
      case 13:
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_13x13));
        break;
      case 19:
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_19x19));
        break;
    }
    switch (goGame.getOpponent().getType()) {
      case LOCAL:
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_human));
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
