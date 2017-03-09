package com.cauchymop.goblob.view;

import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;

public interface GameConfigurationView {
  void setConfigurationModel(ConfigurationViewModel configurationViewModel);
  void setConfigurationViewListener(ConfigurationEventListener configurationEventListener);
}
