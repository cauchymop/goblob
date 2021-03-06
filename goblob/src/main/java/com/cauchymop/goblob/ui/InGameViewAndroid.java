package com.cauchymop.goblob.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.InGameView;
import com.cauchymop.goblob.viewmodel.BoardViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModel;
import com.cauchymop.goblob.viewmodel.PlayerViewModel;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Game Page Fragment.
 */
public class InGameViewAndroid extends LinearLayout implements InGameView {

  private static final String TAG = InGameViewAndroid.class.getName();

  @Inject AvatarManager avatarManager;

  @BindView(R.id.action_button_pass) Button actionButtonPass;
  @BindView(R.id.action_button_done) Button actionButtonDone;
  @BindView(R.id.current_player_name) TextView currentPLayerNameView;
  @BindView(R.id.player_color_icon) ImageView playerColorIcon;
  @BindView(R.id.avatar_image) ImageView avatarImage;
  @BindView(R.id.message_textview) TextView messageView;
  @BindView(R.id.go_board_view) GoBoardViewAndroid goBoardView;

  private InGameEventListener inGameEventListener;

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
    ((GoApplication)getContext().getApplicationContext()).getComponent().inject(this);
  }


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
    String playerName = playerViewModel.getPlayerName();
    currentPLayerNameView.setText(playerName);
    playerColorIcon.setImageResource(playerViewModel.getPlayerColor() == PlayGameData.Color.BLACK ? R.drawable.black_stone : R.drawable.white_stone);
    avatarManager.loadImage(avatarImage, playerName);
  }

  private void updateGoBoardView(BoardViewModel boardViewModel) {
    goBoardView.setBoard(boardViewModel);
  }

  @Override
  public void setInGameEventListener(InGameEventListener inGameEventListener) {
    goBoardView.setBoardEventListener(inGameEventListener);
    this.inGameEventListener = inGameEventListener;
  }

  @OnClick(R.id.action_button_pass)
  void onPass() {
    inGameEventListener.onPass();
  }
  
  @OnClick(R.id.action_button_done)
  void onDone() {
    inGameEventListener.onDone();
  }


  public void onUndo() {
    inGameEventListener.onUndo();
  }

  public void onRedo() {
    inGameEventListener.onRedo();
  }

  public void onResign() {
    inGameEventListener.onResign();
  }
}
