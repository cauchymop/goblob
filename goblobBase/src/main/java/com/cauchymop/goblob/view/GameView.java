package com.cauchymop.goblob.view;


import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel;
import com.cauchymop.goblob.viewmodel.InGameViewModel;

public interface GameView {
  void setConfigurationViewModel(ConfigurationViewModel configurationViewModel);
  void setConfigurationViewListener(ConfigurationEventListener configurationEventListener);

  void setInGameViewModel(InGameViewModel inGameViewModel);
  void setInGameActionListener(InGameView.InGameActionListener inGameActionListener);

  void buzz();
  void clear();
}
