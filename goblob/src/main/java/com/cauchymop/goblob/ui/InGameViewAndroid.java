package com.cauchymop.goblob.ui;

import android.content.Context;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.BoardViewModel;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.model.PlayerViewModel;
import com.cauchymop.goblob.view.InGameView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Game Page Fragment.
 */
public class InGameViewAndroid extends LinearLayout implements InGameView {

  private static final String TAG = InGameViewAndroid.class.getName();

//  @Inject AndroidGameRepository androidGameRepository;
//  @Inject GameDatas gameDatas;
//  @Inject AvatarManager avatarManager;
//  @Inject Analytics analytics;

  final GameDatas gameDatas;
  final AvatarManager avatarManager;


  @BindView(R.id.boardViewContainer) FrameLayout boardViewContainer;
  @BindView(R.id.action_button_pass) Button actionButtonPass;
  @BindView(R.id.action_button_done) Button actionButtonDone;
  @BindView(R.id.title) TextView titleView;
  @BindView(R.id.titleImage) ImageView titleImage;
  @BindView(R.id.avatarImage) ImageView avatarImage;
  @BindView(R.id.message_textview) TextView messageView;

  private GoBoardViewAndroid goBoardView;
  private InGameActionListener inGameActionListener;

  public InGameViewAndroid(Context context, GameDatas gameDatas, AvatarManager avatarManager) {
    super(context);
    this.gameDatas = gameDatas;
    this.avatarManager = avatarManager;
    inflate(getContext(), R.layout.fragment_game_ingame, this);
    ButterKnife.bind(this);
    // TODO: Move BoardView to XML?
    goBoardView = new GoBoardViewAndroid(getContext());
    boardViewContainer.addView(goBoardView);
  }



//  @Override
//  public void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    getComponent().inject(this);
//
//    setHasOptionsMenu(true);
//    Log.d(TAG, "onCreate");
//    if (getArguments() != null && getArguments().containsKey(EXTRA_GO_GAME) && this.goGameController == null) {
//      PlayGameData.GameData gameData = (PlayGameData.GameData) getArguments().getSerializable(EXTRA_GO_GAME);
//      Log.d(TAG, "   onCreate => gameData = " + gameData.getMatchId());
//      this.goGameController = new GoGameController(gameDatas, gameData, analytics);
//    }
//
//  }

//  }
//
//  @Override
//  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//    super.onCreateOptionsMenu(menu, inflater);
//    if (goGameController.canUndo()) {
//      menu.add(Menu.NONE, R.id.menu_undo, Menu.NONE, R.string.undo);
//    }
//    if (goGameController.canRedo()) {
//      menu.add(Menu.NONE, R.id.menu_redo, Menu.NONE, R.string.redo);
//    }
//    if (goGameController.isLocalTurn()) {
//      menu.add(Menu.NONE, R.id.menu_resign, Menu.NONE, R.string.resign);
//    }
//  }
//
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    int id = item.getItemId();
//    if (id == R.id.menu_undo) {
//      if (goGameController.undo()) {
//        endTurn();
//        analytics.undo();
//      }
//      return true;
//    } else if (id == R.id.menu_redo) {
//      if (goGameController.redo()) {
//        analytics.redo();
//        endTurn();
//      }
//      return true;
//    } else if (id == R.id.menu_resign) {
//      goGameController.resign();
//      endTurn();
//      analytics.resign();
//      return true;
//    }
//    return super.onOptionsItemSelected(item);
//  }

//  private void initViews() {
//    showActionButton();
//    initFromGameState();
//    actionButton.setEnabled(goGameController.isLocalTurn());
//  }

//  private void showActionButton() {
//    switch(goGameController.getPhase()) {
//      case IN_GAME:
//        showActionButton(R.string.button_pass_label, new View.OnClickListener() {
//          @Override
//          public void onClick(View v) {
//            goGameController.playMoveOrToggleDeadStone(gameDatas.createPassMove());
//          }
//        });
//        break;
//      case DEAD_STONE_MARKING:
//        showActionButton(R.string.button_done_label, new View.OnClickListener() {
//          @Override
//          public void onClick(View v) {
//            goGameController.markingTurnDone();
//            endTurn();
//          }
//        });
//        break;
//      default:
//        hideActionButton();
//    }
//  }
//
//  private void showActionButton(int buttonLabel, View.OnClickListener clickListener) {
//    actionButton.setVisibility(View.VISIBLE);
//    actionButton.setText(buttonLabel);
//    actionButton.setOnClickListener(clickListener);
//  }
//
//  private void hideActionButton() {
//    actionButton.setVisibility(View.GONE);
//  }

  private void endTurn() {
//    getGoBlobActivity().endTurn(goGameController.buildGameData());
  }


//  private void initFromGameState() {
//    getActivity().runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        initTitleArea();
//        initMessageArea();
//        updateAchievements();
//      }
//    });
//  }

//  private void initTitleArea() {
//    final GoPlayer currentPlayer = goGameController.getCurrentPlayer();
//    titleView.setText(currentPlayer.getName());
//    titleImage.setImageResource(goGameController.getCurrentColor() == Color.WHITE ? R.drawable.white_stone : R.drawable.black_stone);
//    avatarManager.loadImage(avatarImage, currentPlayer.getName());
//  }

//  /**
//   * Display a message if needed (other player has passed...), clean the message area otherwise.
//   */
//  private void initMessageArea() {
//    final String message;
//    if (goGameController.isGameFinished()) {
//      String winnerName = goGameController.getPlayerForColor(goGameController.getScore().getWinner()).getName();
//      if (goGameController.getScore().getResigned()) {
//        message = getString(R.string.end_of_game_resigned_message, winnerName);
//      } else {
//        message = getString(R.string.end_of_game_message, winnerName, goGameController.getScore().getWonBy());
//      }
//    } else if (goGameController.getPhase() == PlayGameData.GameData.Phase.DEAD_STONE_MARKING) {
//      message = getString(R.string.marking_message);
//    } else if (goGameController.getGame().isLastMovePass()) {
//      message = getString(R.string.opponent_passed_message, goGameController.getOpponent().getName());
//    } else {
//      message = null;
//    }
//
//    messageView.setText(message);
//  }

  private void updateAchievements() {
//    if (!isSignedIn() || !goGameController.isGameFinished()) {
//      return;
//    }
//    switch (goGameController.getGame().getBoardSize()) {
//      case 9:
//        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_9x9));
//        break;
//      case 13:
//        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_13x13));
//        break;
//      case 19:
//        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_19x19));
//        break;
//    }
//    if (goGameController.isLocalGame()) {
//      getGoBlobActivity().unlockAchievement(getString(R.string.achievements_local));
//    } else {
//      getGoBlobActivity().unlockAchievement(getString(R.string.achievements_remote));
//      if (goGameController.isLocalPlayer(goGameController.getWinner())) {
//        getGoBlobActivity().unlockAchievement(getString(R.string.achievements_winner));
//      }
//    }
  }

//  private void playMonteCarloMove() {
//    int bestMove = MonteCarlo.getBestMove(goGameController.getGame(), 1000);
//    int boardSize = goGameController.getGameConfiguration().getBoardSize();
//    int x = bestMove % boardSize;
//    int y = bestMove / boardSize;
//    goGameController.playMoveOrToggleDeadStone(gameDatas.createMove(x, y));
//  }

  @Override
  public void setInGameModel(InGameViewModel inGameViewModel) {
    /*
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGameController);
    goBoardView.addListener(this);
    showActionButton();
    boardViewContainer.addView(goBoardView);
    initFromGameState();
    enableInteractions(goGameController.isLocalTurn());
     */

    if (inGameViewModel != null) {
      updateGoBoardView(inGameViewModel.getBoardViewModel());
      updateCurrentPlayerView(inGameViewModel.getCurrentPlayerViewModel());
      updateActionButton(inGameViewModel);
      updateMessageArea(inGameViewModel.getMessage());
    }
  }

  public void updateMessageArea(String message) {
    messageView.setText(message);
  }

  private void updateActionButton(InGameViewModel inGameViewModel) {
    actionButtonPass.setVisibility(inGameViewModel.isPassActionAvailable() ? VISIBLE : GONE);
    actionButtonDone.setVisibility(inGameViewModel.isDoneActionAvailable() ? VISIBLE : GONE);
  }

  private void updateCurrentPlayerView(PlayerViewModel playerViewModel) {
    titleView.setText(playerViewModel.getPlayerName());
  }

  private void updateGoBoardView(BoardViewModel boardViewModel) {
    goBoardView.setBoard(boardViewModel);

//      Log.d(TAG, "   onCreate => gameData = " + gameData.getMatchId());
//      this.goGameController = new GoGameController(gameDatas, gameData, analytics);
  }

  @Override
  public void setInGameActionListener(InGameActionListener inGameActionListener) {
    goBoardView.setBoardEventListener(inGameActionListener);
    this.inGameActionListener = inGameActionListener;
  }

  @OnClick(R.id.action_button_pass)
  void onPass() {
    inGameActionListener.onPass();
  }
  
  @OnClick(R.id.action_button_done)
  void onDone() {
    inGameActionListener.onDone();
  }
  

}
