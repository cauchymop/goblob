package com.cauchymop.goblob.model;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Class to manage the GoogleApiClient life cycle.
 */
@Singleton
public class GoogleApiClientManager implements GoogleApiClientListener, Provider<GoogleApiClient> {

  private final GoogleApiClient googleApiClient;
  private GoogleApiClientListener googleApiClientListener;
  private DelayedCall delayedListenerCall;

  @Inject
  public GoogleApiClientManager(Context context) {
    this.googleApiClient = new GoogleApiClient.Builder(context)
        .addApi(Games.API).addScope(Games.SCOPE_GAMES)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
  }

  @Override
  public GoogleApiClient get() {
    return googleApiClient;
  }

  @Override
  public void onConnected(final Bundle bundle) {
    callListener(new DelayedCall() {
      @Override
      public void execute(GoogleApiClientListener googleApiClientListener) {
        googleApiClientListener.onConnected(bundle);
      }
    });
  }

  @Override
  public void onConnectionSuspended(final int i) {
    callListener(new DelayedCall() {
      @Override
      public void execute(GoogleApiClientListener googleApiClientListener) {
        googleApiClientListener.onConnectionSuspended(i);
      }
    });
  }

  @Override
  public void onConnectionFailed(final ConnectionResult connectionResult) {
    callListener(new DelayedCall() {
      @Override
      public void execute(GoogleApiClientListener googleApiClientListener) {
        googleApiClientListener.onConnectionFailed(connectionResult);
      }
    });
  }

  private void callListener(DelayedCall function) {
    if (googleApiClientListener != null) {
      function.execute(googleApiClientListener);
    } else {
      delayedListenerCall = function;
    }
  }

  public void registerGoogleApiClientListener(GoogleApiClientListener googleApiClientListener) {
    this.googleApiClientListener = googleApiClientListener;
    if (delayedListenerCall != null) {
      delayedListenerCall.execute(this.googleApiClientListener);
      delayedListenerCall = null;
    }
  }

  public void unregisterGoogleApiClientListener(GoogleApiClientListener googleApiClientListener) {
    googleApiClient.unregisterConnectionCallbacks(googleApiClientListener);
    googleApiClient.unregisterConnectionFailedListener(googleApiClientListener);
  }

  public void signout() {
    Games.signOut(googleApiClient);
    googleApiClient.disconnect();
  }

  private interface DelayedCall {
    void execute(GoogleApiClientListener googleApiClientListener);
  }
}
