package com.cauchymop.goblob.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cauchymop.goblob.databinding.FragmentUpdateApplicationBinding;

//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import butterknife.Unbinder;


public class UpdateApplicationFragment extends GoBlobBaseFragment {

//  private Unbinder unbinder;

  private static FragmentUpdateApplicationBinding binding = null;


  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment UpdateApplicationFragment.
   */
  public static UpdateApplicationFragment newInstance() {
    UpdateApplicationFragment fragment = new UpdateApplicationFragment();
    return fragment;
  }

  public UpdateApplicationFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    binding = FragmentUpdateApplicationBinding.inflate(LayoutInflater.from(getContext()));
    View view = binding.getRoot();
    binding.updateButton.setOnClickListener(v -> update());
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  public void update() {
    final String appPackageName = getActivity().getApplicationContext().getPackageName(); // getPackageName() from Context or Activity object
    try {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
    } catch (android.content.ActivityNotFoundException anfe) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
    }
  }
}
