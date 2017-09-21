package com.cauchymop.goblob.ui;


import android.content.res.Resources;

import com.cauchymop.goblob.R;
import com.cauchymop.goblob.presenter.AchievementManager;

class AchievementManagerAndroid implements AchievementManager {
  private final Resources resources;
  private MainActivity goBlobActivity;

  AchievementManagerAndroid(MainActivity goBlobActivity) {
    this.goBlobActivity = goBlobActivity;
    resources = goBlobActivity.getResources();

  }

  @Override
  public void unlockAchievement9x9() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_9x9));
  }

  @Override
  public void unlockAchievement13x13() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_13x13));
  }

  @Override
  public void unlockAchievement19x19() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_19x19));
  }

  @Override
  public void unlockAchievementLocal() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_local));
  }

  @Override
  public void unlockAchievementRemote() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_remote));
  }

  @Override
  public void unlockAchievementWinner() {
    goBlobActivity.unlockAchievement(resources.getString(R.string.achievements_winner));
  }
}
