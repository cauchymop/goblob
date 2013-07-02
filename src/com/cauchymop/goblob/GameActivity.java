package com.cauchymop.goblob;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

public class GameActivity extends Activity {

  public static final String EXTRA_GAME = "Game";
  private GoGameView goGameView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_activity);
    FrameLayout container = (FrameLayout) findViewById(R.id.boardViewContainer);
    GoGame goGame = getIntent().getParcelableExtra(EXTRA_GAME);
    goGameView = new GoGameView(getApplicationContext(), goGame);
    container.addView(goGameView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_menu, menu);
    return true;
  }

  public void pass(View v) {
    goGameView.pass();
  }
}
