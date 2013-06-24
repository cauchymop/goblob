package com.cauchymop.goblob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * TODO: set description.
 */
public class NewGameActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_game_activity);
  }

  public void startGame(View view) {
    Intent startGameIntent = new Intent(getApplicationContext(), GameActivity.class);
    startActivity(startGameIntent);
  }
}
