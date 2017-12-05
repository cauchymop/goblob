package com.cauchymop.goblob.presenter;


import com.cauchymop.goblob.model.GoGameController;

import org.jetbrains.annotations.NotNull;

public interface AchievementManager {
  void updateAchievements(@NotNull GoGameController goGameController);
}
