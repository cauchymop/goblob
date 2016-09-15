package com.cauchymop.goblob.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
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
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.ui.presenters.GamePresenter;
import com.cauchymop.goblob.ui.views.GameView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Game Page Fragment.
 */
public class GameFragment extends GoBlobBaseFragment implements GameView {

  private static final String TAG = GameFragment.class.getName();
  private static final String EXTRA_GO_GAME = "GO_GAME";

  private GamePresenter gamePresenter;
  private GoBoardView goBoardView;
  private boolean undoMenuItemAvailable = false;
  private boolean redoMenuItemAvailable = false;
  private boolean resignMenuItemAvailable = false;

  @Inject GameDatas gameDatas;
  @Inject AvatarManager avatarManager;

  @BindView(R.id.boardViewContainer) FrameLayout boardViewContainer;
  @BindView(R.id.action_button) Button actionButton;
  @BindView(R.id.title) TextView titleView;
  @BindView(R.id.titleImage) ImageView titleImage;
  @BindView(R.id.avatarImage) ImageView avatarImage;
  @BindView(R.id.message_textview) TextView messageView;

  private Unbinder unbinder;



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
    Log.d(TAG, "onCreate");

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_game, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (getArguments() != null && getArguments().containsKey(EXTRA_GO_GAME)) {
      PlayGameData.GameData gameData = (PlayGameData.GameData) getArguments().getSerializable(EXTRA_GO_GAME);
      Log.d(TAG, "   onCreate => gameData = " + gameData.getMatchId());
      this.gamePresenter = new GamePresenter(gameDatas);
      gamePresenter.startPresenting(gameData, this);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(TAG, "onDestroyView");

    gamePresenter.stopPresenting();
    unbinder.unbind();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.setGroupVisible(R.id.group_ingame, true);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem undoMenuItem = menu.findItem(R.id.menu_undo);
    undoMenuItem.setVisible(undoMenuItemAvailable);

    MenuItem redoMenuItem = menu.findItem(R.id.menu_redo);
    redoMenuItem.setVisible(redoMenuItemAvailable);

    MenuItem resignMenuItem = menu.findItem(R.id.menu_resign);
    resignMenuItem.setVisible(resignMenuItemAvailable);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_undo) {
      gamePresenter.onUndoSelected();
      return true;
    } else if (id == R.id.menu_redo) {
      gamePresenter.onRedoSelected();
      return true;
    } else if (id == R.id.menu_resign) {
      gamePresenter.onResignSelected();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void updateMenu(boolean undoMenuItemAvailable, boolean redoMenuItemAvailable, boolean resignMenuItemAvailable) {
    this.undoMenuItemAvailable = undoMenuItemAvailable;
    this.redoMenuItemAvailable = redoMenuItemAvailable;
    this.resignMenuItemAvailable = resignMenuItemAvailable;
    getActivity().invalidateOptionsMenu();
  }

  @Override
  public void cleanBoardView() {
    if (goBoardView != null) {
      goBoardView.removeListener(this);
    }
  }

  @Override
  public void endTurn(GameData gameData) {
    getGoBlobActivity().endTurn(gameData);
  }

  @Override
  public void initViews(GoGameController goGameController) {
    goBoardView = new GoBoardView(getActivity().getApplicationContext(), goGameController);
    goBoardView.addListener(gamePresenter);
    showActionButton();
    boardViewContainer.addView(goBoardView);
  }

  @Override
  public void unlockAchievements(List<GamePresenter.Achievement> achievements) {
    for (GamePresenter.Achievement achievement : achievements) {
      getGoBlobActivity().unlockAchievement(getAchievementString(achievement));
    }
  }

  @Override
  public void buzz() {
    try {
      Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
      r.play();
    } catch (Exception e) {
      System.err.println("Exception while buzzing");
      e.printStackTrace();
    }
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
            endTurn(goGameController.buildGameData());
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


  private String getAchievementString(GamePresenter.Achievement achievement) {
    @StringRes
    final int achievementStringId;
    switch (achievement) {
      case ACHIEVEMENTS_9X9:
        achievementStringId = R.string.achievements_9x9;
        break;
      case ACHIEVEMENTS_13X13:
        achievementStringId = R.string.achievements_13x13;
        break;
      case ACHIEVEMENTS_19X19:
        achievementStringId = R.string.achievements_19x19;
        break;
      case ACHIEVEMENTS_LOCAL:
        achievementStringId = R.string.achievements_local;
        break;
      case ACHIEVEMENTS_REMOTE:
        achievementStringId = R.string.achievements_remote;
        break;
      case ACHIEVEMENTS_WINNER:
        achievementStringId = R.string.achievements_winner;
        break;
      default:
        throw new RuntimeException("Invalid achievement: " + achievement);
    }
    return getString(achievementStringId);
  }

}
