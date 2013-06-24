package com.cauchymop.goblob;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

public class GameActivity extends Activity {

  private GoGame goGame;
  private GoGameView goGameView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_activity);
    FrameLayout container = (FrameLayout) findViewById(R.id.boardViewContainer);
    goGame = new GoGame(5, new HumanPlayer("Mr Black"), new HumanPlayer("Mr White"));
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
    goGame.pass();
  }

  /**
   * A human player, player using the Android interface.
   */
  public static class HumanPlayer implements Player {

    private String name;

    public HumanPlayer(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void startTurn(Game game) {
      // TODO: implement this.
    }
  }
}
