package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Activity to create a new game.
 */
public class NewGameActivity extends Activity {

  private Spinner blackPlayerSpinner;
  private Spinner whitePlayerSpinner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_game_activity);
    blackPlayerSpinner = (Spinner) findViewById(R.id.black_player_spinner);
    blackPlayerSpinner.setAdapter(new PlayerTypeAdapter());
    whitePlayerSpinner = (Spinner) findViewById(R.id.white_player_spinner);
    whitePlayerSpinner.setAdapter(new PlayerTypeAdapter());
  }

  public void startGame(View view) {
    Intent startGameIntent = new Intent(getApplicationContext(), GameActivity.class);
    Player blackPlayer = new Player((Player.PlayerType) blackPlayerSpinner.getSelectedItem(), "Mr Black");
    Player whitePlayer = new Player((Player.PlayerType) whitePlayerSpinner.getSelectedItem(), "Mr White");
    GoGame goGame = new GoGame(5, blackPlayer, whitePlayer);
    startGameIntent.putExtra(GameActivity.EXTRA_GAME, goGame);
    startActivity(startGameIntent);
  }

  private class PlayerTypeAdapter extends ArrayAdapter<Player.PlayerType> {

    public PlayerTypeAdapter() {
      super(NewGameActivity.this, android.R.layout.simple_spinner_item, Player.PlayerType.values());
    }
  }
}
