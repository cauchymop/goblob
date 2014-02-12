package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.AvatarManager;
import com.cauchymop.goblob.model.GameMoveSerializer;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoPlayer;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchInitiatedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdatedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;

import static com.cauchymop.goblob.model.Player.PlayerType;

public class MainActivity extends BaseGameActivity implements OnTurnBasedMatchInitiatedListener, OnTurnBasedMatchUpdatedListener{

  public static final int REQUEST_ACHIEVEMENTS = 1;
  public static final int SELECT_PLAYER = 2;
  private static final String TAG = MainActivity.class.getName();
  private int boardSize = 9;
  private GameFragment gameFragment;
  private AvatarManager avatarManager = new AvatarManager();
  private GameMoveSerializer<GoGame> gameMoveSerializer = new GameMoveSerializer<GoGame>();
  private TurnBasedMatch turnBasedMatch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
      HomeFragment homeFragment = new HomeFragment();
      displayFragment(homeFragment, false);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    super.onActivityResult(requestCode, responseCode, intent);
    if (responseCode != Activity.RESULT_OK) {
      Log.w(TAG, "*** select players UI cancelled, " + responseCode);
      return;
    }
    switch (requestCode) {
      case SELECT_PLAYER:
        handleSelectPlayersResult(intent);
        break;
    }
  }

  @Override
  public void onSignInFailed() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignInFailed();
  }

  @Override
  public void onSignInSucceeded() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignInSucceeded();
    if (mHelper.getTurnBasedMatch() != null) {
      Log.d(TAG, "Found match");

      // prevent screen from sleeping during handshake
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      turnBasedMatch = mHelper.getTurnBasedMatch();
    }
  }

  @Override
  protected void signOut() {
    super.signOut();
    onSignOut();
  }

  @Override
  protected boolean isSignedIn() {
    return super.isSignedIn();
  }

  @Override
  protected GamesClient getGamesClient() {
    return super.getGamesClient();
  }

  private GoBlobBaseFragment getCurrentFragment() {
    return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
  }

  private void displayFragment(GoBlobBaseFragment fragment, boolean addToBackStack) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack
    ft.replace(R.id.fragment_container, fragment);

    // Add the transaction to the back stack if needed.
    if (addToBackStack) {
      ft.addToBackStack(null);
    }

    // Commit the transaction
    ft.commit();
  }

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
  protected void beginUserInitiatedSignIn() {
    super.beginUserInitiatedSignIn();
  }

  public void onSignOut() {
    invalidateOptionsMenu();
    getCurrentFragment().onSignOut();
  }

  public void signOut(View v) {
    signOut();
  }

  public void startChallenges(View v) {
  }

  public void startFreeGame(View v) {
    PlayerChoiceFragment playerChoiceFragment = new PlayerChoiceFragment();
    displayFragment(playerChoiceFragment, true);
  }

  public void configureGame(GoPlayer opponentPlayer, int boardSize) {
    this.boardSize = boardSize;
    if (opponentPlayer.getType().isRemote()) {
      Intent selectPlayersIntent = getGamesClient().getSelectPlayersIntent(1, 1);
      startActivityForResult(selectPlayersIntent, SELECT_PLAYER);
    } else {
      displayGameConfigurationScreen(opponentPlayer, boardSize);
    }
  }

  public void displayGameConfigurationScreen(GoPlayer opponentPlayer, int boardSize) {
    GameConfigurationFragment gameConfigurationFragment = GameConfigurationFragment.newInstance(opponentPlayer, boardSize);
    displayFragment(gameConfigurationFragment, true);
  }

  public void startGame(GoGame goGame) {
    GameFragment gameFragment = GameFragment.newInstance(goGame);
    displayFragment(gameFragment, true);
  }

  public void giveTurn(GoGame gogame) {
    getGamesClient().takeTurn(this, turnBasedMatch.getMatchId(),
        gameMoveSerializer.serialize(gogame), getOpponentId(turnBasedMatch, getMyId(turnBasedMatch)));
  }

  public AvatarManager getAvatarManager() {
    return avatarManager;
  }

  private void handleSelectPlayersResult(Intent intent) {
    Log.d(TAG, "Select players UI succeeded.");

    // get the invitee list
    final ArrayList<String> invitees = intent.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
    Log.d(TAG, "Invitee count: " + invitees.size());

    // get the automatch criteria
    Bundle autoMatchCriteria = null;
    int minAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
    int maxAutoMatchPlayers = intent.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
    if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
      autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
          minAutoMatchPlayers, maxAutoMatchPlayers, 0);
      Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
    }

    // create game
    TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
        .addInvitedPlayers(invitees)
        .setAutoMatchCriteria(autoMatchCriteria).build();

    // kick the match off
    getGamesClient().createTurnBasedMatch(this, tbmc);
  }

  @Override
  public void onTurnBasedMatchInitiated(int statusCode, TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchInitiated " + statusCode);
    if (statusCode != GamesClient.STATUS_OK) {
      return;
    }
    this.turnBasedMatch = turnBasedMatch;

    GoGame gogame = createGoGame(turnBasedMatch);
    if (turnBasedMatch.getData() == null) {
      Log.d(TAG, "getData is null, saving a new game");
      getGamesClient().takeTurn(this, turnBasedMatch.getMatchId(),
          gameMoveSerializer.serialize(gogame), getMyId(turnBasedMatch));
    }

    // TODO: start activity
    Log.d(TAG, "Game created, starting game activity...");
    startGame(gogame);
  }

  private GoGame createGoGame(TurnBasedMatch turnBasedMatch) {
    String myId = getMyId(turnBasedMatch);
    String opponentId = getOpponentId(turnBasedMatch, myId);

    for (String participantId : turnBasedMatch.getParticipantIds()) {
      Log.i(TAG, String.format(" participant %s: player %s", participantId,
          turnBasedMatch.getParticipant(participantId).getPlayer().getPlayerId()));
    }

    GoGame gogame = new GoGame(turnBasedMatch.getVariant());
    gameMoveSerializer.deserializeTo(turnBasedMatch.getData(), gogame);

    GoPlayer myPlayer = createGoPlayer(turnBasedMatch, myId, PlayerType.HUMAN_LOCAL);
    GoPlayer opponentPlayer =
        createGoPlayer(turnBasedMatch, opponentId, PlayerType.HUMAN_REMOTE_FRIEND);
    if (gogame.getMoveHistory().size() % 2 == 0) {
      gogame.setBlackPlayer(myPlayer);
      gogame.setWhitePlayer(opponentPlayer);
    } else {
      gogame.setBlackPlayer(opponentPlayer);
      gogame.setWhitePlayer(myPlayer);
    }

    return gogame;
  }

  private String getOpponentId(TurnBasedMatch turnBasedMatch, String id) {
    for (String participantId : turnBasedMatch.getParticipantIds()) {
      if (!participantId.equals(id)) {
        return participantId;
      }
    }
    return null;
  }

  private String getMyId(TurnBasedMatch turnBasedMatch) {
    return turnBasedMatch.getParticipantId(getGamesClient().getCurrentPlayerId());
  }

  private GoPlayer createGoPlayer(TurnBasedMatch turnBasedMatch, String creatorId,
      PlayerType humanLocal) {
    Player player = turnBasedMatch.getParticipant(creatorId).getPlayer();
    GoPlayer goPlayer = new GoPlayer(humanLocal, player.getDisplayName());
    avatarManager.setAvatarUri(getApplicationContext(), goPlayer, player.getIconImageUri());
    return goPlayer;
  }

  @Override
  public void onTurnBasedMatchUpdated(int statusCode, TurnBasedMatch turnBasedMatch) {
    Log.d(TAG, "onTurnBasedMatchUpdated " + statusCode);
    this.turnBasedMatch = turnBasedMatch;
    startGame(createGoGame(turnBasedMatch));
  }
}
