package com.cauchymop.goblob.ui;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.protobuf.TextFormat;

import javax.inject.Inject;

import static com.google.android.gms.games.Games.TurnBasedMultiplayer;

/**
 * Class to persist games.
 */
public class GameRepository {

  private static final String TAG = GameRepository.class.getName();
  private static final String GAME_DATA = "gameData";

  private final SharedPreferences prefs;
  private final GameDatas gameDatas;
  private final GoogleApiClient googleApiClient;

  private GameData currentLocalGame;

  @Inject
  public GameRepository(SharedPreferences prefs, GameDatas gameDatas,
      GoogleApiClient googleApiClient) {
    this.prefs = prefs;
    this.gameDatas = gameDatas;
    this.googleApiClient = googleApiClient;
  }

  public void saveGame(GameData gameData) {
    if (gameDatas.isLocalGame(gameData)) {
      saveLocalGame(gameData);
    } else {
      publishRemoteGameState(gameData);
    }
  }

  private void saveLocalGame(GameData gameData) {
    Log.i(TAG, "saveLocalGame");
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(GAME_DATA, TextFormat.printToString(gameData));
    editor.apply();
    currentLocalGame = gameData;
  }

  @Nullable
  public GameData getLocalGame() {
    if (currentLocalGame == null) {
      GameData gameData = loadLocalGameData();
      if (gameData == null) {
        return null;
      }
      currentLocalGame = gameData;
    }
    return currentLocalGame;
  }

  public void publishRemoteGameState(GameData gameData) {
    switch (gameDatas.getMode(gameData)) {
      case START_GAME_NEGOTIATION:
        giveTurn(gameData);
        break;
      case IN_GAME:
        if (gameDatas.isLocalTurn(gameData)) {
          keepTurn(gameData);
        } else {
          giveTurn(gameData);
        }
        break;
      case END_GAME_NEGOTIATION:
        if (gameDatas.isGameFinished(gameData)) {
          finishTurn(gameData);
        } else if (gameDatas.isLocalTurn(gameData)) {
          keepTurn(gameData);
        } else {
          giveTurn(gameData);
        }
        break;
    }
  }
  public void giveTurn(GameData gameData) {
    Log.d(TAG, "giveTurn: " + gameData);
    takeTurn(gameData, gameDatas.getRemotePlayerId(gameData));
    fireGameChange(gameData);
  }

  private void fireGameChange(GameData gameData) {

  }

  public void keepTurn(GameData gameData) {
    Log.d(TAG, "keepTurn: " + gameData);
    takeTurn(gameData, gameDatas.getLocalPlayerId(gameData));
  }

  public void finishTurn(GameData gameData) {
    Log.d(TAG, "finishTurn: " + gameData);
    takeTurn(gameData, gameDatas.getLocalPlayerId(gameData));
    TurnBasedMultiplayer.finishMatch(googleApiClient, gameData.getMatchId());
    fireGameChange(gameData);
  }

  private void takeTurn(GameData gameData, String myId) {
    byte[] gameDataBytes = gameData.toByteArray();
    TurnBasedMultiplayer.takeTurn(googleApiClient, gameData.getMatchId(), gameDataBytes, myId);
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
