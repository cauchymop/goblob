package com.cauchymop.goblob.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameRepository;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.BoardEventListener;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.presenter.GamePresenter;
import com.cauchymop.goblob.view.GameView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class GameFragment extends GoBlobBaseFragment implements GameView {

  @Inject
  GameDatas gameDatas;

  @Inject
  AvatarManager avatarManager;

  @Inject
  Analytics analytics;

  @Inject
  GameRepository gameRepository;

  @BindView(R.id.current_game_view)
  FrameLayout currentGameViewContainer;

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
    inGameView = new InGameViewAndroid(getContext(), gameDatas, avatarManager);
    gameConfigurationView = new GameConfigurationViewAndroid(getContext());
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
  public void initInGameView(final InGameViewModel inGameViewModel) {
    displaySubView(inGameView);
    inGameView.setInGameModel(inGameViewModel);
  }

  public void displaySubView(View view) {
    currentGameViewContainer.removeAllViews();
    currentGameViewContainer.addView(view);
//    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//    ft.replace(R.id.current_game_view, fragment).commitAllowingStateLoss();

  }

  @Override
  public void setMovePlayedListener(BoardEventListener boardEventListener) {
    inGameView.setMovePlayedListener(boardEventListener);
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

  @Override
  public void setConfigurationViewModel(ConfigurationViewModel configurationViewModel) {
    gameConfigurationView.setConfigurationModel(configurationViewModel);
  }

  @Override
  public void initConfigurationView(ConfigurationViewModel configurationViewModel) {
    displaySubView(gameConfigurationView);
    setConfigurationViewModel(configurationViewModel);
  }
}
