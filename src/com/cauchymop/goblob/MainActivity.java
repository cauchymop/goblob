package com.cauchymop.goblob;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    public void startChallenges(View v) {

    }

    public void startFreeGame(View v) {
        Intent newGameIntent = new Intent(getApplicationContext(), NewGameActivity.class);
        startActivity(newGameIntent);
    }

}
