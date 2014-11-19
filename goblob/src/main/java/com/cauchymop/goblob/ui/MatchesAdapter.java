package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauchymop.goblob.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Adapter displaying MatchMenuItem from the actionbar navigation spinner.
 */
public class MatchesAdapter extends ArrayAdapter<MatchMenuItem> {

  public MatchesAdapter(Context context, List<MatchMenuItem> items) {
    super(context, R.layout.match_row_view, R.id.label, items);
  }

  @Override @NotNull
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    final View matchRowView = super.getDropDownView(position, convertView, parent);
    MatchMenuItem item = getItem(position);

    TextView labelView = (TextView) matchRowView.findViewById( R.id.label);
    labelView.setText(item.getDisplayName(getContext()));

    ImageView iconView = (ImageView) matchRowView.findViewById( R.id.match_type_icon);
    iconView.setImageDrawable(item.getIcon(getContext()));
    // May have been set to GONE if it was a closed view.
    iconView.setVisibility(View.VISIBLE);

    return matchRowView;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO: Add number of games by turn status
    final View matchRowView = getDropDownView(position, convertView, parent);
    View icon = matchRowView.findViewById(R.id.match_type_icon);
    icon.setVisibility(View.GONE);
    return matchRowView;
  }
}
