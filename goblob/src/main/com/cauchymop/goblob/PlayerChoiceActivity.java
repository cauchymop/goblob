package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to create a new game.
 */
public class PlayerChoiceActivity extends GoBlobBaseActivity implements RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener {

  private static final String TAG = PlayerChoiceActivity.class.getName();

  private RadioGroup opponentRadioGroup;
  private RadioGroup boardSizeRadioGroup;
  private boolean previousOpponentChoiceHuman;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.player_choice_activity);

    opponentRadioGroup = (RadioGroup) findViewById(R.id.opponent_radio_group);
    boardSizeRadioGroup = (RadioGroup) findViewById(R.id.board_size_radio_group);

    RadioButton localHumanButton = (RadioButton) findViewById(R.id.opponent_human_local_radio);
    localHumanButton.setChecked(true);
    previousOpponentChoiceHuman = true;
    updateBoardSizes();

    opponentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i) {
        boolean newOpponentChoiceHuman = isRadioIdHuman(i);
        if (previousOpponentChoiceHuman != newOpponentChoiceHuman) {
          updateBoardSizes();
        }
        previousOpponentChoiceHuman = newOpponentChoiceHuman;
      }
    });
  }

  private boolean isRadioIdHuman(int id) {
    return id != R.id.opponent_computer_radio;
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateRemotePlayerRadios();
  }

  private void updateBoardSizes() {
    // Clear the group to rebuild it depending on the user type
    boardSizeRadioGroup.removeAllViews();

    // layout params to use when adding each radio button
    LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
        RadioGroup.LayoutParams.WRAP_CONTENT,
        RadioGroup.LayoutParams.WRAP_CONTENT);

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_5x5), R.id.board_size_5x5);
        boardSizeRadioGroup.check(R.id.board_size_5x5);
        break;
      case R.id.opponent_human_local_radio:
      case R.id.opponent_human_remote_friend_radio:
      case R.id.opponent_human_remote_random_radio:
      default:
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_9x9), R.id.board_size_9x9);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_13x13), R.id.board_size_13x13);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_19x19), R.id.board_size_19x19);
        boardSizeRadioGroup.check(R.id.board_size_9x9);
        break;
    }
  }

  private void addBoardSizeRadio(LinearLayout.LayoutParams layoutParams, String label, int id) {
    RadioButton newRadioButton = new RadioButton(this);
    newRadioButton.setText(label);
    newRadioButton.setId(id);
    boardSizeRadioGroup.addView(newRadioButton, layoutParams);
  }

  public void configureGame(View view) {
    if (view == null || view.getId() != R.id.configure_game_button) {
      return;
    }
    GoPlayer opponentPlayer = getOpponent();
    int boardSize = getBoardSize();

    if (opponentPlayer.getType().isRemote()) {
      Intent selectPlayersIntent = getGamesClient().getSelectPlayersIntent(1, 1);
      startActivityForResult(selectPlayersIntent, GoBlobBaseActivity.SELECT_PLAYER);
    } else {
      Intent configureGameIntent = new Intent(getApplicationContext(), GameConfigurationActivity.class);
      configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_OPPONENT, opponentPlayer);
      configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_BOARD_SIZE, boardSize);
      startActivity(configureGameIntent);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    super.onActivityResult(requestCode, responseCode, intent);
    switch (requestCode) {
      case SELECT_PLAYER:
        handleSelectPlayersResult(responseCode, intent);
        break;
    }
  }

  private void handleSelectPlayersResult(int response, Intent intent) {
    if (response != Activity.RESULT_OK) {
      Log.w(TAG, "*** select players UI cancelled, " + response);
      return;
    }

    Log.d(TAG, "Select players UI succeeded.");

    // get the invitee list
    final ArrayList<String> invitees = intent.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
    Log.d(TAG, "Invitee count: " + invitees.size());

    // get the automatch criteria
    Bundle autoMatchCriteria = null;
    int minAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
    int maxAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
    if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
      autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
          minAutoMatchPlayers, maxAutoMatchPlayers, 0);
      Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
    }

    // create the room
    Log.d(TAG, "Creating room...");
    RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
    rtmConfigBuilder.addPlayersToInvite(invitees);
    rtmConfigBuilder.setMessageReceivedListener(this);
    rtmConfigBuilder.setRoomStatusUpdateListener(this);
    if (autoMatchCriteria != null) {
      rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
    }
    getGamesClient().createRoom(rtmConfigBuilder.build());
    Log.d(TAG, "Room created, waiting for it to be ready...");
  }

  private GoPlayer getOpponent() {
    Player.PlayerType opponentType;
    final String opponentDefaultName;

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        opponentType = Player.PlayerType.AI;
        opponentDefaultName = Build.MODEL;
        break;
      default:
      case R.id.opponent_human_local_radio:
        opponentType = Player.PlayerType.HUMAN_LOCAL;
        opponentDefaultName = getString(R.string.opponent_default_name);
        break;
      case R.id.opponent_human_remote_friend_radio:
        opponentType = Player.PlayerType.HUMAN_REMOTE_FRIEND;
        opponentDefaultName = null;
        break;
      case R.id.opponent_human_remote_random_radio:
        opponentType = Player.PlayerType.HUMAN_REMOTE_RANDOM;
        opponentDefaultName = null;
        break;
    }
    return new GoPlayer(opponentType, opponentDefaultName);
  }

  private int getBoardSize() {
    switch (boardSizeRadioGroup.getCheckedRadioButtonId()) {
      case R.id.board_size_5x5:
        return 5;
      case R.id.board_size_9x9:
        return 9;
      case R.id.board_size_13x13:
        return 13;
      case R.id.board_size_19x19:
        return 19;
      default:
        return 9;
    }
  }

  @Override
  public void onSignOut() {
    super.onSignInFailed();
    updateRemotePlayerRadios();
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    updateRemotePlayerRadios();
  }

  private void updateRemotePlayerRadios() {
    updateRemotePlayerRadio(R.id.opponent_human_remote_random_radio);
    updateRemotePlayerRadio(R.id.opponent_human_remote_friend_radio);
  }

  private void updateRemotePlayerRadio(int id) {
    RadioButton radioButton = (RadioButton) findViewById(id);
    radioButton.setEnabled(isSignedIn());
    if (radioButton.isChecked()) {
      RadioButton localHumanButton = (RadioButton) findViewById(R.id.opponent_human_local_radio);
      localHumanButton.setChecked(true);
    }
  }

  @Override
  public void onRoomCreated(int statusCode, Room room) {
    Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
  }

  @Override
  public void onJoinedRoom(int statusCode, Room room) {
    Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
  }

  @Override
  public void onLeftRoom(int statusCode, String roomId) {
    Log.d(TAG, "onLeftRoom(" + statusCode + ", " + roomId + ")");
  }

  @Override
  public void onRoomConnected(int statusCode, Room room) {
    Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
  }

  @Override
  public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
    Log.d(TAG, "onRealTimeMessageReceived(" + realTimeMessage + ")");
  }

  @Override
  public void onRoomConnecting(Room room) {
    Log.d(TAG, "onRoomConnecting(" + room + ")");
  }

  @Override
  public void onRoomAutoMatching(Room room) {
    Log.d(TAG, "onRoomAutoMatching(" + room + ")");
  }

  @Override
  public void onPeerInvitedToRoom(Room room, List<String> strings) {
    Log.d(TAG, "onPeerInvitedToRoom(" + room + ", " + strings + ")");
  }

  @Override
  public void onPeerDeclined(Room room, List<String> strings) {
    Log.d(TAG, "onPeerDeclined(" + room + ", " + strings + ")");
  }

  @Override
  public void onPeerJoined(Room room, List<String> strings) {
    Log.d(TAG, "onPeerJoined(" + room + ", " + strings + ")");
  }

  @Override
  public void onPeerLeft(Room room, List<String> strings) {
    Log.d(TAG, "onPeerLeft(" + room + ", " + strings + ")");
  }

  @Override
  public void onConnectedToRoom(Room room) {
    Log.d(TAG, "onConnectedToRoom(" + room + ")");
  }

  @Override
  public void onDisconnectedFromRoom(Room room) {
    Log.d(TAG, "onDisconnectedFromRoom(" + room + ")");
  }

  @Override
  public void onPeersConnected(Room room, List<String> strings) {
    Log.d(TAG, "onPeersConnected(" + room + ", " + strings + ")");
  }

  @Override
  public void onPeersDisconnected(Room room, List<String> strings) {
    Log.d(TAG, "onPeersDisconnected(" + room + ", " + strings + ")");
  }
}
