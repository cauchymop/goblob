package com.cauchymop.goblob.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.model.GameDatas;
import com.cauchymop.goblob.proto.PlayGameData;

/**
 * {@link MatchMenuItem} representing a Local Game.
 */
public class LocalMatchMenuItem extends MatchMenuItem {
  private final PlayGameData.GameData gameData;

  public LocalMatchMenuItem(PlayGameData.GameData gameData) {
    super(GameDatas.LOCAL_MATCH_ID);
    this.gameData = gameData;
  }

  @Override
  public String getFirstLine(Context context) {
    return context.getString(R.string.local_match_item_label);
  }

  @Override
  public String getSecondLine(Context context) {
    PlayGameData.GameConfiguration conf = gameData.getGameConfiguration();
    return context.getString(R.string.match_label_local_second_line_format, conf.getBoardSize());
  }

  @Override
  public Drawable getIcon(Context context) {
    return null;
  }

  @Override
  public void start(GameStarter gameStarter) {
    gameStarter.selectGame(gameData.getMatchId());
  }
}
