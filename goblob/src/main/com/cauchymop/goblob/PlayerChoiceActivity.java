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
  private int previouslyCheckedRadioButtonId = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.player_choice_activity);

    opponentRadioGroup = (RadioGroup) findViewById(R.id.opponent_radio_group);
    boardSizeRadioGroup = (RadioGroup) findViewById(R.id.board_size_radio_group);
    updateBoardSizes();
    opponentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i) {
        updateBoardSizes();
      }
    });
  }

  private void updateBoardSizes() {
    // Copy previous Id before it changes to be able to reselect it
    final int rememberedId = previouslyCheckedRadioButtonId;

    // Retains current selection for next switch
    previouslyCheckedRadioButtonId = boardSizeRadioGroup.getCheckedRadioButtonId();

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
      default:
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_9x9), R.id.board_size_9x9);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_13x13), R.id.board_size_13x13);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_19x19), R.id.board_size_19x19);

        // Select default value if none was previously selected
        if (rememberedId == -1 || rememberedId == R.id.board_size_5x5) {
          boardSizeRadioGroup.check(R.id.board_size_9x9);
        } else {
          boardSizeRadioGroup.check(rememberedId);
        }
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
    Player.PlayerType opponentType;
    int boardSize;
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

    switch (boardSizeRadioGroup.getCheckedRadioButtonId()) {
      case R.id.board_size_5x5:
        boardSize = 5;
        break;
      case R.id.board_size_9x9:
        boardSize = 9;
        break;
      case R.id.board_size_13x13:
        boardSize = 13;
        break;
      case R.id.board_size_19x19:
        boardSize = 19;
        break;
      default:
        boardSize = 9;
        break;
    }

    Intent configureGameIntent = new Intent(getApplicationContext(), GameConfigurationActivity.class);

    GoPlayer opponentPlayer = new GoPlayer(opponentType, opponentDefaultName);
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_OPPONENT, opponentPlayer);
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_BOARD_SIZE, boardSize);
    startActivity(configureGameIntent);
  }
}
