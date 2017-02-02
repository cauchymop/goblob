package com.cauchymop.goblob.model;

public class ConfigurationViewModel {
  final double komi;

  public ConfigurationViewModel(double komi) {
    this.komi = komi;
  }

  public double getKomi() {
    return komi;
  }
}
