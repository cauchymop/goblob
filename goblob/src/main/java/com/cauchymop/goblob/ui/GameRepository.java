package com.cauchymop.goblob.ui;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;

import static com.google.android.gms.games.Games.TurnBasedMultiplayer;
import static com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult;

/**
 * Class to persist games.
 */
public class GameRepository implements OnTurnBasedMatchUpdateReceivedListener {

  private static final String TAG = GameRepository.class.getName();
  private static final String GAME_DATA = "gameData";

  private final SharedPreferences prefs;
  private final GameDatas gameDatas;
  private final GoogleApiClient googleApiClient;

  private GameData currentLocalGame;
  private String currentMatchId;

  private AvatarManager avatarManager;
  private Lazy<String> localGoogleIdentity;
  private List<GameRepositoryListener> listeners = Lists.newArrayList();

  @Inject
  public GameRepository(SharedPreferences prefs, GameDatas gameDatas,
      GoogleApiClient googleApiClient, AvatarManager avatarManager, @Named("LocalGoogleIdentity") Lazy<String> localGoogleIdentity) {
    this.prefs = prefs;
    this.gameDatas = gameDatas;
    this.googleApiClient = googleApiClient;
    this.avatarManager = avatarManager;
    this.localGoogleIdentity = localGoogleIdentity;
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
    Log.d(TAG, "publishRemoteGameState: " + gameData);
    String turnParticipantId = gameDatas.getCurrentPlayer(gameData).getId();
    byte[] gameDataBytes = gameData.toByteArray();
    Log.d(TAG, "takeTurn " + turnParticipantId);
    TurnBasedMultiplayer.takeTurn(googleApiClient, gameData.getMatchId(), gameDataBytes, turnParticipantId);
    if (gameData.getPhase() == Phase.FINISHED) {
      TurnBasedMultiplayer.finishMatch(googleApiClient, gameData.getMatchId());
      fireGameSelected(gameData);
    }
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

  public GameData getGameData(TurnBasedMatch turnBasedMatch) {
    try {
      if (turnBasedMatch.getData() == null) {
        String myId = getMyId(turnBasedMatch);
        String opponentId = getOpponentId(turnBasedMatch);
        PlayGameData.GoPlayer blackPlayer = createGoPlayer(turnBasedMatch, myId);
        PlayGameData.GoPlayer whitePlayer = createGoPlayer(turnBasedMatch, opponentId);
        return gameDatas.createGameData(turnBasedMatch.getMatchId(), Phase.INITIAL, turnBasedMatch.getVariant(), GameDatas.DEFAULT_HANDICAP,
            GameDatas.DEFAULT_KOMI, PlayGameData.GameType.REMOTE, blackPlayer, whitePlayer);
      } else {
        GameData gameData = GameData.parseFrom(turnBasedMatch.getData());
        gameData = handleBackwardCompatibility(turnBasedMatch, gameData);
        return gameData;
      }
    } catch (InvalidProtocolBufferException exception) {
      throw new RuntimeException(exception);
    }
  }

  @NonNull
  private GameData handleBackwardCompatibility(TurnBasedMatch turnBasedMatch,
      GameData initialGameData) {
    GameData.Builder gameData = initialGameData.toBuilder();

    // No players
    if (!gameData.getGameConfiguration().hasBlack() || !gameData.getGameConfiguration().hasWhite()) {
      String myId = getMyId(turnBasedMatch);
      String opponentId = getOpponentId(turnBasedMatch);
      Map<String, PlayGameData.GoPlayer> goPlayers = ImmutableMap.of(
          myId, createGoPlayer(turnBasedMatch, myId),
          opponentId, createGoPlayer(turnBasedMatch, opponentId));
      PlayGameData.GameConfiguration gameConfiguration = gameData.getGameConfiguration();

      PlayGameData.GoPlayer blackPlayer = goPlayers.get(gameConfiguration.getBlackId());
      PlayGameData.GoPlayer whitePlayer = goPlayers.get(gameConfiguration.getWhiteId());

      gameData.getGameConfigurationBuilder().setBlack(blackPlayer).setWhite(whitePlayer);
    }

    // No match Id
    if (Strings.isNullOrEmpty(gameData.getMatchId())) {
      gameData.setMatchId(turnBasedMatch.getMatchId());
    }

    // No phase (version < 2)
    if (gameData.getPhase() == Phase.UNKNOWN) {
      final Phase result;
      if (gameData.hasMatchEndStatus()) {
        if(gameData.getMatchEndStatus().getGameFinished()) {
          result = Phase.FINISHED;
        } else {
          result = Phase.DEAD_STONE_MARKING;
        }
      } else {
        result = Phase.IN_GAME;
      }
      gameData.setPhase(result);
    }

    // No turn (version < 2)
    if (!gameData.hasTurn()) {
      if (gameData.hasMatchEndStatus()) {
        gameData.setTurn(gameData.getMatchEndStatus().getTurn());
      } else {
        boolean hasHandicap = gameData.getGameConfiguration().getHandicap() > 0;
        boolean isBlackTurn = gameData.getMoveCount() % 2 == (hasHandicap ? 1 : 0);
        gameData.setTurn(isBlackTurn ? PlayGameData.Color.BLACK : PlayGameData.Color.WHITE);
      }
    }

    // Fill googleId.
    boolean isMyTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    boolean turnIsBlack = gameData.getTurn() == PlayGameData.Color.BLACK;
    boolean iAmBlack = isMyTurn && turnIsBlack || (!isMyTurn && !turnIsBlack);
    PlayGameData.GoPlayer.Builder player = iAmBlack
        ? gameData.getGameConfigurationBuilder().getBlackBuilder()
        : gameData.getGameConfigurationBuilder().getWhiteBuilder();
    player.setGoogleId(localGoogleIdentity.get());

    return gameData.build();
  }

  private @NonNull String getOpponentId(TurnBasedMatch turnBasedMatch) {
    String myId = getMyId(turnBasedMatch);
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(myId)) {
        return participantId;
      }
    }
    throw new RuntimeException("Our TurnBasedMatch should contain 2 players!");
  }

  private String getMyId(TurnBasedMatch turnBasedMatch) {
    return turnBasedMatch.getParticipantId(localGoogleIdentity.get());
  }

  private PlayGameData.GoPlayer createGoPlayer(TurnBasedMatch match, String participantId) {
    PlayGameData.GoPlayer goPlayer;
    Player player = match.getParticipant(participantId).getPlayer();
    goPlayer = gameDatas.createGamePlayer(participantId, player.getDisplayName(), player.getPlayerId());
    avatarManager.setAvatarUri(player.getDisplayName(), player.getIconImageUri());
    return goPlayer;
  }

  @Override
  public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchReceived");
    fireGameListChanged();
    // While the spinner is updating, we can reload the game, that is already displayed.
    if (currentMatchId !=null && currentMatchId.equals(turnBasedMatch.getMatchId())) {
      selectGame(turnBasedMatch);
    }
  }

  @Override
  public void onTurnBasedMatchRemoved(String s) {
    Log.d(TAG, "onTurnBasedMatchRemoved: " + s);
    fireGameListChanged();
  }

  public void selectGame(@NonNull String matchId) {
    currentMatchId = matchId;

    if (matchId.equals(GameDatas.LOCAL_MATCH_ID)) {
      fireGameSelected(getLocalGame());
    } else if (matchId.equals(GameDatas.NEW_GAME_MATCH_ID)) {
      fireGameSelected(null);
    } else {
      TurnBasedMultiplayer.loadMatch(googleApiClient, matchId)
          .setResultCallback(new ResultCallback<com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult>() {
            @Override
            public void onResult(
                @NonNull LoadMatchResult loadMatchResult) {
              selectGame(loadMatchResult.getMatch());
            }
          });
    }
  }

  public void addGameRepositoryListener(GameRepositoryListener listener) {
    listeners.add(listener);
  }

  public void removeGameRepositoryListener(GameRepositoryListener listener) {
    listeners.remove(listener);
  }

  private void selectGame(TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "selectGame turnBaseMatch.getMatchId() = " + turnBasedMatch.getMatchId());
    boolean myTurn = turnBasedMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
    if (myTurn && turnBasedMatch.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
      TurnBasedMultiplayer.finishMatch(googleApiClient, turnBasedMatch.getMatchId());
    }
    fireGameSelected(getGameData(turnBasedMatch));
  }

  private void fireGameListChanged() {
    for (GameRepositoryListener listener : listeners) {
      listener.gameListChanged();
    }
  }

  private void fireGameSelected(GameData gameData) {
    for (GameRepositoryListener listener : listeners) {
      listener.gameSelected(gameData);
    }
  }

  public String getCurrentMatchId() {
    return currentMatchId;
  }

  public interface GameRepositoryListener {
    void gameListChanged();
    void gameSelected(GameData gameData);
  }
}
