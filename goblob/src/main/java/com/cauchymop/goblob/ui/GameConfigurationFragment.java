package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;
import com.google.android.gms.games.Player;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.cauchymop.goblob.proto.PlayGameData.Color;

/**
 * Home Page Fragment.
 */
public class GameConfigurationFragment extends GoBlobBaseFragment {

  public static final String EXTRA_OPPONENT = "opponent";
  public static final String EXTRA_BOARD_SIZE = "board_size";
  public static final String LOCAL_PARTICIPANT_ID = "local";

  @Bind(R.id.opponent_color_spinner) Spinner opponentColorSpinner;
  @Bind(R.id.home_player_color_spinner) Spinner homePlayerColorSpinner;
  @Bind(R.id.home_player_name) EditText homePlayerNameField;
  @Bind(R.id.opponent_player_name) EditText opponentNameField;
  @Bind(R.id.handicap_spinner) Spinner handicapSpinner;
  @Bind(R.id.komi_value) EditText komiText;

  private int boardSize;
  private GoPlayer opponentPlayer;
  private GoPlayer homePlayer;

//  public static GameConfigurationFragment newInstance(GoPlayer opponent, int boardSize) {
//    GameConfigurationFragment instance = new GameConfigurationFragment();
//
//    Bundle args = new Bundle();
//    args.putSerializable(EXTRA_OPPONENT, opponent);
//    args.putInt(EXTRA_BOARD_SIZE, boardSize);
//    instance.setArguments(args);
//
//    return instance;
//  }

  public static GameConfigurationFragment newInstance(PlayGameData.GameConfiguration gameConfiguration) {
    GameConfigurationFragment instance = new GameConfigurationFragment();

    Bundle args = new Bundle();
    args.putSerializable(EXTRA_OPPONENT, gameConfiguration.getWhite());
    args.putInt(EXTRA_BOARD_SIZE, gameConfiguration.getBoardSize());
    instance.setArguments(args);

    return instance;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_game_configuration, container, false);
    ButterKnife.bind(this, v);

    opponentColorSpinner.setAdapter(new PlayerColorAdapter());
    opponentColorSpinner.setEnabled(false);

    homePlayerColorSpinner.setAdapter(new PlayerColorAdapter());

    final Bundle extras = getArguments();
    if (extras != null) {
      boardSize = extras.getInt(EXTRA_BOARD_SIZE);
      opponentPlayer = (GoPlayer) extras.getSerializable(EXTRA_OPPONENT);
    } else {
      // This should never happen: if extras are null, it means previous activity has not
      // provided the necessary data we need to create a Game => we finish to go back to
      // previous screen.
      throw new RuntimeException("A GameConfigurationFragment should always be provided boardSize and opponent Player as EXTRA arguments!");
    }

    homePlayer = GameDatas.createLocalGamePlayer(LOCAL_PARTICIPANT_ID, getString(R.string.home_player_default_name));

    opponentNameField.setText(opponentPlayer.getName());
    homePlayerNameField.setText(homePlayer.getName());

    return v;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    configureCurrentPlayerFromGooglePlusAccount();
  }

  @Override
  public void updateFromConnectionStatus() {
    configureCurrentPlayerFromGooglePlusAccount();
  }

  @OnItemSelected(R.id.home_player_color_spinner)
  void onHomePlayerColorSelected(int position) {
    opponentColorSpinner.setSelection(1 - position);
  }

  @OnClick(R.id.start_game_button)
  void startGame() {
    final Editable opponentNameText = opponentNameField.getText();
    if (opponentNameText != null) {
      opponentPlayer = updateName(opponentPlayer, opponentNameText.toString());
    }

    final Editable homePayerNameText = homePlayerNameField.getText();
    if (homePayerNameText != null) {
      homePlayer = updateName(homePlayer, homePayerNameText.toString());
    }

    final Color homePlayerColor = (Color) homePlayerColorSpinner.getSelectedItem();
    GoPlayer blackPlayer = homePlayerColor == Color.BLACK ? homePlayer : opponentPlayer;
    GoPlayer whitePlayer = homePlayerColor == Color.WHITE ? homePlayer : opponentPlayer;

    GameData gameData = GameDatas.createGameData(boardSize, getHandicap(), getKomi(),
        PlayGameData.GameType.LOCAL, blackPlayer, whitePlayer);
    GoGameController goGameController = new GoGameController(gameData, getGoBlobActivity().getLocalGoogleId());

    getGoBlobActivity().startLocalGame(goGameController);
  }

  private void configureCurrentPlayerFromGooglePlusAccount() {
    if (isSignedIn()) {
      final Player currentPlayer = getGoBlobActivity().getLocalPlayer();
      final String homePlayerName = currentPlayer.getDisplayName();
      homePlayer = GameDatas.createLocalGamePlayer(LOCAL_PARTICIPANT_ID, homePlayerName);
      getGoBlobActivity().getAvatarManager().setAvatarUri(homePlayerName,
          currentPlayer.getIconImageUri());
      homePlayerNameField.setText(homePlayer.getName());
    }
  }

  private GoPlayer updateName(GoPlayer player, String name) {
    return player.toBuilder().setName(name).build();
  }

  private int getHandicap() {
    String selectedItem = (String) handicapSpinner.getSelectedItem();
    try {
      return Integer.valueOf(selectedItem);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private float getKomi() {
    return Float.valueOf(komiText.getText().toString());
  }

  private class PlayerColorAdapter extends ArrayAdapter<Color> {

    public PlayerColorAdapter() {
      super(getGoBlobActivity(), android.R.layout.simple_spinner_item, Color.values());
    }
  }
}
