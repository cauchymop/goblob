package com.cauchymop.goblob.ui;

import com.cauchymop.goblob.injection.GoApplicationComponent;

import androidx.fragment.app.Fragment;

/**
 * Base Fragment implementing common behaviour.
 */
public abstract class GoBlobBaseFragment extends Fragment {
  protected MainActivity getGoBlobActivity() {
    return ((MainActivity)getActivity());
  }

  public void updateFromConnectionStatus(boolean isSignInComplete) {}

  protected GoApplicationComponent getComponent() {
    return ((GoApplication)getActivity().getApplication()).getComponent();
  }
}
