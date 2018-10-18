package com.cauchymop.goblob.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject
import javax.inject.Provider


class GoogleAccountManager @Inject constructor(private val signInAccountProvider:Provider<GoogleSignInAccount>) {

  val signedInAccount = signInAccountProvider.get()

  val signedIn =  signedInAccount != null

}