package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauchymop.goblob.R;

import java.util.List;

/**
 * Adapter displaying MatchMenuItem from the actionbar navigation spinner.
 */
public class MatchesAdapter extends ArrayAdapter<MatchMenuItem> {




  public MatchesAdapter(Context context, List<MatchMenuItem> matchItems) {
    super(context, R.layout.match_row, R.id.label, matchItems);
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    final View rowView = super.getView(position, convertView, parent);
    assert rowView != null;
    MatchMenuItem item = getItem(position);

    TextView labelView = (TextView) rowView.findViewById( R.id.label);
    labelView.setText(item.getDisplayName(getContext()));

    ImageView iconView = (ImageView) rowView.findViewById( R.id.match_type_icon);
    iconView.setVisibility(View.VISIBLE);
    Drawable icon = item.getIcon(getContext());
    iconView.setImageDrawable(icon);

    return rowView;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO: Add number of messages
    final View rowView =  getDropDownView(position, convertView, parent);
    View icon = rowView.findViewById( R.id.match_type_icon);
    icon.setVisibility(View.GONE);
    return rowView;
  }





}
