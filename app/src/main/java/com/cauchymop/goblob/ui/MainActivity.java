package com.cauchymop.goblob.ui;

import static com.cauchymop.goblob.model.GameRepositoryKt.NO_MATCH_ID;
import static com.cauchymop.goblob.proto.PlayGameData.GameData;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.databinding.ActivityMainBinding;
import com.cauchymop.goblob.model.AccountStateListener;
import com.cauchymop.goblob.model.CredentialAuthHelper;
import com.cauchymop.goblob.model.GameChangeListener;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GameListListener;
import com.cauchymop.goblob.model.GameSelectionListener;
import com.cauchymop.goblob.model.GoogleAccountManager;
import com.cauchymop.goblob.proto.PlayGameData;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.yura.lobby.client.PushLobbyClient;
import net.yura.lobby.model.Game;

import java.util.List;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity
        implements GameListListener, GameChangeListener, GameSelectionListener, AccountStateListener {

    private static final int RC_SELECT_PLAYER = 1;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CURRENT_MATCH_ID = "CURRENT_MATCH_ID";

    private ActivityMainBinding binding;
    private MatchesAdapter navigationSpinnerAdapter;
    private List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
    private AlertDialog currentDialog;

    @Inject
    GameDatas gameDatas;
    @Inject
    AndroidGameRepository androidGameRepository;
    @Inject
    GoogleAccountManager googleAccountManager;
    @Inject
    CredentialAuthHelper credentialAuthHelper;

    private GameFragment gameFragment;
    private Runnable pendingAction;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    updatePushToken();
                }
                if (pendingAction != null) {
                    pendingAction.run();
                    pendingAction = null;
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - intent = " + getIntent().getExtras());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.appToolbar.setPadding(0, insets.top, 0, 0);
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        ((GoApplication) getApplication()).getComponent().inject(this);

        setUpToolbar();

        androidGameRepository.addGameListListener(this);
        androidGameRepository.addGameChangeListener(this);
        androidGameRepository.addGameSelectionListener(this);
        googleAccountManager.addAccountStateListener(this);

        if (savedInstanceState != null) {
            String currentMatchId = savedInstanceState.getString(CURRENT_MATCH_ID);
            if (currentMatchId != null) {
                androidGameRepository.selectGame(currentMatchId);
            }
        } else {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String gameId = intent.getStringExtra(PushLobbyClient.GAME_ID);
        if (gameId != null) {
            Log.d(TAG, "handleIntent gameId = " + gameId);
            androidGameRepository.selectGame(gameId);
        }
    }

    private void updatePushToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            androidGameRepository.getLobbyClient().sendPushToken(token);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        updateMatchSpinner();
        if (!googleAccountManager.getSignInComplete()) {
            trySilentSignIn();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
        androidGameRepository.removeGameListListener(this);
        androidGameRepository.removeGameChangeListener(this);
        androidGameRepository.removeGameSelectionListener(this);
//    unbinder.unbind();
    }

    private void setUpToolbar() {
        // Set up the action bar to show a dropdown list.
        setSupportActionBar(binding.appToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            navigationSpinnerAdapter = new MatchesAdapter(supportActionBar.getThemedContext(), matchMenuItems);

            binding.toolbarMatchSpinner.setAdapter(navigationSpinnerAdapter);
            binding.toolbarMatchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onMatchItemSelected(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    void onMatchItemSelected(int position) {
        MatchMenuItem item = navigationSpinnerAdapter.getItem(position);
        Log.d(TAG, "onItemSelected: " + item.getMatchId());
        if (item instanceof GameMatchMenuItem || item instanceof LobbyGameMatchMenuItem) {
            ensureNotificationsPermission(() -> androidGameRepository.selectGame(item.getMatchId()));
        } else {
            androidGameRepository.selectGame(item.getMatchId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean signedIn = googleAccountManager.getSignInComplete();
        menu.setGroupVisible(R.id.group_signedIn, signedIn);
        menu.setGroupVisible(R.id.group_signedOut, !signedIn);
        if (com.cauchymop.goblob.BuildConfig.DEBUG) {
            menu.findItem(R.id.menu_clear_cache).setVisible(true);
        }

        boolean isRemoteGame = false;
        PlayGameData.GameData currentGame = androidGameRepository.getCurrentGame();
        if (currentGame != null) {
            isRemoteGame = androidGameRepository.getGameDatas().isRemoteGame(currentGame);
        }
        menu.findItem(R.id.menu_leave_game).setVisible(isRemoteGame);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_signout) {
            signOut();
        } else if (id == R.id.menu_signin) {
            Log.d(TAG, "signIn from menu");
            signIn();
        } else if (id == R.id.menu_change_display_name) {
            showDisplayNameDialog();
            return true;
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.menu_clear_cache) {
            if (com.cauchymop.goblob.BuildConfig.DEBUG) {
                androidGameRepository.clearCache();
            }
        } else if (id == R.id.menu_leave_game) {
            PlayGameData.GameData currentGame = androidGameRepository.getCurrentGame();
            if (currentGame != null) {
                androidGameRepository.leaveGame(currentGame.getMatchId());
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        Log.d(TAG, String.format("onActivityResult requestCode = %d, responseCode = %d", requestCode, responseCode));
        if (requestCode == RC_SELECT_PLAYER) {
            if (responseCode == RESULT_OK) {
//          androidGameRepository.handlePlayersSelected(intent);
            } else {
                setWaitingScreenVisible(false);
            }
        } else {
            Log.e(TAG, "onActivityResult unexpected requestCode " + requestCode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_MATCH_ID, androidGameRepository.getCurrentMatchId());
        super.onSaveInstanceState(outState);
    }

    public void updateUiFromConnectionStatus(boolean isSignInComplete) {
        Log.d(TAG, "updateUiFromConnectionStatus isSignedIn = " + isSignInComplete);
        invalidateOptionsMenu();

        // When initial connection fails, there is no fragment yet.
        GoBlobBaseFragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.updateFromConnectionStatus(isSignInComplete);
        }
    }

    private void setMatchMenuItems(List<MatchMenuItem> newMatchMenuItems) {
        runOnUiThread(() -> {
            matchMenuItems.clear();
            matchMenuItems.addAll(newMatchMenuItems);

            matchMenuItems.add(new CreateNewGameMenuItem(MainActivity.this.getString(R.string.new_game_label)));
            navigationSpinnerAdapter.notifyDataSetChanged();

            String pendingMatchId = androidGameRepository.getPendingMatchId();
            String matchIdToSelect = pendingMatchId != null ? pendingMatchId : androidGameRepository.getCurrentMatchId();
            MainActivity.this.selectMenuItem(matchIdToSelect);
        });
    }

    private List<MatchMenuItem> getMatchMenuItems(Iterable<GameData> gameDataList) {
        List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
        for (GameData gameData : gameDataList) {
            matchMenuItems.add(new GameMatchMenuItem(gameDatas, gameData));
        }
        return matchMenuItems;
    }

    private List<MatchMenuItem> getLobbyMatchMenuItems(Iterable<Game> gameList) {
        List<MatchMenuItem> matchMenuItems = Lists.newArrayList();
        for (Game game : gameList) {
            matchMenuItems.add(new LobbyGameMatchMenuItem(androidGameRepository.getLobbyClient(), game));
        }
        return matchMenuItems;
    }

    private void trySilentSignIn() {
        String webClientId = getString(R.string.web_client_id);
        credentialAuthHelper.startGoogleSignIn(this, webClientId, true,
                (idToken, email) -> {
                    googleAccountManager.onSignInSuccess(email, idToken);
                },
                e -> Log.d(TAG, "Silent sign-in failed", e)
        );
    }

    private void signIn() {
        String webClientId = getString(R.string.web_client_id);
        credentialAuthHelper.startGoogleSignIn(this, webClientId, false,
                (idToken, email) -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    googleAccountManager.onSignInSuccess(email, idToken);
                },
                e -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    googleAccountManager.onSignOut();
                    String message = "No Google accounts found on this device. Please add a Google account in your device Settings and try again.";
                    if (!(e instanceof androidx.credentials.exceptions.NoCredentialException)) {
                        message = "Sign-in error: " + e.getLocalizedMessage();
                    }
                    if (currentDialog != null && currentDialog.isShowing()) {
                        currentDialog.dismiss();
                    }
                    currentDialog = new AlertDialog.Builder(this).setMessage(message)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
        );
    }

    private void signOut() {
        Log.d(TAG, "signOut");
        googleAccountManager.onSignOut();
    }

    private GoBlobBaseFragment getCurrentFragment() {
        return (GoBlobBaseFragment) getSupportFragmentManager().findFragmentById(R.id.current_fragment);
    }

    private void displayFragment(GoBlobBaseFragment fragment) {
        Log.d(TAG, "displayFragment " + fragment.getClass().getSimpleName());
        setWaitingScreenVisible(false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        // Replace whatever is the current_fragment view with this fragment,
        // and add the transaction to the back stack
        ft.replace(R.id.current_fragment, fragment);

        // Commit the transaction
        ft.commitAllowingStateLoss();
    }

    public void configureGame(boolean isLocal) {
        if (isLocal) {
            GameData localGame = androidGameRepository.createNewLocalGame();
            androidGameRepository.selectGame(localGame.getMatchId());
        } else {
            ensureNotificationsPermission(() -> {
                setWaitingScreenVisible(true);
                Log.d(TAG, "Starting getSelectOpponentsIntent");
                boolean success = androidGameRepository.createNewRemoteGame();
                if (!success) {
                    setWaitingScreenVisible(false);
                    Toast.makeText(this, "Not connected to the Lobby. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void ensureNotificationsPermission(Runnable onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingAction = onPermissionGranted;
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (currentDialog != null && currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
                currentDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.notification_permission_title)
                        .setMessage(R.string.notification_permission_message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            onPermissionGranted.run();
                            pendingAction = null;
                        })
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            onPermissionGranted.run();
        }
    }

    @Override
    public void gameListChanged() {
        runOnUiThread(() -> updateMatchSpinner());
    }

    @Override
    public void gameChanged(GameData gameData) {
        runOnUiThread(() -> {
            if (gameData.getGameConfiguration().getGameType() == PlayGameData.GameType.REMOTE) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);
            }
        });
    }

    @Override
    public void gameSelected(GameData gameData) {
        runOnUiThread(() -> {
            Log.d(TAG, "gameSelected gameData = " + (gameData == null ? null : gameData.getMatchId()));
            if (gameData == null) {
                selectMenuItem(NO_MATCH_ID);
                displayFragment(new PlayerChoiceFragment());
                return;
            }

            if (gameDatas.needsApplicationUpdate(gameData)) {
                displayFragment(UpdateApplicationFragment.newInstance());
                return;
            }

            selectMenuItem(gameData.getMatchId());
            displayFragment(getGameFragment());
        });
    }

    @Override
    public void gameSelectionPending(@NonNull String matchId) {
        runOnUiThread(() -> {
            Log.d(TAG, "gameSelectionPending matchId = " + matchId);
            selectMenuItem(matchId);
            setWaitingScreenVisible(true);
        });
    }

    protected GameFragment getGameFragment() {
        if (gameFragment == null) {
            gameFragment = GameFragment.Companion.newInstance();
        }
        return gameFragment;
    }

    private void updateMatchSpinner() {
        Log.d(TAG, "updateMatchSpinner");

        List<MatchMenuItem> newMatchMenuItems = Lists.newArrayList();
        newMatchMenuItems.addAll(getLobbyMatchMenuItems(androidGameRepository.getLobbyGames()));
        newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getMyTurnGames()));
        newMatchMenuItems.addAll(getMatchMenuItems(androidGameRepository.getTheirTurnGames()));
        setMatchMenuItems(newMatchMenuItems);
    }

    /**
     * Selects the given match (or the first one) and return its index.
     */
    private void selectMenuItem(@NonNull String matchId) {
        Log.d(TAG, "selectMenuItem matchId = " + matchId);
        for (int index = 0; index < navigationSpinnerAdapter.getCount(); index++) {
            MatchMenuItem item = navigationSpinnerAdapter.getItem(index);
            if (Objects.equal(item.getMatchId(), matchId)) {
                setSelection(index);
                return;
            }
        }

        Log.d(TAG, String.format("selectMenuItem(%s) didn't find anything; we do nothing (it's probably loading...)", matchId));
    }

    private void setSelection(final int index) {
        runOnUiThread(() -> binding.toolbarMatchSpinner.setSelection(index));
    }

    public void setWaitingScreenVisible(boolean visible) {
        binding.waitingView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void accountStateChanged(boolean isSignInComplete) {
        invalidateOptionsMenu();
        updateUiFromConnectionStatus(isSignInComplete);
    }

    @Override
    public void onLoginSuccess(String userName) {
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this, getString(R.string.login_success, userName), android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onServerError(String message) {
        runOnUiThread(() -> {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAuthError(String message) {
        runOnUiThread(() -> {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
            currentDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.signin_other_error)
                    .setMessage(getString(R.string.auth_error, message))
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        });
    }

    private void showDisplayNameDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        android.widget.EditText input = new android.widget.EditText(this);
        String currentName = androidGameRepository.getLobbyClient().myPlayerName();
        input.setText(currentName);
        input.setSelection(input.getText().length());

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        currentDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.display_name_dialog_title)
                .setMessage("Your current display name is: " + currentName)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        androidGameRepository.getLobbyClient().setNick(newName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

//  private TurnBasedMultiplayerClient getTurnBasedMultiplayerClient() {
//    return Games.getTurnBasedMultiplayerClient(this, googleAccountManager.getSignedInAccount());
//  }
}
