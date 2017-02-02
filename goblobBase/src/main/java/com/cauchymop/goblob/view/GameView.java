package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.MovePlayedListener;

public interface GameView {
  void initInGameView(InGameViewModel inGameViewModel);
  void initConfigurationView(ConfigurationViewModel configurationViewModel);
  void setMovePlayedListener(MovePlayedListener movePlayedListener);
  void buzz();
}
