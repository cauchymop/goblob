package com.cauchymop.goblob.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.databinding.BoardviewContainerBinding;
import com.cauchymop.goblob.databinding.MessageAreaBinding;
import com.cauchymop.goblob.databinding.TitleAreaBinding;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.InGameView;
import com.cauchymop.goblob.viewmodel.BoardViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModel;
import com.cauchymop.goblob.viewmodel.PlayerViewModel;

import javax.inject.Inject;

/**
 * Game Page Fragment.
 */
public class InGameViewAndroid extends LinearLayout implements InGameView {

  private static final String TAG = InGameViewAndroid.class.getName();

  @Inject AvatarManager avatarManager;

  private MessageAreaBinding messageAreaBinding;
  private TitleAreaBinding titleAreaBinding;

  private BoardviewContainerBinding boardViewBinding;

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
    setOrientation(LinearLayout.VERTICAL);
    messageAreaBinding = MessageAreaBinding.inflate(LayoutInflater.from(getContext()), this, true);
    titleAreaBinding = TitleAreaBinding.inflate(LayoutInflater.from(getContext()), this, true);
    boardViewBinding = BoardviewContainerBinding.inflate(LayoutInflater.from(getContext()), this, true);
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
    messageAreaBinding.messageTextview.setText(message);
  }

  private void updateActionButton(InGameViewModel inGameViewModel) {
    messageAreaBinding.actionButtonPass.setVisibility(inGameViewModel.isPassActionAvailable() ? VISIBLE : GONE);
    messageAreaBinding.actionButtonPass.setOnClickListener(v -> onPass());
    messageAreaBinding.actionButtonDone.setVisibility(inGameViewModel.isDoneActionAvailable() ? VISIBLE : GONE);
    messageAreaBinding.actionButtonDone.setOnClickListener(v -> onDone());
  }

  private void updateCurrentPlayerView(PlayerViewModel playerViewModel) {
    String playerName = playerViewModel.getPlayerName();
    titleAreaBinding.currentPlayerName.setText(playerName);
    titleAreaBinding.playerColorIcon.setImageResource(playerViewModel.getPlayerColor() == PlayGameData.Color.BLACK ? R.drawable.black_stone : R.drawable.white_stone);
    avatarManager.loadImage(titleAreaBinding.avatarImage, playerName);
  }

  private void updateGoBoardView(BoardViewModel boardViewModel) {
    boardViewBinding.goBoardView.setBoard(boardViewModel);
  }

  @Override
  public void setInGameEventListener(InGameEventListener inGameEventListener) {
    boardViewBinding.goBoardView.setBoardEventListener(inGameEventListener);
    this.inGameEventListener = inGameEventListener;
  }

  void onPass() {
    inGameEventListener.onPass();
  }
  
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
