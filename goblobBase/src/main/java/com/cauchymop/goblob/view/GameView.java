package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.MovePlayedListener;

import java.util.concurrent.Callable;

public interface GameView {
  void initInGameView(InGameViewModel inGameViewModel, Callable<Void> continuation);
  void initConfigurationView(ConfigurationViewModel configurationViewModel);
  void setMovePlayedListener(MovePlayedListener movePlayedListener);
}
