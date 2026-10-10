package com.cauchymop.goblob.model

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_SIGNED_IN_EMAIL = "signed_in_email"
private const val KEY_ID_TOKEN = "id_token"

@Singleton
class GoogleAccountManager @Inject constructor(private val prefs: SharedPreferences) {

  private val listeners = mutableListOf<AccountStateListener>()

  val signInComplete: Boolean
    get() = getEmail() != null

  fun getEmail(): String? = prefs.getString(KEY_SIGNED_IN_EMAIL, null)
  fun getIdToken(): String? = prefs.getString(KEY_ID_TOKEN, null)

  fun onSignInSuccess(email: String, idToken: String) {
    prefs.edit {
      putString(KEY_SIGNED_IN_EMAIL, email)
      putString(KEY_ID_TOKEN, idToken)
    }
    fireStateChanged(true)
  }

  fun onSignOut() {
    prefs.edit {
      remove(KEY_SIGNED_IN_EMAIL)
      remove(KEY_ID_TOKEN)
    }
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
