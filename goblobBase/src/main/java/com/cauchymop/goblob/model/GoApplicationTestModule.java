package com.cauchymop.goblob.model;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {
        GameDatas.class,
        GoGameController.class
    }
)
public class GoApplicationTestModule {
  @Provides
  @Named("PlayerOneDefaultName")
  String providePlayerOneDefaultName() {
    return "Pipo";
  }

  @Provides
  @Named("PlayerTwoDefaultName")
  String providePlayerTwoDefaultName() {
    return "Bimbo";
  }
}
