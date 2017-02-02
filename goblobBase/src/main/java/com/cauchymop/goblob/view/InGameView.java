package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.InGameViewModel;
import com.cauchymop.goblob.presenter.MovePlayedListener;

public interface InGameView {
  void setInGameModel(InGameViewModel inGameViewModel);
  void setMovePlayedListener(MovePlayedListener movePlayedListener);
}
