package com.cauchymop.goblob.ui;

import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.cauchymop.goblob.model.GoGame;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.Player.PlayerType;
import com.cauchymop.goblob.model.StoneColor;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Player;

/**
 * Home Page Fragment.
 */
public class GameConfigurationFragment extends GoBlobBaseFragment {

  public static final String EXTRA_OPPONENT = "opponent";
  public static final String EXTRA_BOARD_SIZE = "board_size";
  private Spinner opponentColorSpinner;
  private Spinner homePlayerColorSpinner;
  private EditText homePlayerNameField;
  private EditText opponentNameField;
  private int boardSize;
  private GoPlayer opponentPlayer;
  private GoPlayer homePlayer;

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

    homePlayer = new GoPlayer(PlayerType.HUMAN_LOCAL, getString(R.string.home_player_default_name));

    opponentNameField.setText(opponentPlayer.getName());
    homePlayerNameField.setText(homePlayer.getName());

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
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    configureCurrentPlayerFromGooglePlusAccount();
  }

  private void configureCurrentPlayerFromGooglePlusAccount() {
    if (isSignedIn()) {
      homePlayer = getHomePlayer();
      homePlayerNameField.setText(homePlayer.getName());
    }
  }

  public GoPlayer getHomePlayer() {
    final Player currentPlayer = getGoBlobActivity().getGamesClient().getCurrentPlayer();
    final String homePlayerName = currentPlayer.getDisplayName();
    final GoPlayer homePlayer = new GoPlayer(PlayerType.HUMAN_LOCAL, homePlayerName);
    getGoBlobActivity().getAvatarManager().setAvatarUri(getActivity(), homePlayer,
        currentPlayer.getIconImageUri());
    return homePlayer;
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

    GoGame goGame;
    final PlayerColor homePlayerColor = (PlayerColor) homePlayerColorSpinner.getSelectedItem();
    final GoPlayer blackPlayer, whitePlayer;
    switch (homePlayerColor != null ? homePlayerColor : PlayerColor.BLACK) {
      case BLACK:
        blackPlayer = homePlayer;
        whitePlayer = opponentPlayer;
        break;
      case WHITE:
      default:
        blackPlayer = opponentPlayer;
        whitePlayer = homePlayer;
        break;
    }

    blackPlayer.setStoneColor(StoneColor.Black);
    whitePlayer.setStoneColor(StoneColor.White);
    goGame = new GoGame(boardSize, blackPlayer, whitePlayer);

    getGoBlobActivity().startGame(goGame);
  }

  private enum PlayerColor {
    BLACK,
    WHITE
  }

  private class PlayerTypeAdapter extends ArrayAdapter<PlayerColor> {

    public PlayerTypeAdapter() {
      super(getGoBlobActivity(), android.R.layout.simple_spinner_item, PlayerColor.values());
    }
  }
}
