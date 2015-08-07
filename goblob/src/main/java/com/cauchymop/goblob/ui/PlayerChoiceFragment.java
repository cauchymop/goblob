package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cauchymop.goblob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Home Page Fragment.
 */
public class PlayerChoiceFragment extends GoBlobBaseFragment {

  private static final String TAG = PlayerChoiceFragment.class.getName();
  @Bind(R.id.game_type_radio_group) RadioGroup opponentRadioGroup;
  @Bind(R.id.board_size_radio_group) RadioGroup boardSizeRadioGroup;
  @Bind(R.id.game_type_radio_local) RadioButton localHumanButton;
  @Bind(R.id.game_type_radio_remote) RadioButton remotePlayerRadio;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_player_choice, container, false);
    ButterKnife.bind(this, v);
    localHumanButton.setChecked(true);
    updateBoardSizes();
    return v;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  @OnClick(R.id.configure_game_button)
  void configureGame() {
    getGoBlobActivity().configureGame(isLocal(), getBoardSize());
  }

  private boolean isLocal() {
    switch (opponentRadioGroup.getCheckedRadioButtonId()) {
      default:
      case R.id.game_type_radio_local:
        return true;
      case R.id.game_type_radio_remote:
        return false;
    }
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

    addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_9x9), R.id.board_size_9x9);
    addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_13x13), R.id.board_size_13x13);
    addBoardSizeRadio(layoutParams, getString(R.string.board_size_label_19x19), R.id.board_size_19x19);
    boardSizeRadioGroup.check(R.id.board_size_9x9);
  }

  private void addBoardSizeRadio(LinearLayout.LayoutParams layoutParams, String label, int id) {
    RadioButton newRadioButton = new RadioButton(getActivity());
    newRadioButton.setText(label);
    newRadioButton.setId(id);
    boardSizeRadioGroup.addView(newRadioButton, layoutParams);
  }

  public int getBoardSize() {
    switch (boardSizeRadioGroup.getCheckedRadioButtonId()) {
      default:
      case R.id.board_size_9x9:
        return 9;
      case R.id.board_size_13x13:
        return 13;
      case R.id.board_size_19x19:
        return 19;
    }
  }

  @Override
  public void updateFromConnectionStatus() {
    super.updateFromConnectionStatus();
    updateRemotePlayerRadios();
  }

  private void updateRemotePlayerRadios() {
    remotePlayerRadio.setEnabled(isSignedIn());
    if (remotePlayerRadio.isChecked()) {
      localHumanButton.setChecked(true);
    }
  }
}
