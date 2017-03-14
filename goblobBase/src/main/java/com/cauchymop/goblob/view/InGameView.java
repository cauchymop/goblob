package com.cauchymop.goblob.view;


import com.cauchymop.goblob.model.InGameViewModel;

public interface InGameView {
  void setInGameModel(InGameViewModel inGameViewModel);
  void setInGameActionListener(InGameActionListener inGameActionListener);

  interface InGameActionListener extends GoBoardView.BoardEventListener {
    void onPass();
    void onDone();
  }
}
