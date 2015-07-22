package com.cauchymop.goblob.ui;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.google.protobuf.TextFormat;

import javax.inject.Inject;

/**
 * Class to persist the local game.
 */
public class LocalGameRepository {

  private static final String TAG = LocalGameRepository.class.getName();
  private static final String GAME_DATA = "gameData";

  @Inject
  SharedPreferences prefs;
  private GoGameController currentLocalGame;

  public LocalGameRepository() {
  }

  public void saveLocalGame(GoGameController gameController) {
    Log.i(TAG, "saveLocalGame");
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(GAME_DATA, TextFormat.printToString(gameController.getGameData()));
    editor.apply();
    currentLocalGame = gameController;
  }

  @Nullable
  public GoGameController getLocalGame() {
    if (currentLocalGame == null) {
      GameData gameData = loadLocalGameData();
      if (gameData == null) {
        return null;
      }
      currentLocalGame = new GoGameController(gameData, null);
    }
    return currentLocalGame;
  }

  @Nullable
  private GameData loadLocalGameData() {
    Log.i(TAG, "loadLocalGame");
    String gameDataString = prefs.getString(GAME_DATA, null);
    if (gameDataString == null) {
      return null;
    }
    GameData.Builder gameDataBuilder = GameData.newBuilder();
    try {
      TextFormat.merge(gameDataString, gameDataBuilder);
    } catch (TextFormat.ParseException e) {
      Log.e(TAG, "Error parsing local GameData: " + e.getMessage());
      return null;
    }
    return gameDataBuilder.build();
  }
}
