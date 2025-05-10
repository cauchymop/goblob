package com.cauchymop.goblob.ui;

import android.content.Context;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.databinding.FragmentGameConfigurationBinding;
import com.cauchymop.goblob.databinding.MessageAreaBinding;
import com.cauchymop.goblob.presenter.ConfigurationEventListener;
import com.cauchymop.goblob.view.GameConfigurationView;
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel;
import com.crashlytics.android.Crashlytics;

/**
 * Home Page Fragment.
 */
public class GameConfigurationViewAndroid extends LinearLayout implements GameConfigurationView {

    private static final String TAG = GameConfigurationViewAndroid.class.getName();

    private FragmentGameConfigurationBinding binding;

    private ConfigurationEventListener configurationEventListener;

    public GameConfigurationViewAndroid(Context context) {
        super(context);
        init();
    }

    public GameConfigurationViewAndroid(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameConfigurationViewAndroid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        binding = FragmentGameConfigurationBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    @Override
    public void setConfigurationModel(@NonNull ConfigurationViewModel configurationViewModel) {
        setBoardSize(configurationViewModel.getBoardSize());
        binding.komiValue.setText(String.valueOf(configurationViewModel.getKomi()));
        binding.komiValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                fireKomiChanged();
            }
        });
        setHandicap(configurationViewModel.getHandicap());
        binding.blackPlayerName.setText(configurationViewModel.getBlackPlayerName());
        binding.blackPlayerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                OnBlackPlayerTextChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.whitePlayerName.setText(configurationViewModel.getWhitePlayerName());
        binding.whitePlayerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                OnWhitePlayerTextChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.configurationMessage.setText(configurationViewModel.getMessage());
        boolean interactionsEnabled = configurationViewModel.getInteractionsEnabled();
        setEnabled(binding.configurationContainer, interactionsEnabled);
        binding.configurationDoneButton.setVisibility(interactionsEnabled ? View.VISIBLE : View.GONE);
        binding.configurationDoneButton.setOnClickListener(v -> fireConfigurationValidationEvent());
        binding.swapPlayersButton.setOnClickListener(v -> fireSwapEvent());
        binding.boardSize9.setOnClickListener(v -> fireBoardSizeChanged());
        binding.boardSize13.setOnClickListener(v -> fireBoardSizeChanged());
        binding.boardSize19.setOnClickListener(v -> fireBoardSizeChanged());
        binding.handicapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fireHandicapChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fireHandicapChanged();
            }
        });
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
    public void setConfigurationViewListener(ConfigurationEventListener configurationEventListener) {
        this.configurationEventListener = configurationEventListener;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // We do nothing here a we do not want the System to handle our View States
        // (our presenter will reset the appropriate state).
    }

    void fireConfigurationValidationEvent() {
        if (configurationEventListener != null) {
            Crashlytics.log(Log.DEBUG, TAG, " ==> OnClick configuration_done_button");
            configurationEventListener.onConfigurationValidationEvent();
        }
    }

    void fireSwapEvent() {
        if (configurationEventListener != null) {
            configurationEventListener.onSwapEvent();
        }
    }

    void OnBlackPlayerTextChanged() {
        if (configurationEventListener != null) {
            String blackPlayerName = binding.blackPlayerName.getText().toString();
            configurationEventListener.onBlackPlayerNameChanged(blackPlayerName);
        }
    }

    void OnWhitePlayerTextChanged() {
        if (configurationEventListener != null) {
            String whitePlayerName = binding.whitePlayerName.getText().toString();
            configurationEventListener.onWhitePlayerNameChanged(whitePlayerName);
        }
    }

    void fireKomiChanged() {
        if (configurationEventListener != null) {
            configurationEventListener.onKomiChanged(Float.parseFloat(binding.komiValue.getText().toString()));
        }
    }

    void fireHandicapChanged() {
        if (configurationEventListener != null) {
            configurationEventListener.onHandicapChanged(getHandicap());
        }
    }

    void fireBoardSizeChanged() {
        if (configurationEventListener != null) {
            configurationEventListener.onBoardSizeChanged(getBoardSize());
        }
    }

    private void setBoardSize(int size) {
        binding.boardSize9.setChecked(size == 9);
        binding.boardSize13.setChecked(size == 13);
        binding.boardSize19.setChecked(size == 19);
    }

    private int getBoardSize() {
        int checkedRadioButtonId = binding.boardSizeRadioGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.board_size_9) {
            return 9;
        } else if (checkedRadioButtonId == R.id.board_size_13) {
            return 13;
        } else if (checkedRadioButtonId == R.id.board_size_19) {
            return 19;
        } else {
            throw new RuntimeException("No size selected! id = " + checkedRadioButtonId);
        }
    }

    private int getHandicap() {
        String selectedItem = (String) binding.handicapSpinner.getSelectedItem();
        try {
            return Integer.valueOf(selectedItem);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setHandicap(int handicap) {
        int index = (handicap == 0 ? 0 : handicap - 1);
        binding.handicapSpinner.setSelection(index);
    }
}
