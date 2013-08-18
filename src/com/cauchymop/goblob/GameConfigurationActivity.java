package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity to create a new game.
 */
public class GameConfigurationActivity extends Activity {

  public static final String EXTRA_OPPONENT = "opponent";
  public static final String EXTRA_BOARD_SIZE = "board_size";
  private Spinner opponentColorSpinner;
  private Spinner yourColorSpinner;
  private EditText yourNameField;
  private EditText opponentNameField;
  private int boardSize;
  private Player opponentPlayer;
  private Player yourPlayer;

  private enum PlayerColor {
    BLACK,
    WHITE;
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_configuration_activity);

    opponentColorSpinner = (Spinner) findViewById(R.id.opponent_color_spinner);
    opponentColorSpinner.setAdapter(new PlayerTypeAdapter());
    opponentColorSpinner.setEnabled(false);

    yourColorSpinner = (Spinner) findViewById(R.id.your_player_color_spinner);
    yourColorSpinner.setAdapter(new PlayerTypeAdapter());
    yourColorSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        opponentColorSpinner.setSelection(1-position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    yourNameField = (EditText)findViewById(R.id.your_player_name);
    opponentNameField = (EditText)findViewById(R.id.opponent_player_name);

    boardSize = getIntent().getExtras().getInt(EXTRA_BOARD_SIZE);
    opponentPlayer = getIntent().getExtras().getParcelable(EXTRA_OPPONENT);

      yourPlayer = new Player(Player.PlayerType.HUMAN, getString(R.string.your_default_name));

      opponentNameField.setText(opponentPlayer.getName());
    yourNameField.setText(yourPlayer.getName());
  }

  public void startGame(View view) {
    opponentPlayer.setName(opponentNameField.getText().toString());
    yourPlayer.setName(yourNameField.getText().toString());

    GoGame goGame;
    switch ((PlayerColor) yourColorSpinner.getSelectedItem()) {
      case BLACK:
        goGame = new GoGame(boardSize, yourPlayer, opponentPlayer);
        break;
      case WHITE:
      default:
        goGame = new GoGame(boardSize, opponentPlayer, yourPlayer);
        break;
    }

    Intent startGameIntent = new Intent(getApplicationContext(), GameActivity.class);
    startGameIntent.putExtra(GameActivity.EXTRA_GAME, goGame);
    startActivity(startGameIntent);
  }

  private class PlayerTypeAdapter extends ArrayAdapter<PlayerColor> {

    public PlayerTypeAdapter() {
      super(GameConfigurationActivity.this, android.R.layout.simple_spinner_item, PlayerColor.values());
    }
  }
}
