package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.Move;

public interface Analytics {
  void gameCreated(GameData localGame);

  void configurationChanged(GameData gameData);

  void undo();

  void redo();

  void resign();

  void movePlayed(GameConfiguration gameConfiguration, Move move, Phase phase);

  void invalidMovePlayed(GameConfiguration gameConfiguration);
}
