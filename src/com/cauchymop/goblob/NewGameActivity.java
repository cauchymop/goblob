package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Activity to create a new game.
 */
public class NewGameActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_game_activity);
  }

  public void startGame(View view) {
    Intent startGameIntent = new Intent(getApplicationContext(), GameActivity.class);
    Player blackPlayer = new Player(Player.PlayerType.HUMAN, "Mr Black");
    Player whitePlayer = new Player(Player.PlayerType.HUMAN, "Mr White");
    GoGame goGame = new GoGame(5, blackPlayer, whitePlayer);
    startGameIntent.putExtra(GameActivity.EXTRA_GAME, goGame);
    startActivity(startGameIntent);
  }
}
