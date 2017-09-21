package com.cauchymop.goblob.view;


import com.cauchymop.goblob.viewmodel.InGameViewModel;

public interface InGameView {
  void setInGameModel(InGameViewModel inGameViewModel);
  void setInGameEventListener(InGameEventListener inGameEventListener);

  interface InGameEventListener extends GoBoardView.BoardEventListener {
    void onPass();
    void onDone();
  }
}
