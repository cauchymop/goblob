package com.cauchymop.goblob.view;

import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel;

public interface GameConfigurationView {
  void setConfigurationModel(ConfigurationViewModel configurationViewModel);
  void setConfigurationViewListener(ConfigurationEventListener configurationEventListener);
}
