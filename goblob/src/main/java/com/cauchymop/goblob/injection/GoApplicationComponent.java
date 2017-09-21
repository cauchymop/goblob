package com.cauchymop.goblob.injection;

import com.cauchymop.goblob.ui.AchievementManagerAndroid;
import com.cauchymop.goblob.ui.GameFragment;
import com.cauchymop.goblob.ui.InGameViewAndroid;
import com.cauchymop.goblob.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dagger Component to instantiate injected objects.
 */
@Singleton
@Component (
    modules= GoApplicationModule.class
)
public interface GoApplicationComponent {
  void inject(MainActivity mainActivity);
  void inject(GameFragment gameFragment);
  void inject(InGameViewAndroid inGameViewAndroid);
  void inject(AchievementManagerAndroid achievementManagerAndroid);
}
