package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.Analytics;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.GamePresenter;
import com.cauchymop.goblob.presenter.MovePlayedListener;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.view.GameView;

import java.util.concurrent.Callable;

import javax.inject.Inject;

public class GameFragment extends GoBlobBaseFragment implements GameView {

  @Inject
  GameDatas gameDatas;

  @Inject
  Analytics analytics;

  private static final String EXTRA_GO_GAME = "GO_GAME";

  private InGameFragment inGameView;
  private GameConfigurationFragment gameConfigurationView;
  private GamePresenter gamePresenter;

  public static GameFragment newInstance(GameData gameData) {
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GO_GAME, gameData);
    fragment.setArguments(args);
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
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    GameData gameData = (GameData) getArguments().getSerializable(EXTRA_GO_GAME);
    inGameView = InGameFragment.newInstance(gameData);
    gameConfigurationView = GameConfigurationFragment.newInstance(gameData);
    gamePresenter = new GamePresenter(gameDatas, analytics, new GoGameController(gameDatas, gameData, analytics), this);
  }

  @Override
  public void initInGameView(final InGameViewModel inGameViewModel, final Callable<Void> continuation) {
    displaySubView(inGameView);
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        inGameView.setInGameModel(inGameViewModel);
        try {
          continuation.call();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 2000);

  }

  public void displaySubView(Fragment fragment) {
    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.current_game_view, fragment).commitAllowingStateLoss();
  }

  @Override
  public void setMovePlayedListener(MovePlayedListener movePlayedListener) {
    inGameView.setMovePlayedListener(movePlayedListener);
  }

  @Override
  public void initConfigurationView(ConfigurationViewModel configurationViewModel) {
    displaySubView(gameConfigurationView);
    gameConfigurationView.setConfigurationModel(configurationViewModel);
  }
}
