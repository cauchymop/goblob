package com.cauchymop.goblob;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Activity to create a new game.
 */
public class PlayerChoiceActivity extends GoBlobBaseActivity {

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
    Intent configureGameIntent = new Intent(getApplicationContext(), GameConfigurationActivity.class);
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_OPPONENT, opponentPlayer);
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_BOARD_SIZE, boardSize);
    startActivity(configureGameIntent);
  }

  private GoPlayer getOpponent() {
    Player.PlayerType opponentType;
    final String opponentDefaultName;

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        opponentType = Player.PlayerType.AI;
        opponentDefaultName = Build.MODEL;
        break;
      case R.id.opponent_human_local_radio:
      default:
        opponentType = Player.PlayerType.HUMAN_LOCAL;
        opponentDefaultName = getString(R.string.opponent_default_name);
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
}
