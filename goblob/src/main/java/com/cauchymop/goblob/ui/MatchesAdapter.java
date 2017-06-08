package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauchymop.goblob.R;
import com.google.common.base.Strings;

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
    return populateView(position, convertView, parent, true);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO: Add number of games by turn status
    return populateView(position, convertView, parent, false);
  }

  @NonNull
  public View populateView(int position, View convertView, ViewGroup parent, boolean hasIcon) {
    final View matchRowView;

    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      matchRowView = inflater.inflate(R.layout.match_row_view, parent, false);
    } else {
      matchRowView = convertView;
    }

    MatchMenuItem item = getItem(position);
    int textColor = item.isValid() ? Color.BLACK : Color.RED;

    TextView firstLineLabelView = (TextView) matchRowView.findViewById( R.id.label_first_line);
    firstLineLabelView.setText(item.getFirstLine(getContext()));
    firstLineLabelView.setTextColor(textColor);

    TextView secondLineLabelView = (TextView) matchRowView.findViewById( R.id.label_second_line);
    String secondLine = item.getSecondLine(getContext());
    secondLineLabelView.setText(secondLine);
    secondLineLabelView.setVisibility(Strings.isNullOrEmpty(secondLine) ? View.GONE : View.VISIBLE);
    secondLineLabelView.setTextColor(textColor);

    ImageView iconView = (ImageView) matchRowView.findViewById( R.id.match_type_icon);
    if(hasIcon) {
      iconView.setImageDrawable(item.getIcon(getContext()));
      iconView.setVisibility(View.VISIBLE);
    } else {
      iconView.setVisibility(View.GONE);
    }

    return matchRowView;
  }
}
