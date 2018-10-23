package com.cauchymop.goblob.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.cauchymop.goblob.R
import com.cauchymop.goblob.model.GoogleAccountManager
import javax.inject.Inject

/**
 * Home Page Fragment.
 */
class PlayerChoiceFragment : GoBlobBaseFragment() {
  @BindView(R.id.game_type_radio_group)
  lateinit var opponentRadioGroup: RadioGroup
  @BindView(R.id.game_type_radio_local)
  lateinit var localHumanButton: RadioButton
  @BindView(R.id.game_type_radio_remote)
  lateinit var remotePlayerRadio: RadioButton

  private var unbinder: Unbinder? = null

  @Inject
  lateinit var accountManager: GoogleAccountManager

  private val isLocal: Boolean
    get() = when (opponentRadioGroup.checkedRadioButtonId) {
      R.id.game_type_radio_local -> true
      R.id.game_type_radio_remote -> false
      else -> true
    }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    component.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    val v = inflater.inflate(R.layout.fragment_player_choice, container, false)
    unbinder = ButterKnife.bind(this, v)
    localHumanButton.isChecked = true
    return v
  }

  override fun onDestroyView() {
    super.onDestroyView()
    unbinder!!.unbind()
  }

  @OnClick(R.id.configure_game_button)
  internal fun configureGame() {
    goBlobActivity.configureGame(isLocal)
  }

  override fun onResume() {
    super.onResume()
    updateRemotePlayerRadios()
  }


  override fun updateFromConnectionStatus(isSignInComplete: Boolean) {
    updateRemotePlayerRadios(isSignInComplete)
  }

  private fun updateRemotePlayerRadios(isSignInComplete: Boolean = accountManager.signInComplete) {
    remotePlayerRadio.isEnabled = isSignInComplete
    if (remotePlayerRadio.isChecked) {
      localHumanButton.isChecked = true
    }
  }

}
