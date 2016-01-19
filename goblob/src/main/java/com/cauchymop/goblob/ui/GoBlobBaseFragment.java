package com.cauchymop.goblob.ui;

import android.support.v4.app.Fragment;

import com.cauchymop.goblob.injection.GoApplicationComponent;

/**
 * Base Fragment implementing common behaviour.
 */
public class GoBlobBaseFragment extends Fragment {
  protected MainActivity getGoBlobActivity() {
    return ((MainActivity)getActivity());
  }

  public void updateFromConnectionStatus() {
  }

  protected boolean isSignedIn() {
    return getGoBlobActivity().isSignedIn();
  }

  protected GoApplicationComponent getComponent() {
    return ((GoApplication)getActivity().getApplication()).getComponent();
  }
}
