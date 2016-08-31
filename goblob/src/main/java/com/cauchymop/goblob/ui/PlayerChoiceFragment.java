package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cauchymop.goblob.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Home Page Fragment.
 */
public class PlayerChoiceFragment extends GoBlobBaseFragment {

  private static final String TAG = PlayerChoiceFragment.class.getName();
  @BindView(R.id.game_type_radio_group) RadioGroup opponentRadioGroup;
  @BindView(R.id.game_type_radio_local) RadioButton localHumanButton;
  @BindView(R.id.game_type_radio_remote) RadioButton remotePlayerRadio;
  private Unbinder unbinder;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_player_choice, container, false);
    unbinder = ButterKnife.bind(this, v);
    localHumanButton.setChecked(true);
    return v;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @OnClick(R.id.configure_game_button)
  void configureGame() {
    getGoBlobActivity().configureGame(isLocal());
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
