package com.cauchymop.goblob.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.presenter.GamePresenter;
import com.cauchymop.goblob.view.GameView;
import com.cauchymop.goblob.view.InGameView;

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

  @BindView(R.id.current_game_view)
  ViewSwitcher currentGameViewContainer;

  private InGameViewAndroid inGameView;
  private GameConfigurationViewAndroid gameConfigurationView;
  private GamePresenter gamePresenter;
  private Unbinder unbinder;

  public static GameFragment newInstance() {
    GameFragment fragment = new GameFragment();
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getComponent().inject(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO:
    // When InGameView and ConfigurationView will simply be custom Views and not fragments,
    // we will simply instantiate and return the appropriate one form the given state/viewModel (gameData for now)
    View view = inflater.inflate(R.layout.fragment_game, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    gameConfigurationView = new GameConfigurationViewAndroid(getContext());
    inGameView = new InGameViewAndroid(getContext(), gameDatas, avatarManager);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    currentGameViewContainer.addView(gameConfigurationView, GAME_CONFIGURATION_VIEW_INDEX, params);
    currentGameViewContainer.addView(inGameView, IN_GAME_VIEW_INDEX, params);
    gamePresenter = new GamePresenter(gameDatas, analytics, gameRepository, this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    gamePresenter.clear();
    gamePresenter = null;
  }

  @Override
  public void setConfigurationViewModel(ConfigurationViewModel configurationViewModel) {
    currentGameViewContainer.setDisplayedChild(GAME_CONFIGURATION_VIEW_INDEX);
    gameConfigurationView.setConfigurationModel(configurationViewModel);
  }

  @Override
  public void setInGameViewModel(InGameViewModel inGameViewModel) {
    currentGameViewContainer.setDisplayedChild(IN_GAME_VIEW_INDEX);
    inGameView.setInGameModel(inGameViewModel);
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

  @Override
  public void clear() {
    // TODO
  }
}
