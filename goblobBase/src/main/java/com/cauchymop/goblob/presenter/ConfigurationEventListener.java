package com.cauchymop.goblob.presenter;

public interface ConfigurationEventListener {
  void onBlackPlayerNameChanged(String blackPlayerName);
  void onWhitePlayerNameChanged(String whitePlayerName);
  void onHandicapChanged(int handicap);
  void onKomiChanged(float komi);
  void onBoardSizeChanged(int boardSize);
  void onSwapEvent();
  void onConfigurationValidationEvent();
}
