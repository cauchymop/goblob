package com.cauchymop.goblob.injection;

import dagger.ObjectGraph;

/**
 * Singleton providing dependency injection.
 */
public class Injector {
  private static ObjectGraph objectGraph;

  public static void setObjectGraph(ObjectGraph objectGraph) {
    Injector.objectGraph = objectGraph;
  }

  public static void inject(Object instance) {
    objectGraph.inject(instance);
  }
}
