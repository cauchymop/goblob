package com.cauchymop.goblob.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Player
import com.google.android.gms.games.PlayersClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class GoogleAccountManager @Inject constructor(
    private val signInAccountProvider: Provider<GoogleSignInAccount?>,
    private val playersClientProvider: Provider<PlayersClient>) {

  private var player: Player? = null
  private val listeners = mutableListOf<AccountStateListener>()

  val signedInAccount
    get() = signInAccountProvider.get()

  val signInComplete
    get() = player != null

  val currentPlayerId
    get() = player!!.playerId

  val currentPlayer
    get() = player

  fun onSignInSuccess() {
    playersClientProvider.get().getCurrentPlayer().addOnSuccessListener { player ->
      this.player = player
      fireStateChanged(true)
    }
  }

  fun onSignOut() {
    this.player = null
    fireStateChanged(false)
  }

  private fun fireStateChanged(isSignInComplete: Boolean) {
    listeners.forEach { it.accountStateChanged(isSignInComplete) }
  }

  fun addAccountStateListener(accountStateListener: AccountStateListener) {
    listeners.add(accountStateListener)
  }

}

interface AccountStateListener {
  fun accountStateChanged(isSignInComplete: Boolean)
}
