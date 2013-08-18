package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * Activity to create a new game.
 */
public class PlayerChoiceActivity extends Activity {

  private static final String TAG = GoGame.class.getName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.player_choice_activity);

  }

  public void configureGame(View view) {
    Log.i(TAG, "configureGame");
    RadioGroup opponentRadioGroup = (RadioGroup) findViewById(R.id.opponent_radio_group);
    RadioGroup boardSizeRadioGroup = (RadioGroup) findViewById(R.id.board_size_radio_group);

    Player.PlayerType opponentType;
    int boardSize;

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        opponentType = Player.PlayerType.AI;
        break;
      case R.id.opponent_human_local_radio:
      default:
        opponentType = Player.PlayerType.HUMAN;
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
    Player opponentPlayer = new Player(opponentType, getString(R.string.opponent_label));
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_OPPONENT, opponentPlayer);
    configureGameIntent.putExtra(GameConfigurationActivity.EXTRA_BOARD_SIZE, boardSize);
    startActivity(configureGameIntent);
  }

  private class PlayerTypeAdapter extends ArrayAdapter<Player.PlayerType> {

    public PlayerTypeAdapter() {
      super(PlayerChoiceActivity.this, android.R.layout.simple_spinner_item, Player.PlayerType.values());
    }
  }
}
