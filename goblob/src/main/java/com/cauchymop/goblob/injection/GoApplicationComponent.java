package com.cauchymop.goblob.injection;

import com.cauchymop.goblob.ui.GameFragment;
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
}
