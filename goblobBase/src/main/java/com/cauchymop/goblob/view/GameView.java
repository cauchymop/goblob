package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;

public interface GameView {
  void setConfigurationViewModel(ConfigurationViewModel configurationViewModel);
  void setConfigurationViewListener(ConfigurationEventListener configurationEventListener);

  void setInGameViewModel(InGameViewModel inGameViewModel);
  void setInGameActionListener(InGameView.InGameActionListener inGameActionListener);

  void buzz();
  void clear();
}
