package com.cauchymop.goblob.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cauchymop.goblob.R;

public class MainActivity extends GoBlobBaseActivity {

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

  public void startChallenges(View v) {
  }

  public void startFreeGame(View v) {
    Intent newGameIntent = new Intent(getApplicationContext(), PlayerChoiceActivity.class);
    startActivity(newGameIntent);
  }

  public void signOut(View v) {
    signOut();
  }

  @Override
  protected void signOut() {
    super.signOut();
    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    findViewById(R.id.sign_out_button).setVisibility(View.GONE);
  }

  @Override
  public void onSignInFailed() {
    super.onSignInFailed();
    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
  }
}
