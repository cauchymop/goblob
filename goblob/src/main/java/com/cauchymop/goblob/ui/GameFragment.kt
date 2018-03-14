package com.cauchymop.goblob.ui

import android.os.Bundle
import android.view.*
import com.cauchymop.goblob.R
import com.cauchymop.goblob.presenter.ConfigurationEventListener
import com.cauchymop.goblob.presenter.GamePresenter
import com.cauchymop.goblob.view.GameView
import com.cauchymop.goblob.view.InGameView
import com.cauchymop.goblob.viewmodel.ConfigurationViewModel
import com.cauchymop.goblob.viewmodel.InGameViewModel
import kotlinx.android.synthetic.main.fragment_game.*
import javax.inject.Inject

private const val GAME_CONFIGURATION_VIEW_INDEX = 0
private const val IN_GAME_VIEW_INDEX = 1

class GameFragment : GoBlobBaseFragment(), GameView {

    @Inject
    lateinit var gamePresenter: GamePresenter

    private var undoActionAvailable: Boolean = false
    private var redoActionAvailable: Boolean = false
    private var resignActionAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
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

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_undo).isVisible = undoActionAvailable
        menu.findItem(R.id.menu_redo).isVisible = redoActionAvailable
        menu.findItem(R.id.menu_resign).isVisible = resignActionAvailable
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        if (id == R.id.menu_undo) {
            inGameView.onUndo()
            return true
        } else if (id == R.id.menu_redo) {
            inGameView.onRedo()
            return true
        } else if (id == R.id.menu_resign) {
            inGameView.onResign()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setConfigurationViewModel(configurationViewModel: ConfigurationViewModel?) {
        updateMenu(false, false, false)
        configurationView.setConfigurationModel(configurationViewModel!!)
        currentGameView.displayedChild = GAME_CONFIGURATION_VIEW_INDEX
    }

    override fun setInGameViewModel(inGameViewModel: InGameViewModel) {
        updateMenu(inGameViewModel.isUndoActionAvailable, inGameViewModel.isRedoActionAvailable, inGameViewModel.isResignActionAvailable)
        inGameView!!.setInGameModel(inGameViewModel)
        currentGameView.displayedChild = IN_GAME_VIEW_INDEX
    }

    private fun updateMenu(undoActionAvailable: Boolean,
                           redoActionAvailable: Boolean, resignActionAvailable: Boolean) {
        this.undoActionAvailable = undoActionAvailable
        this.redoActionAvailable = redoActionAvailable
        this.resignActionAvailable = resignActionAvailable
    }

    override fun setInGameActionListener(inGameEventListener: InGameView.InGameEventListener?) {
        inGameView!!.setInGameEventListener(inGameEventListener)
    }

    override fun setConfigurationViewListener(configurationEventListener: ConfigurationEventListener?) {
        configurationView.setConfigurationViewListener(configurationEventListener)
    }

    companion object {
        fun newInstance(): GameFragment {
            return GameFragment()
        }
    }
}
