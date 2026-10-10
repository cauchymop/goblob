package com.cauchymop.goblob.model

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialAuthHelper @Inject constructor() {

    fun startGoogleSignIn(
        context: Context,
        webClientId: String,
        isSilent: Boolean,
        onSuccess: BiConsumer<String, String>,
        onError: Consumer<Throwable>
    ) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(isSilent)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                handleSignIn(result, onSuccess, onError)
            } catch (e: GetCredentialException) {
                if (!isSilent) {
                    onError.accept(e)
                }
            }
        }
    }

    private fun handleSignIn(
        result: GetCredentialResponse,
        onSuccess: BiConsumer<String, String>,
        onError: Consumer<Throwable>
    ) {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onSuccess.accept(googleIdTokenCredential.idToken, googleIdTokenCredential.id)
            } catch (e: Exception) {
                onError.accept(e)
            }
        } else {
            onError.accept(IllegalStateException("Unexpected credential type"))
        }
    }
}
