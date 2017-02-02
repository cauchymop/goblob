package com.cauchymop.goblob.ui;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.ConfigurationViewModel;
import com.cauchymop.goblob.view.GameConfigurationView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Home Page Fragment.
 */
public class GameConfigurationViewAndroid extends LinearLayout implements GameConfigurationView {

  @BindView(R.id.configuration_container)
  LinearLayout configurationContainer;
  @BindView(R.id.configuration_message)
  TextView configurationMessage;
  @BindView(R.id.configuration_done_button)
  Button configurationDoneButton;
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

  public GameConfigurationViewAndroid(Context context) {
    super(context);
    inflate(getContext(), R.layout.fragment_game_configuration, this);
    ButterKnife.bind(this);
  }

  @Override
  public void setConfigurationModel(ConfigurationViewModel configurationViewModel) {
    komiText.setText(String.valueOf(configurationViewModel.getKomi()));

  }
}
