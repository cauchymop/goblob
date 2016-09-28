package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData;

public interface Analytics {
  void gameCreated(PlayGameData.GameData localGame);
}
