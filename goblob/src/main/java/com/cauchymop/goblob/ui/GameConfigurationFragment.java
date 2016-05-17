package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.proto.PlayGameData;
import com.cauchymop.goblob.proto.PlayGameData.GameConfiguration;
import com.cauchymop.goblob.proto.PlayGameData.GameData;
import com.cauchymop.goblob.proto.PlayGameData.GameData.Phase;
import com.cauchymop.goblob.proto.PlayGameData.GoPlayer;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Home Page Fragment.
 */
public class GameConfigurationFragment extends GoBlobBaseFragment {

  private static final String EXTRA_GAME_DATA = "game_configuration";

  @Bind(R.id.configuration_container)
  LinearLayout configurationContainer;
  @Bind(R.id.configuration_message)
  TextView configurationMessage;
  @Bind(R.id.configuration_done_button)
  Button configurationDoneButton;
  @Bind(R.id.black_player_name)
  EditText blackPlayerNameField;
  @Bind(R.id.white_player_name)
  EditText whitePlayerNameField;
  @Bind(R.id.handicap_spinner)
  Spinner handicapSpinner;
  @Bind(R.id.komi_value)
  EditText komiText;
  @Bind(R.id.board_size_radio_group)
  RadioGroup boardSizeRadioGroup;
  @Bind(R.id.board_size_9)
  RadioButton boardSize9;
  @Bind(R.id.board_size_13)
  RadioButton boardSize13;
  @Bind(R.id.board_size_19)
  RadioButton boardSize19;

  @Inject
  GameDatas gameDatas;

  GoPlayer blackPlayer;
  GoPlayer whitePlayer;

  public static GameConfigurationFragment newInstance(GameData gameData) {
    GameConfigurationFragment instance = new GameConfigurationFragment();

    Bundle args = new Bundle();
    args.putSerializable(EXTRA_GAME_DATA, gameData);
    instance.setArguments(args);

    return instance;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getComponent().inject(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_game_configuration, container, false);
    ButterKnife.bind(this, v);

    init(getInitialGameData());

    return v;
  }

  private GameData getInitialGameData() {
    final Bundle extras = getArguments();
    if (extras == null) {
      // This should never happen
      throw new RuntimeException("A GameConfigurationFragment should always be provided " +
          "a PlayGameData.GameConfiguration as EXTRA argument!");
    }
    return (GameData) extras.getSerializable(EXTRA_GAME_DATA);
  }

  private void init(GameData gameData) {
    boolean isLocalTurn = gameDatas.isLocalTurn(gameData);

    setEnabled(configurationContainer, isLocalTurn);
    configurationDoneButton.setVisibility(isLocalTurn ? View.VISIBLE : View.GONE);
    final @StringRes int message;
    if (gameData.getPhase() == Phase.INITIAL) {
      message = R.string.configuration_message_initial;
    } else if (isLocalTurn) {
      message = R.string.configuration_message_accept_or_change;
    } else {
      message = R.string.configuration_message_waiting_for_opponent;
    }
    configurationMessage.setText(message);

    GameConfiguration configuration = gameData.getGameConfiguration();
    blackPlayer = configuration.getBlack();
    whitePlayer = configuration.getWhite();

    blackPlayerNameField.setText(blackPlayer.getName());
    whitePlayerNameField.setText(whitePlayer.getName());
    komiText.setText(String.valueOf(configuration.getKomi()));
    setHandicap(configuration.getHandicap());
    setBoardSize(configuration.getBoardSize());
  }

  private void setEnabled(ViewGroup vg, boolean enable) {
    for (int i = 0; i < vg.getChildCount(); i++) {
      View child = vg.getChildAt(i);
      child.setEnabled(enable);
      if (child instanceof ViewGroup) {
        setEnabled((ViewGroup) child, enable);
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  @OnClick(R.id.configuration_done_button)
  void done() {
    GoPlayer blackPlayer = getBlackPlayer();
    GoPlayer whitePlayer = getWhitePlayer();
    GameConfiguration newGameConfiguration =
        gameDatas.createGameConfiguration(getBoardSize(), getHandicap(), getKomi(),
            getInitialGameType(), blackPlayer, whitePlayer);

    Phase phase = getPhase(getInitialGameData(), newGameConfiguration);

    GameData gameData = gameDatas.createGameData(getInitialMatchId(), phase, newGameConfiguration);
    getGoBlobActivity().endTurn(gameData);
  }

  private int getBoardSize() {
    switch(boardSizeRadioGroup.getCheckedRadioButtonId()) {
      case R.id.board_size_9:
        return 9;
      case R.id.board_size_13:
        return 13;
      case R.id.board_size_19:
        return 19;
      default:
        throw new RuntimeException("No size selected! id = " + boardSizeRadioGroup.getCheckedRadioButtonId());
    }
  }

  private void setBoardSize(int size) {
    boardSize9.setChecked(size == 9);
    boardSize13.setChecked(size == 13);
    boardSize19.setChecked(size == 19);
  }

  private Phase getPhase(GameData initialGame, GameConfiguration newGameConfiguration) {
    if (getInitialGameType() == PlayGameData.GameType.LOCAL
        || isConfigurationAgreed(initialGame, newGameConfiguration)) {
      return Phase.IN_GAME;
    }
    return Phase.CONFIGURATION;
  }

  private boolean isConfigurationAgreed(GameData initialGame,
      GameConfiguration newGameConfiguration) {
    return initialGame.getPhase() == Phase.CONFIGURATION
        && initialGame.getGameConfiguration().equals(newGameConfiguration);
  }

  @OnClick(R.id.swap_players_button)
  void swapPlayers() {
    GoPlayer tempPlayer = whitePlayer;
    whitePlayer = blackPlayer;
    blackPlayer = tempPlayer;

    String tempPlayerName = blackPlayerNameField.getText().toString();
    blackPlayerNameField.setText(whitePlayerNameField.getText().toString());
    whitePlayerNameField.setText(tempPlayerName);
  }

  private int getHandicap() {
    String selectedItem = (String) handicapSpinner.getSelectedItem();
    try {
      return Integer.valueOf(selectedItem);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void setHandicap(int handicap) {
    int index = (handicap == 0 ? 0 : handicap - 1);
    handicapSpinner.setSelection(index);
  }

  private float getKomi() {
    return Float.valueOf(komiText.getText().toString());
  }

  @NonNull
  private String getInitialMatchId() {
    return getInitialGameData().getMatchId();
  }

  private PlayGameData.GameType getInitialGameType() {
    return getInitialGameData().getGameConfiguration().getGameType();
  }

  private GoPlayer getWhitePlayer() {
    final String whitePlayerName = whitePlayerNameField.getText().toString();
    return gameDatas.createGamePlayer(whitePlayer.getId(), whitePlayerName, whitePlayer.getGoogleId());
  }

  private GoPlayer getBlackPlayer() {
    final String blackPayerName = blackPlayerNameField.getText().toString();
    return gameDatas.createGamePlayer(blackPlayer.getId(), blackPayerName, blackPlayer.getGoogleId());
  }

}
