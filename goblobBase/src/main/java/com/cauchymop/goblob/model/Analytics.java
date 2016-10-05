package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData.GameData;

public interface Analytics {
  void gameCreated(GameData localGame);

  void configurationChanged(GameData gameData);

  void undo();

  void redo();

  void resign();
}
