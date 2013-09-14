package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.Player;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends BaseGameActivity {

  public static final int REQUEST_ACHIEVEMENTS = 1;
  public static final int SELECT_PLAYER = 2;
  public static final int WAITING_ROOM = 3;
  private static final String TAG = MainActivity.class.getName();
  private Room gameRoom;
  private RoomUpdateListener gameRoomUpdateListener = new RoomUpdateListener() {

    @Override
    public void onRoomCreated(int statusCode, Room room) {
      Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
      MainActivity.this.gameRoom = room;
      Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, 1);
      startActivityForResult(i, WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
      Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
    }

    @Override
    public void onLeftRoom(int statusCode, String roomId) {
      Log.d(TAG, "onLeftRoom(" + statusCode + ", " + roomId + ")");
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
      Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
    }
  };
  private RoomStatusUpdateListener gameRoomStatusListener = new RoomStatusUpdateListener() {
    @Override
    public void onRoomConnecting(Room room) {
      Log.d(TAG, "onRoomConnecting(" + room + ")");
    }

    @Override
    public void onRoomAutoMatching(Room room) {
      Log.d(TAG, "onRoomAutoMatching(" + room + ")");
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {
      Log.d(TAG, "onPeerInvitedToRoom(" + room + ", " + strings + ")");
    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
      Log.d(TAG, "onPeerDeclined(" + room + ", " + strings + ")");
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
      Log.d(TAG, "onPeerJoined(" + room + ", " + strings + ")");
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
      Log.d(TAG, "onPeerLeft(" + room + ", " + strings + ")");
    }

    @Override
    public void onConnectedToRoom(Room room) {
      Log.d(TAG, "onConnectedToRoom(" + room + ")");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
      Log.d(TAG, "onDisconnectedFromRoom(" + room + ")");
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
      Log.d(TAG, "onPeersConnected(" + room + ", " + strings + ")");
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
      Log.d(TAG, "onPeersDisconnected(" + room + ", " + strings + ")");
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    HomeFragment homeFragment = new HomeFragment();
    displayFragment(homeFragment, false);
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
      case WAITING_ROOM:
        handleRoomReady(intent);
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
    if (opponentPlayer.getType().isRemote()) {
      GameFragment gameFragment = startGame();
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

  public GameFragment startGame() {
    GameFragment gameFragment = GameFragment.newInstance();
    displayFragment(gameFragment, true);
    return gameFragment;
  }

  public void startLocalGame(GoGame goGame) {
    GameFragment gameFragment = startGame();
    gameFragment.setGoGame(goGame);
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

    // create the room
    Log.d(TAG, "Creating room...");
    RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(gameRoomUpdateListener);
    rtmConfigBuilder.addPlayersToInvite(invitees);
    GameFragment gameFragment = startGame();
    rtmConfigBuilder.setMessageReceivedListener(gameFragment);
    rtmConfigBuilder.setRoomStatusUpdateListener(gameRoomStatusListener);
    if (autoMatchCriteria != null) {
      rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
    }
    getGamesClient().createRoom(rtmConfigBuilder.build());
    Log.d(TAG, "Room created, waiting for it to be ready...");
  }

  private void handleRoomReady(Intent intent) {
    Log.d(TAG, "Back from waiting room!");
    //    // Only for test purposes.
    if (!isSignedIn() || gameRoom == null) {
      return;
    }
    String myId = getGamesClient().getCurrentPlayerId();
    com.google.android.gms.games.Player opponent = null;
    ArrayList<Participant> participants = gameRoom.getParticipants();
    Iterator<Participant> it = participants.iterator();
    while (it.hasNext()) {
      Participant participant = it.next();
      com.google.android.gms.games.Player player = participant.getPlayer();
      if (!myId.equals(participant.getPlayer().getPlayerId())) {
        opponent = player;
        break;
      }
    }

    if (opponent == null) {
      return;
    }

    final String name = opponent.getDisplayName();
    Uri iconImageUriUri = opponent.getIconImageUri();

    ImageManager.create(this).loadImage(new ImageManager.OnImageLoadedListener() {
      @Override
      public void onImageLoaded(Uri uri, Drawable drawable) {
        GoPlayer opponent = new GoPlayer(GoPlayer.PlayerType.HUMAN_REMOTE_FRIEND, name);
        opponent.setAvatar(drawable);
        displayGameConfigurationScreen(opponent, getBoardSize());
      }

      /**
       * DUMMY FOR TEST ONLY
       * TODO: Find a clean way to do this
       *
       * @return
       */
      private int getBoardSize() {
        GoBlobBaseFragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof PlayerChoiceFragment) {
          return ((PlayerChoiceFragment) currentFragment).getBoardSize();
        }
        return 9;
      }
    }, iconImageUriUri);
  }
}
