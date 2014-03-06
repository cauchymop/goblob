package com.cauchymop.goblob.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cauchymop.goblob.R;

/**
 * Home Page Fragment.
 */
public class HomeFragment extends GoBlobBaseFragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_home, container, false);
    v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        getGoBlobActivity().beginUserInitiatedSignIn();
      }
    });
    return v;
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    getView().findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    getView().findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.check_matches_button).setVisibility(View.VISIBLE);
  }

  @Override
  public void onSignInFailed() {
    super.onSignInFailed();
    getView().findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
    getView().findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    getView().findViewById(R.id.check_matches_button).setVisibility(View.GONE);
  }
}
