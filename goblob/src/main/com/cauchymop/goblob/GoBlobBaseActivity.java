package com.cauchymop.goblob;

import android.view.Menu;
import android.view.MenuItem;

import com.google.example.games.basegameutils.BaseGameActivity;

/**
 * Created by olivierbonal on 8/26/13.
 */
public class GoBlobBaseActivity extends BaseGameActivity {

  private static final int REQUEST_ACHIEVEMENTS = 1;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.game_menu, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean signedIn = isSignedIn();
    menu.setGroupVisible(R.id.group_signedIn, signedIn);
    menu.setGroupVisible(R.id.group_signedOut, !signedIn);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_achievements) {
      startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
      return true;
    } else if (id == R.id.menu_signout) {
      signOut();
    } else if (id == R.id.menu_signin) {
      beginUserInitiatedSignIn();
    }
    return false;
  }

  @Override
  public void onSignInFailed() {
    invalidateOptionsMenu();
  }

  @Override
  public void onSignInSucceeded() {
    invalidateOptionsMenu();
  }

  @Override
  protected void signOut() {
    super.signOut();
    onSignOut();
  }

  public void onSignOut() {
    invalidateOptionsMenu();
  }
}
