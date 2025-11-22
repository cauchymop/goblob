package com.cauchymop.goblob.ui

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.cauchymop.goblob.BuildConfig
import com.cauchymop.goblob.lobby.LobbyClient
import com.cauchymop.goblob.model.AccountStateListener
import com.cauchymop.goblob.model.Analytics
import com.cauchymop.goblob.model.AvatarManager
import com.cauchymop.goblob.model.GameDatas
import com.cauchymop.goblob.model.GameRepository
import com.cauchymop.goblob.model.GoogleAccountManager
import com.cauchymop.goblob.proto.PlayGameData.GameData
import com.cauchymop.goblob.proto.PlayGameData.GameList
import com.crashlytics.android.Crashlytics
import com.google.protobuf.TextFormat
import dagger.Lazy
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Class to persist games.
 */

private const val GAME_DATA = "gameData"
private const val KEY_GAMES = "games"
private const val KEY_CLIENT_UUID = "client_uuid"
private const val TAG = "AndroidGameRepository"

@Singleton
class AndroidGameRepository @Inject
constructor(
    private val prefs: SharedPreferences, gameDatas: GameDatas,
    private val googleAccountManager: GoogleAccountManager,
    private val avatarManager: AvatarManager, analytics: Analytics,
    @Named("ApplicationName") private val appName: String,
    @Named("PlayerOneDefaultName") playerOneDefaultName: Lazy<String>,
    @Named("PlayerTwoDefaultName") playerTwoDefaultName: String
) : GameRepository(
    analytics,
    playerOneDefaultName,
    playerTwoDefaultName,
    gameDatas,
    gameCache = loadGameCache(prefs)
) {
    private val lobbyClient: LobbyClient = LobbyClient(
        getOrCreateClientId(prefs),
        appName,
        BuildConfig.VERSION_NAME
    )

    init {
        loadLegacyLocalGame()
        fireGameListChanged()
        googleAccountManager.addAccountStateListener(object : AccountStateListener {
            override fun accountStateChanged(isSignInComplete: Boolean) {
                if (isSignInComplete) {
                    onGoogleSignIn()
                }
            }
        })
        // TODO: Pass Google Signin to LobbyClient somehow when isSignInComplete?
        lobbyClient.addListener(this)
    }

    private fun onGoogleSignIn() {
        val signedInAccount = googleAccountManager.signedInAccount
        val email = signedInAccount?.email ?: return
        val idToken = signedInAccount.idToken ?: return
        lobbyClient.setGoogleLogin(email, idToken)
    }

    override fun forceCacheRefresh() {
        Crashlytics.log(Log.DEBUG, TAG, "forceCacheRefresh")
        persistCache()
        fireGameListChanged()
    }

    override fun getLobbyClient(): LobbyClient = lobbyClient


    private fun persistCache() {
        prefs.edit {
            putString(KEY_GAMES, TextFormat.printer().printToString(gameCache))
        }
    }

    override fun log(message: String) {
        Crashlytics.log(Log.DEBUG, TAG, message)
    }

    private fun loadLegacyLocalGame() {
        val gameDataString = prefs.getString(GAME_DATA, null) ?: return
        log("loadLegacyLocalGame")
        val gameDataBuilder = GameData.newBuilder()
        try {
            TextFormat.merge(gameDataString, gameDataBuilder)
        } catch (e: TextFormat.ParseException) {
            Crashlytics.log(Log.ERROR, TAG, "Error parsing local GameData: ${e.message}")
        }

        val localGame = gameDataBuilder.build()
        if (saveToCache(localGame)) {
            forceCacheRefresh()
        }
        prefs.edit { remove(GAME_DATA) }
    }

}

private fun loadGameCache(sharedPreferences: SharedPreferences): GameList.Builder {
    Crashlytics.log(Log.DEBUG, TAG, "loadGameList")
    val gameListString = sharedPreferences.getString(KEY_GAMES, "")
    val gameListBuilder = GameList.newBuilder()
    try {
        TextFormat.merge(gameListString, gameListBuilder)
    } catch (e: TextFormat.ParseException) {
        Crashlytics.log(Log.ERROR, TAG, "Error parsing local GameList: " + e.message)
    }

    Crashlytics.log(
        Log.DEBUG,
        TAG,
        "loadGameList: " + gameListBuilder.gamesCount + " games loaded."
    )
    return gameListBuilder
}

private fun getOrCreateClientId(sharedPreferences: SharedPreferences): String {
    var guid = sharedPreferences.getString(KEY_CLIENT_UUID, null)
    if (guid == null) {
        guid = UUID.randomUUID().toString()
        sharedPreferences.edit { putString(KEY_CLIENT_UUID, guid) }
    }
    return guid
}
