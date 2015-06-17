package com.cauchymop.goblob.ui;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.google.protobuf.TextFormat;

import javax.inject.Inject;

/**
 * Class to persist the local game.
 */
public class LocalGameRepository {

  private static final String TAG = LocalGameRepository.class.getName();
  private static final String GAME_DATA = "gameData";
  private static final String BLACK_NAME = "blackName";
  private static final String BLACK_ID = "blackId";
  private static final String WHITE_NAME = "whiteName";
  private static final String WHITE_ID = "whiteId";

  @Inject
  SharedPreferences prefs;
  private GoGameController currentLocalGame;

  public LocalGameRepository() {
  }

  public void saveLocalGame(GoGameController gameController) {
    Log.i(TAG, "saveLocalGame");
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(GAME_DATA, TextFormat.printToString(gameController.getGameData()));
    GoPlayer blackPlayer = gameController.getGoPlayer(PlayGameData.Color.BLACK);
    editor.putString(BLACK_NAME, blackPlayer.getName());
    editor.putString(BLACK_ID, blackPlayer.getId());
    GoPlayer whitePlayer = gameController.getGoPlayer(PlayGameData.Color.WHITE);
    editor.putString(WHITE_NAME, whitePlayer.getName());
    editor.putString(WHITE_ID, whitePlayer.getId());
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
      GoPlayer blackPlayer = new GoPlayer(GoPlayer.PlayerType.LOCAL, prefs.getString(BLACK_ID, null), prefs.getString(BLACK_NAME, null));
      GoPlayer whitePlayer = new GoPlayer(GoPlayer.PlayerType.LOCAL, prefs.getString(WHITE_ID, null), prefs.getString(WHITE_NAME, null));
      currentLocalGame = new GoGameController(gameData, blackPlayer, whitePlayer);

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
