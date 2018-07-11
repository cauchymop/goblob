package com.cauchymop.goblob.model;

import com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.Move;
import com.cauchymop.goblob.proto.PlayGameData.Score;

public interface Analytics {
  void gameCreated(GameData localGame);

  void configurationChanged(GameData gameData);

  void undo();

  void redo();

  void resign();

  void movePlayed(GameConfiguration gameConfiguration, Move move);

  void deadStoneToggled(GameConfiguration gameConfiguration);

  void invalidMovePlayed(GameConfiguration gameConfiguration);

  void gameFinished(GameConfiguration gameConfiguration, Score score);
}
