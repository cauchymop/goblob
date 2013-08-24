package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_configuration_activity);

    opponentColorSpinner = (Spinner) findViewById(R.id.opponent_color_spinner);
    opponentColorSpinner.setAdapter(new PlayerTypeAdapter());
    opponentColorSpinner.setEnabled(false);

    yourColorSpinner = (Spinner) findViewById(R.id.your_player_color_spinner);
    yourColorSpinner.setAdapter(new PlayerTypeAdapter());
    yourColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        opponentColorSpinner.setSelection(1 - position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    yourNameField = (EditText) findViewById(R.id.your_player_name);
    opponentNameField = (EditText) findViewById(R.id.opponent_player_name);

    final Bundle extras = getIntent().getExtras();
    if (extras != null) {
      boardSize = extras.getInt(EXTRA_BOARD_SIZE);
      opponentPlayer = extras.getParcelable(EXTRA_OPPONENT);
    } else {
      // This should never happen: if extras are null, it means previous activity has not
      // provided the necessary data we need to create a Game => we finish to go back to
      // previous screen.
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    yourPlayer = new Player(Player.PlayerType.HUMAN, getString(R.string.your_default_name));

    opponentNameField.setText(opponentPlayer.getName());
    yourNameField.setText(yourPlayer.getName());
  }

  public void startGame(View view) {
    if (view == null || view.getId() != R.id.start_game_button) {
      return;
    }
    final Editable opponentNameText = opponentNameField.getText();
    if (opponentNameText != null) {
      opponentPlayer.setName(opponentNameText.toString());
    }

    final Editable yourNameText = yourNameField.getText();
    if (yourNameText != null) {
      yourPlayer.setName(yourNameText.toString());
    }

    GoGame goGame;
    final PlayerColor selectedItem = (PlayerColor) yourColorSpinner.getSelectedItem();
    final Bitmap blackAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.black_stone);
    final Bitmap whiteAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.white_stone);
    switch (selectedItem != null ? selectedItem : PlayerColor.BLACK) {
      case BLACK:
        yourPlayer.setAvatar(blackAvatar);
        opponentPlayer.setAvatar(whiteAvatar);
        goGame = new GoGame(boardSize, yourPlayer, opponentPlayer);
        break;
      case WHITE:
      default:
        yourPlayer.setAvatar(whiteAvatar);
        opponentPlayer.setAvatar(blackAvatar);
        goGame = new GoGame(boardSize, opponentPlayer, yourPlayer);
        break;
    }

    Intent startGameIntent = new Intent(getApplicationContext(), GameActivity.class);
    startGameIntent.putExtra(GameActivity.EXTRA_GAME, goGame);
    startActivity(startGameIntent);
  }

  private enum PlayerColor {
    BLACK,
    WHITE
  }

  private class PlayerTypeAdapter extends ArrayAdapter<PlayerColor> {

    public PlayerTypeAdapter() {
      super(GameConfigurationActivity.this, android.R.layout.simple_spinner_item, PlayerColor.values());
    }
  }
}
