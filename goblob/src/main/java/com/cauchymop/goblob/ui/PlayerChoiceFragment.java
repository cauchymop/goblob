package com.cauchymop.goblob.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GoPlayer;
import com.cauchymop.goblob.model.Player;

/**
 * Home Page Fragment.
 */
public class PlayerChoiceFragment extends GoBlobBaseFragment {

  private static final String TAG = PlayerChoiceFragment.class.getName();
  private RadioGroup opponentRadioGroup;
  private RadioGroup boardSizeRadioGroup;
  private boolean previousOpponentChoiceHuman;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_player_choice, container, false);

    opponentRadioGroup = (RadioGroup) v.findViewById(R.id.opponent_radio_group);
    boardSizeRadioGroup = (RadioGroup) v.findViewById(R.id.board_size_radio_group);

    RadioButton localHumanButton = (RadioButton) v.findViewById(R.id.opponent_human_local_radio);
    localHumanButton.setChecked(true);
    previousOpponentChoiceHuman = true;
    updateBoardSizes();

    opponentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i) {
        boolean newOpponentChoiceHuman = isRadioIdHuman(i);
        if (previousOpponentChoiceHuman != newOpponentChoiceHuman) {
          updateBoardSizes();
        }
        previousOpponentChoiceHuman = newOpponentChoiceHuman;
      }
    });

    Button configureGameButton = (Button) v.findViewById(R.id.configure_game_button);
    configureGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getGoBlobActivity().configureGame(getOpponent(), getBoardSize());
      }
    });
    return v;
  }

  private boolean isRadioIdHuman(int id) {
    return id != R.id.opponent_computer_radio;
  }

  @Override
  public void onResume() {
    super.onResume();
    updateRemotePlayerRadios();
  }

  private void updateBoardSizes() {
    // Clear the group to rebuild it depending on the user type
    boardSizeRadioGroup.removeAllViews();

    // layout params to use when adding each radio button
    LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
        RadioGroup.LayoutParams.WRAP_CONTENT,
        RadioGroup.LayoutParams.WRAP_CONTENT);

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_5x5), R.id.board_size_5x5);
        boardSizeRadioGroup.check(R.id.board_size_5x5);
        break;
      case R.id.opponent_human_local_radio:
      case R.id.opponent_human_remote_friend_radio:
      case R.id.opponent_human_remote_random_radio:
      default:
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_9x9), R.id.board_size_9x9);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_13x13), R.id.board_size_13x13);
        addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_19x19), R.id.board_size_19x19);
        boardSizeRadioGroup.check(R.id.board_size_9x9);
        break;
    }
  }

  private void addBoardSizeRadio(LinearLayout.LayoutParams layoutParams, String label, int id) {
    RadioButton newRadioButton = new RadioButton(getActivity());
    newRadioButton.setText(label);
    newRadioButton.setId(id);
    boardSizeRadioGroup.addView(newRadioButton, layoutParams);
  }

  private GoPlayer getOpponent() {
    Player.PlayerType opponentType;
    final String opponentDefaultName;

    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      case R.id.opponent_computer_radio:
        opponentType = Player.PlayerType.AI;
        opponentDefaultName = Build.MODEL;
        break;
      default:
      case R.id.opponent_human_local_radio:
        opponentType = Player.PlayerType.HUMAN_LOCAL;
        opponentDefaultName = getString(R.string.opponent_default_name);
        break;
      case R.id.opponent_human_remote_friend_radio:
        opponentType = Player.PlayerType.HUMAN_REMOTE_FRIEND;
        opponentDefaultName = null;
        break;
      case R.id.opponent_human_remote_random_radio:
        opponentType = Player.PlayerType.HUMAN_REMOTE_RANDOM;
        opponentDefaultName = null;
        break;
    }
    return new GoPlayer(opponentType, opponentDefaultName);
  }

  public int getBoardSize() {
    switch (boardSizeRadioGroup.getCheckedRadioButtonId()) {
      case R.id.board_size_5x5:
        return 5;
      case R.id.board_size_9x9:
        return 9;
      case R.id.board_size_13x13:
        return 13;
      case R.id.board_size_19x19:
        return 19;
      default:
        return 9;
    }
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
    updateRemotePlayerRadios();
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    updateRemotePlayerRadios();
  }

  private void updateRemotePlayerRadios() {
    updateRemotePlayerRadio(R.id.opponent_human_remote_random_radio);
    updateRemotePlayerRadio(R.id.opponent_human_remote_friend_radio);
  }

  private void updateRemotePlayerRadio(int id) {
    RadioButton radioButton = (RadioButton) getView().findViewById(id);
    radioButton.setEnabled(isSignedIn());
    if (radioButton.isChecked()) {
      RadioButton localHumanButton = (RadioButton) getView().findViewById(R.id.opponent_human_local_radio);
      localHumanButton.setChecked(true);
    }
  }
}
