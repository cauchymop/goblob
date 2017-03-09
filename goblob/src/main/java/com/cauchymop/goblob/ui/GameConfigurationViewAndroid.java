package com.cauchymop.goblob.ui;

import android.content.Context;
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
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.view.GameConfigurationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;

/**
 * Home Page Fragment.
 */
public class GameConfigurationViewAndroid extends LinearLayout implements GameConfigurationView {

  @BindView(R.id.configuration_container)
  LinearLayout configurationContainer;
  @BindView(R.id.configuration_done_button)
  Button configurationDoneButton;
  @BindView(R.id.configuration_message)
  TextView configurationMessage;
  @BindView(R.id.black_player_name)
  EditText blackPlayerNameField;
  @BindView(R.id.white_player_name)
  EditText whitePlayerNameField;
  @BindView(R.id.handicap_spinner)
  Spinner handicapSpinner;
  @BindView(R.id.komi_value)
  EditText komiText;
  @BindView(R.id.board_size_radio_group)
  RadioGroup boardSizeRadioGroup;
  @BindView(R.id.board_size_9)
  RadioButton boardSize9;
  @BindView(R.id.board_size_13)
  RadioButton boardSize13;
  @BindView(R.id.board_size_19)
  RadioButton boardSize19;

  private ConfigurationEventListener configurationEventListener;

  public GameConfigurationViewAndroid(Context context) {
    super(context);
    inflate(getContext(), R.layout.fragment_game_configuration, this);
    ButterKnife.bind(this);
  }

  @Override
  public void setConfigurationModel(ConfigurationViewModel configurationViewModel) {
    setBoardSize(configurationViewModel.getBoardSize());
    komiText.setText(String.valueOf(configurationViewModel.getKomi()));
    setHandicap(configurationViewModel.getHandicap());
    blackPlayerNameField.setText(configurationViewModel.getBlackPlayerName());
    whitePlayerNameField.setText(configurationViewModel.getWhitePlayerName());
    configurationMessage.setText(getConfigurationMessage(configurationViewModel.getConfigurationMessage()));
    boolean interactionsEnabled = configurationViewModel.isInteractionsEnabled();
    setEnabled(configurationContainer, interactionsEnabled);
    configurationDoneButton.setVisibility(interactionsEnabled ? View.VISIBLE : View.GONE);

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

  private int getConfigurationMessage(ConfigurationViewModel.ConfigurationMessage configurationMessage) {
    switch (configurationMessage) {
      case INITIAL:
        return R.string.configuration_message_initial;
      case ACCEPT_OR_CHANGE:
        return R.string.configuration_message_accept_or_change;
      case WAITING_FOR_OPPONENT:
        return R.string.configuration_message_waiting_for_opponent;
      default:
        throw new RuntimeException("Invalid Configuration Message: " + configurationMessage);
    }
  }

  @Override
  public void setConfigurationViewListener(ConfigurationEventListener configurationEventListener) {
    this.configurationEventListener = configurationEventListener;
  }

  @OnClick(R.id.configuration_done_button)
  void fireSwapEvent() {
    if (configurationEventListener != null) {
      configurationEventListener.onConfigurationValidationEvent();
    }
  }

  @OnClick(R.id.swap_players_button)
  void fireConfigurationValidationEvent() {
    if (configurationEventListener != null) {
      configurationEventListener.onSwapEvent();
    }
  }

  @OnFocusChange(R.id.black_player_name)
  void fireBlackPlayerNameChanged() {
    if (configurationEventListener != null) {
      configurationEventListener.onBlackPlayerNameChanged(blackPlayerNameField.getText().toString());
    }
  }

  @OnFocusChange(R.id.white_player_name)
  void fireWhitePlayerNameChanged() {
    if (configurationEventListener != null) {
      configurationEventListener.onWhitePlayerNameChanged(whitePlayerNameField.getText().toString());
    }
  }

  @OnFocusChange(R.id.komi_value)
  void fireKomiChanged() {
    if (configurationEventListener != null) {
      configurationEventListener.onKomiChanged(Float.valueOf(komiText.getText().toString()));
    }
  }

  @OnItemSelected(R.id.handicap_spinner)
  void fireHandicapChanged() {
    if (configurationEventListener != null) {
      configurationEventListener.onHandicapChanged(getHandicap());
    }
  }

  @OnClick({R.id.board_size_9, R.id.board_size_13, R.id.board_size_19})
  void fireBoardSizeChanged() {
    if (configurationEventListener != null) {
      configurationEventListener.onBoardSizeChanged(getBoardSize());
    }
  }

  private void setBoardSize(int size) {
    boardSize9.setChecked(size == 9);
    boardSize13.setChecked(size == 13);
    boardSize19.setChecked(size == 19);
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
}
