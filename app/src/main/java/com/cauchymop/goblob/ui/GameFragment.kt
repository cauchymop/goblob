package com.cauchymop.goblob.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.cauchymop.goblob.R
import com.cauchymop.goblob.databinding.FragmentGameBinding
import com.cauchymop.goblob.presenter.ConfigurationEventListener
import com.cauchymop.goblob.presenter.GamePresenter
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.view.InGameView
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import com.cauchymop.goblob.viewmodel.InGameViewModel
import javax.inject.Inject

private const val GAME_CONFIGURATION_VIEW_INDEX = 0
private const val IN_GAME_VIEW_INDEX = 1

class GameFragment : GoBlobBaseFragment(), GameView {

    @Inject
    lateinit var gamePresenter: GamePresenter

    private var undoActionAvailable: Boolean = false
    private var redoActionAvailable: Boolean = false
    private var resignActionAvailable: Boolean = false
    private var _binding: FragmentGameBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gamePresenter.view = this
    }

    override fun onDestroyView() {
        gamePresenter.clear()
        super.onDestroyView()
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_undo).isVisible = undoActionAvailable
        menu.findItem(R.id.menu_redo).isVisible = redoActionAvailable
        menu.findItem(R.id.menu_resign).isVisible = resignActionAvailable
        super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_undo) {
            binding.inGameView.onUndo()
            return true
        } else if (id == R.id.menu_redo) {
            binding.inGameView.onRedo()
            return true
        } else if (id == R.id.menu_resign) {
            binding.inGameView.onResign()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setConfigurationViewModel(configurationViewModel: ConfigurationViewModel?) {
        requireActivity().runOnUiThread {
            updateMenu(false, false, false)
            binding.configurationView.setConfigurationModel(configurationViewModel!!)
            binding.currentGameView.displayedChild = GAME_CONFIGURATION_VIEW_INDEX
        }

    }

    override fun setInGameViewModel(inGameViewModel: InGameViewModel) {
        updateMenu(
            inGameViewModel.isUndoActionAvailable,
            inGameViewModel.isRedoActionAvailable,
            inGameViewModel.isResignActionAvailable
        )
        binding.inGameView.setInGameModel(inGameViewModel)
        binding.currentGameView.displayedChild = IN_GAME_VIEW_INDEX
    }

    private fun updateMenu(
        undoActionAvailable: Boolean,
        redoActionAvailable: Boolean, resignActionAvailable: Boolean
    ) {
        this.undoActionAvailable = undoActionAvailable
        this.redoActionAvailable = redoActionAvailable
        this.resignActionAvailable = resignActionAvailable
    }

    override fun setInGameActionListener(inGameEventListener: InGameView.InGameEventListener?) {
        binding.inGameView.setInGameEventListener(inGameEventListener)
    }

    override fun setConfigurationViewListener(configurationEventListener: ConfigurationEventListener?) {
        binding.configurationView.setConfigurationViewListener(configurationEventListener)
    }

    companion object {
        fun newInstance(): GameFragment {
            return GameFragment()
        }
    }
}
