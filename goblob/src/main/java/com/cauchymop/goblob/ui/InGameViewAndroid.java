package com.cauchymop.goblob.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.view.InGameView;
import com.cauchymop.goblob.viewmodel.BoardViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModel;
import com.cauchymop.goblob.viewmodel.PlayerViewModel;

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


  @BindView(R.id.action_button_pass) Button actionButtonPass;
  @BindView(R.id.action_button_done) Button actionButtonDone;
  @BindView(R.id.title) TextView titleView;
  @BindView(R.id.titleImage) ImageView titleImage;
  @BindView(R.id.avatarImage) ImageView avatarImage;
  @BindView(R.id.message_textview) TextView messageView;
  @BindView(R.id.go_board_view) GoBoardViewAndroid goBoardView;

  private InGameActionListener inGameActionListener;

  public InGameViewAndroid(Context context) {
    super(context);
    init();
  }

  public InGameViewAndroid(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public InGameViewAndroid(Context context,
      @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init() {
    inflate(getContext(), R.layout.fragment_game_ingame, this);
    ButterKnife.bind(this);
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


//  private void initTitleArea() {
//    final GoPlayer currentPlayer = goGameController.getCurrentPlayer();
//    titleView.setText(currentPlayer.getName());
//    titleImage.setImageResource(goGameController.getCurrentColor() == Color.WHITE ? R.drawable.white_stone : R.drawable.black_stone);
//    avatarManager.loadImage(avatarImage, currentPlayer.getName());
//  }


  @Override
  public void setInGameModel(@NonNull InGameViewModel inGameViewModel) {
    updateGoBoardView(inGameViewModel.getBoardViewModel());
    updateCurrentPlayerView(inGameViewModel.getCurrentPlayerViewModel());
    updateActionButton(inGameViewModel);
    updateMessageArea(inGameViewModel.getMessage());
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
