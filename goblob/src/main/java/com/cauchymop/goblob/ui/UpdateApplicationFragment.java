package com.cauchymop.goblob.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cauchymop.goblob.R;


public class UpdateApplicationFragment extends GoBlobBaseFragment {

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
    View view = inflater.inflate(R.layout.fragment_update_application, container, false);
    Button updateButton = (Button) view.findViewById(R.id.update_button);
    updateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        update();
      }
    });
    return view;
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
