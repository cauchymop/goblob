package com.cauchymop.goblob.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauchymop.goblob.BuildConfig;
import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.MonteCarlo;
import com.cauchymop.goblob.model.StoneColor;
import com.cauchymop.goblob.proto.PlayGameData;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements GoBoardView.Listener {

  private static final String TAG = GoBlobBaseFragment.class.getName();
  private static final String EXTRA_GO_GAME = "GO_GAME";

  private GoGameController goGameController;
  private GoBoardView goBoardView;

  public static GameFragment newInstance(GoGameController gameController) {
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GO_GAME, gameController);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    Log.d(TAG, "onCreate: " + getArguments());
    if (getArguments() != null && getArguments().containsKey(EXTRA_GO_GAME) && this.goGameController == null) {
      this.goGameController = (GoGameController) getArguments().getSerializable(EXTRA_GO_GAME);
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
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    if (BuildConfig.DEBUG) {
      menu.add(Menu.NONE, R.id.menu_imfeelinglucky, Menu.NONE, R.string.imfeelinglucky);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_imfeelinglucky) {
      int bestMove = MonteCarlo.getBestMove(goGameController.getGame(), 1000);
      int boardSize = goGameController.getGameConfiguration().getBoardSize();
      int x = bestMove % boardSize;
      int y = bestMove / boardSize;
      goGameController.playMove(GameDatas.createMove(x, y));
    }
    return super.onOptionsItemSelected(item);
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
    if (goGameController == null) {
      return;
    }

    FrameLayout boardViewContainer = (FrameLayout) getView().findViewById(R.id.boardViewContainer);
    boardViewContainer.removeAllViews();
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGameController.getGame());
    goBoardView.addListener(this);

    // Disable Interactions for Local Humans
    setHumanInteractionEnabled(goGameController.isLocalTurn());

    boardViewContainer.addView(goBoardView);

    Button passButton = (Button) getView().findViewById(R.id.pass_button);
    passButton.setVisibility(View.VISIBLE);

    passButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playMove(GameDatas.createPassMove());
      }
    });

    updateFromGameState();
  }

  private boolean playMove(PlayGameData.Move move) {
    if (goGameController.playMove(move)) {
      updateAchievements();

      if (goGameController.getCurrentPlayer().getType() == GoPlayer.PlayerType.REMOTE) {
        getGoBlobActivity().giveTurn(goGameController);
      }
      getGoBlobActivity().startGame(goGameController);

      return true;
    }

    return false;
  }

  @Override
  public void played(int x, int y) {
    if(!playMove(GameDatas.createMove(x, y))) {
      buzz();
    }
  }

  private void cleanBoardView() {
    if (goBoardView != null) {
      goBoardView.removeListener(this);
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
    final GoPlayer currentPlayer = goGameController.getCurrentPlayer();
    titleView.setText(currentPlayer.getName());
    titleImage.setImageResource(goGameController.getGame().getCurrentColor() == StoneColor.White ? R.drawable.white_stone : R.drawable.black_stone);
    updateAvatar(currentPlayer);
  }

  private void updateAvatar(GoPlayer currentPlayer) {
    ImageView avatarImage = (ImageView) getView().findViewById(R.id.avatarImage);
    getGoBlobActivity().getAvatarManager().loadImage(avatarImage, currentPlayer.getName());
  }

  /**
   * Display a message if needed (other player has passed...), clean the message area otherwise.
   */
  private void updateMessageArea() {
    final String message;
    if (goGameController.getGame().isGameEnd()) {
      message = getString(R.string.end_of_game_message);
    } else if (goGameController.getGame().isLastMovePass()) {
      message = getString(R.string.opponent_passed_message, goGameController.getOpponent().getName());
    } else {
      message = null;
    }

    TextView messageView = (TextView) getView().findViewById(R.id.message_textview);
    messageView.setText(message);
  }

  private void updateAchievements() {
    getGoBlobActivity().unlockAchievement(getString(R.string.achievements_gamers));
    switch (goGameController.getGame().getBoardSize()) {
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
    switch (goGameController.getOpponent().getType()) {
      case LOCAL:
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_human));
        break;
    }
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
