package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.MovePlayedListener;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.view.GameView;

public class GameFragment extends GoBlobBaseFragment implements GameView {

  private static final String EXTRA_GO_GAME = "GO_GAME";

  private InGameFragment inGameView;
  private GameConfigurationFragment gameConfigurationView;

  public static GameFragment newInstance(PlayGameData.GameData gameData) {
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GO_GAME, gameData);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PlayGameData.GameData gameData = (PlayGameData.GameData) getArguments().getSerializable(EXTRA_GO_GAME);
    inGameView = InGameFragment.newInstance(gameData);
    gameConfigurationView = GameConfigurationFragment.newInstance(gameData);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // TODO:
    // When InGameView and ConfigurationView will simply be custom Views and not fragments,
    // we will simply instantiate and return the appropriate one form the given state/viewModel (gameData for now)
    return inflater.inflate(R.layout.fragment_game, container, false);
  }




  @Override
  public void initInGameView(InGameViewModel inGameViewModel) {
    displaySubView(inGameView);
    inGameView.setInGameModel(inGameViewModel);
  }

  public void displaySubView(Fragment fragment) {
    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.current_fragment, fragment).commitAllowingStateLoss();
  }

  @Override
  public void setMovePlayedListener(MovePlayedListener movePlayedListener) {
    inGameView.setMovePlayedListener(movePlayedListener);
  }

  @Override
  public void initConfigurationView(ConfigurationViewModel configurationViewModel) {
    displaySubView(gameConfigurationView);
    //TODO:
    //gameConfigurationView.setXXXModel
  }
}
