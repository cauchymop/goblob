package com.cauchymop.goblob.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cauchymop.goblob.R
import com.cauchymop.goblob.databinding.FragmentPlayerChoiceBinding
import com.cauchymop.goblob.model.GoogleAccountManager
import javax.inject.Inject

/**
 * Home Page Fragment.
 */
class PlayerChoiceFragment : GoBlobBaseFragment() {

  private var _binding: FragmentPlayerChoiceBinding? = null

  // This property is only valid between onCreateView and onDestroyView.
  private val binding get() = _binding!!

  @Inject
  lateinit var accountManager: GoogleAccountManager

  private val isLocal: Boolean
    get() = when (binding.gameTypeRadioGroup.checkedRadioButtonId) {
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
    _binding = FragmentPlayerChoiceBinding.inflate(inflater, container, false)
    binding.gameTypeRadioLocal.isChecked = true
    binding.configureGameButton.setOnClickListener { configureGame() }
    return binding.root
  }

  private fun configureGame() {
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
    val remotePlayerRadio = binding.gameTypeRadioRemote
    remotePlayerRadio.isEnabled = isSignInComplete
    if (remotePlayerRadio.isChecked) {
      binding.gameTypeRadioLocal.isChecked = true
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}
