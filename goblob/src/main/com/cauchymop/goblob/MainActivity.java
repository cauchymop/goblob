package com.cauchymop.goblob;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity {

  private static final int REQUEST_ACHIEVEMENTS = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        beginUserInitiatedSignIn();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.menu_achievements) {
      startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
      return true;
    } else if (id == R.id.menu_signout) {
      signOut(null);
    }
    return false;
  }

  public void startChallenges(View v) {
  }

  public void startFreeGame(View v) {
    Intent newGameIntent = new Intent(getApplicationContext(), PlayerChoiceActivity.class);
    startActivity(newGameIntent);
  }

  public void signOut(View v) {
    signOut();
    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    findViewById(R.id.sign_out_button).setVisibility(View.GONE);
  }

  @Override
  public void onSignInFailed() {
    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
  }

  @Override
  public void onSignInSucceeded() {
    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
  }
}
