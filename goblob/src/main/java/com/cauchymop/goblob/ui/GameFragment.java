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

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.MonteCarlo;
import com.cauchymop.goblob.proto.PlayGameData;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cauchymop.goblob.proto.PlayGameData.Color;
import static com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import static com.cauchymop.goblob.proto.PlayGameData.Move;
import static com.cauchymop.goblob.proto.PlayGameData.Score;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements GoBoardView.Listener {

  private static final String TAG = GameFragment.class.getName();
  private static final String EXTRA_GO_GAME = "GO_GAME";

  private GoGameController goGameController;
  private GoBoardView goBoardView;

  @Inject
  GameRepository gameRepository;
  @Inject GameDatas gameDatas;
  @Inject AvatarManager avatarManager;

  @Bind(R.id.boardViewContainer) FrameLayout boardViewContainer;
  @Bind(R.id.action_button) Button actionButton;
  @Bind(R.id.title) TextView titleView;
  @Bind(R.id.titleImage) ImageView titleImage;
  @Bind(R.id.avatarImage) ImageView avatarImage;
  @Bind(R.id.message_textview) TextView messageView;

  public static GameFragment newInstance(PlayGameData.GameData gameData) {
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GO_GAME, gameData);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getComponent().inject(this);

    setHasOptionsMenu(true);
    Log.d(TAG, "onCreate: " + getArguments());
    if (getArguments() != null && getArguments().containsKey(EXTRA_GO_GAME) && this.goGameController == null) {
      PlayGameData.GameData gameData = (PlayGameData.GameData) getArguments().getSerializable(EXTRA_GO_GAME);
      this.goGameController = new GoGameController(gameDatas, gameData);
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_game, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(TAG, "onDestroyView");
    cleanBoardView();
    ButterKnife.unbind(this);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    if (goGameController.canUndo()) {
      menu.add(Menu.NONE, R.id.menu_undo, Menu.NONE, R.string.undo);
    }
    if (goGameController.canRedo()) {
      menu.add(Menu.NONE, R.id.menu_redo, Menu.NONE, R.string.redo);
    }
    if (goGameController.isLocalTurn()) {
      menu.add(Menu.NONE, R.id.menu_resign, Menu.NONE, R.string.resign);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_undo) {
      if (goGameController.undo()) {
        endTurn();
      }
      return true;
    } else if (id == R.id.menu_redo) {
      if (goGameController.redo()) {
        endTurn();
      }
      return true;
    } else if (id == R.id.menu_resign) {
      goGameController.resign();
      endTurn();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initViews() {
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGameController);
    goBoardView.addListener(this);
    showActionButton();
    boardViewContainer.addView(goBoardView);
    initFromGameState();
    enableInteractions(goGameController.isLocalTurn());
  }

  private void enableInteractions(boolean enabled) {
    actionButton.setEnabled(enabled);
    goBoardView.setClickable(enabled);
  }

  private void showActionButton() {
    switch(goGameController.getPhase()) {
      case IN_GAME:
        showActionButton(R.string.button_pass_label, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            play(gameDatas.createPassMove());
          }
        });
        break;
      case DEAD_STONE_MARKING:
        showActionButton(R.string.button_done_label, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            goGameController.markingTurnDone();
            endTurn();
          }
        });
        break;
      default:
        hideActionButton();
    }
  }

  private void showActionButton(int buttonLabel, View.OnClickListener clickListener) {
    actionButton.setVisibility(View.VISIBLE);
    actionButton.setText(buttonLabel);
    actionButton.setOnClickListener(clickListener);
  }

  private void hideActionButton() {
    actionButton.setVisibility(View.GONE);
  }

  private void endTurn() {
    getGoBlobActivity().endTurn(goGameController.getGameData());
  }


  @Override
  public void played(int x, int y) {
    play(gameDatas.createMove(x, y));
  }

  private void play(Move move) {
    boolean played = goGameController.playMoveOrToggleDeadStone(move);
    if(played) {
      endTurn();
    } else {
      buzz();
    }
  }

  private void cleanBoardView() {
    if (goBoardView != null) {
      goBoardView.removeListener(this);
    }
  }

  private void initFromGameState() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        initTitleArea();
        initMessageArea();
        updateAchievements();
      }
    });
  }

  private void initTitleArea() {
    final GoPlayer currentPlayer = goGameController.getCurrentPlayer();
    titleView.setText(currentPlayer.getName());
    titleImage.setImageResource(goGameController.getCurrentColor() == Color.WHITE ? R.drawable.white_stone : R.drawable.black_stone);
    avatarManager.loadImage(avatarImage, currentPlayer.getName());
  }

  /**
   * Display a message if needed (other player has passed...), clean the message area otherwise.
   */
  private void initMessageArea() {
    final String message;
    if (goGameController.isGameFinished()) {
      Score score = goGameController.getScore();
      if (score.getResigned()) {
        message = getString(R.string.end_of_game_resigned_message, score.getWinner());
      } else {
        message = getString(R.string.end_of_game_message, score.getWinner(), score.getWonBy());
      }
    } else if (goGameController.getPhase() == PlayGameData.GameData.Phase.DEAD_STONE_MARKING) {
      message = getString(R.string.marking_message);
    } else if (goGameController.getGame().isLastMovePass()) {
      message = getString(R.string.opponent_passed_message, goGameController.getOpponent().getName());
    } else {
      message = null;
    }

    messageView.setText(message);
  }

  private void updateAchievements() {
    if (!isSignedIn() || !goGameController.isGameFinished()) {
      return;
    }
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
    if (goGameController.isLocalGame()) {
      getGoBlobActivity().unlockAchievement(getString(R.string.achievements_local));
    } else {
      getGoBlobActivity().unlockAchievement(getString(R.string.achievements_remote));
      if (goGameController.isLocalPlayer(gameDatas.getWinner(goGameController.getGameData()))) {
        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_winner));
      }
    }
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

  private void playMonteCarloMove() {
    int bestMove = MonteCarlo.getBestMove(goGameController.getGame(), 1000);
    int boardSize = goGameController.getGameConfiguration().getBoardSize();
    int x = bestMove % boardSize;
    int y = bestMove / boardSize;
    goGameController.playMoveOrToggleDeadStone(gameDatas.createMove(x, y));
  }
}
