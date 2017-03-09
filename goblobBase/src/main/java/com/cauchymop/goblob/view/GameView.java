package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.BoardEventListener;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;

public interface GameView {
  void initInGameView(InGameViewModel inGameViewModel);
  void initConfigurationView(ConfigurationViewModel configurationViewModel);
  void setMovePlayedListener(BoardEventListener boardEventListener);
  void setConfigurationViewListener(ConfigurationEventListener configurationEventListener);
  void buzz();
  void clear();
  void setConfigurationViewModel(ConfigurationViewModel configurationViewModel);
}
