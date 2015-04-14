package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.model.GoBoard;
import com.cauchymop.goblob.model.GoGameController;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.GoPlayer.PlayerType;
import com.google.android.gms.games.Player;

import static com.cauchymop.goblob.proto.PlayGameData.Color;

/**
 * Home Page Fragment.
 */
public class GameConfigurationFragment extends GoBlobBaseFragment {

  public static final String EXTRA_OPPONENT = "opponent";
  public static final String EXTRA_BOARD_SIZE = "board_size";
  public static final String LOCAL_PARTICIPANT_ID = "local";
  private Spinner opponentColorSpinner;
  private Spinner homePlayerColorSpinner;
  private EditText homePlayerNameField;
  private EditText opponentNameField;
  private int boardSize;
  private GoPlayer opponentPlayer;
  private GoPlayer homePlayer;
  private EditText handicapText;
  private EditText komiText;

  public static GameConfigurationFragment newInstance(GoPlayer opponent, int boardSize) {
    GameConfigurationFragment instance = new GameConfigurationFragment();

    Bundle args = new Bundle();
    args.putSerializable(EXTRA_OPPONENT, opponent);
    args.putInt(EXTRA_BOARD_SIZE, boardSize);
    instance.setArguments(args);

    return instance;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_game_configuration, container, false);
    opponentColorSpinner = (Spinner) v.findViewById(R.id.opponent_color_spinner);
    opponentColorSpinner.setAdapter(new PlayerTypeAdapter());
    opponentColorSpinner.setEnabled(false);

    homePlayerColorSpinner = (Spinner) v.findViewById(R.id.home_player_color_spinner);
    homePlayerColorSpinner.setAdapter(new PlayerTypeAdapter());
    homePlayerColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        opponentColorSpinner.setSelection(1 - position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    homePlayerNameField = (EditText) v.findViewById(R.id.home_player_name);
    opponentNameField = (EditText) v.findViewById(R.id.opponent_player_name);

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

    homePlayer = new GoPlayer(PlayerType.LOCAL, LOCAL_PARTICIPANT_ID, getString(R.string.home_player_default_name));

    opponentNameField.setText(opponentPlayer.getName());
    homePlayerNameField.setText(homePlayer.getName());

    handicapText = (EditText) v.findViewById(R.id.handicap_value);
    komiText = (EditText) v.findViewById(R.id.komi_value);

    Button startGameButton = (Button) v.findViewById(R.id.start_game_button);
    startGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startGame();
      }
    });
    return v;
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

  private void configureCurrentPlayerFromGooglePlusAccount() {
    if (isSignedIn()) {
      final Player currentPlayer = getGoBlobActivity().getLocalPlayer();
      final String homePlayerName = currentPlayer.getDisplayName();
      homePlayer = new GoPlayer(PlayerType.LOCAL, LOCAL_PARTICIPANT_ID, homePlayerName);
      getGoBlobActivity().getAvatarManager().setAvatarUri(homePlayerName,
          currentPlayer.getIconImageUri());
      homePlayerNameField.setText(homePlayer.getName());
    }
  }

  private void startGame() {
    final Editable opponentNameText = opponentNameField.getText();
    if (opponentNameText != null) {
      opponentPlayer.setName(opponentNameText.toString());
    }

    final Editable homePayerNameText = homePlayerNameField.getText();
    if (homePayerNameText != null) {
      homePlayer.setName(homePayerNameText.toString());
    }

    final Color homePlayerColor = (Color) homePlayerColorSpinner.getSelectedItem();
    String blackId = homePlayerColor == Color.BLACK ? homePlayer.getId() : opponentPlayer.getId();
    String whiteId = homePlayerColor == Color.BLACK ? opponentPlayer.getId() : homePlayer.getId();

    GoGameController goGameController =
        new GoGameController(GameDatas.createGameData(boardSize, GameDatas.DEFAULT_HANDICAP, GameDatas.DEFAULT_KOMI, blackId, whiteId));
    goGameController.setGoPlayer(homePlayerColor, homePlayer);
    goGameController.setGoPlayer(GoBoard.getOpponent(homePlayerColor), opponentPlayer);

    getGoBlobActivity().startLocalGame(goGameController);
  }

  private class PlayerTypeAdapter extends ArrayAdapter<Color> {

    public PlayerTypeAdapter() {
      super(getGoBlobActivity(), android.R.layout.simple_spinner_item, Color.values());
    }
  }
}
