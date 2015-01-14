package com.cauchymop.goblob.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
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

  public MatchesAdapter(Context context, List<MatchMenuItem> items) {
    super(context, R.layout.match_row_view, items);
  }

  @Override @NonNull
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    final View matchRowView;

    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      matchRowView = inflater.inflate(R.layout.match_row_view, parent, false);
    } else {
      matchRowView = convertView;
    }

    MatchMenuItem item = getItem(position);

    TextView firstLineLabelView = (TextView) matchRowView.findViewById( R.id.label_first_line);
    firstLineLabelView.setText(item.getFirstLine(getContext()));

    TextView secondLineLabelView = (TextView) matchRowView.findViewById( R.id.label_second_line);
    secondLineLabelView.setText(item.getSecondLine(getContext()));

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
