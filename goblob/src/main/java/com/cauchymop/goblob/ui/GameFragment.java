package com.cauchymop.goblob.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.presenter.ConfigurationViewModelCreator;
import com.cauchymop.goblob.presenter.GamePresenter;
import com.cauchymop.goblob.view.GameView;
import com.cauchymop.goblob.view.InGameView;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class GameFragment extends GoBlobBaseFragment implements GameView {

  private static final int GAME_CONFIGURATION_VIEW_INDEX = 0;
  private static final int IN_GAME_VIEW_INDEX = 1;

  @Inject
  GameDatas gameDatas;

  @Inject
  AvatarManager avatarManager;

  @Inject
  Analytics analytics;

  @Inject
  GameRepository gameRepository;

  @Inject
  ConfigurationViewModelCreator configurationViewModelCreator;

  @BindView(R.id.current_game_view)
  ViewSwitcher currentGameViewContainer;

  @BindView(R.id.in_game_view)
  InGameViewAndroid inGameView;

  @BindView(R.id.configuration_view)
  GameConfigurationViewAndroid gameConfigurationView;

  private GamePresenter gamePresenter;
  private Unbinder unbinder;
  private boolean undoActionAvailable;
  private boolean redoActionAvailable;
  private boolean resignActionAvailable;

  public static GameFragment newInstance() {
    return new GameFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getComponent().inject(this);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_game, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    gamePresenter = new GamePresenter(gameDatas, analytics, gameRepository,
        new GameMessageGeneratorAndroid(getActivity().getApplicationContext()),
        new AchievementManagerAndroid(getGoBlobActivity()), configurationViewModelCreator, this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    gamePresenter.clear();
    gamePresenter = null;
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.menu_undo).setVisible(undoActionAvailable);
    menu.findItem(R.id.menu_redo).setVisible(redoActionAvailable);
    menu.findItem(R.id.menu_resign).setVisible(resignActionAvailable);
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_undo) {
      gamePresenter.onUndo();
      return true;
    } else if (id == R.id.menu_redo) {
      gamePresenter.onRedo();
      return true;
    } else if (id == R.id.menu_resign) {
      gamePresenter.onResign();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void setConfigurationViewModel(ConfigurationViewModel configurationViewModel) {
    gameConfigurationView.setConfigurationModel(configurationViewModel);
    currentGameViewContainer.setDisplayedChild(GAME_CONFIGURATION_VIEW_INDEX);
  }

  @Override
  public void setInGameViewModel(InGameViewModel inGameViewModel) {
    undoActionAvailable = inGameViewModel.isUndoActionAvailable();
    redoActionAvailable = inGameViewModel.isRedoActionAvailable();
    resignActionAvailable = inGameViewModel.isResignActionAvailable();
    inGameView.setInGameModel(inGameViewModel);
    currentGameViewContainer.setDisplayedChild(IN_GAME_VIEW_INDEX);
  }

  @Override
  public void setInGameActionListener(InGameView.InGameActionListener inGameActionListener) {
    inGameView.setInGameActionListener(inGameActionListener);
  }

  @Override
  public void setConfigurationViewListener(ConfigurationEventListener configurationEventListener) {
    gameConfigurationView.setConfigurationViewListener(configurationEventListener);
  }

  @Override
  public void buzz() {
    try {
      Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Ringtone r = RingtoneManager.getRingtone(getContext().getApplicationContext(), notification);
      r.play();
    } catch (Exception e) {
      System.err.println("Exception while buzzing");
      e.printStackTrace();
    }
  }
}
