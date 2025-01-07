package org.jvnkr.blogbackend.utils;

import lombok.Getter;

@Getter
public enum AppEnvironments {
  DEV("development"),
  PROD("production");

  private final String envName;

  AppEnvironments(String envName) {
    this.envName = envName;
  }

  public static AppEnvironments fromString(String envName) {
    for (AppEnvironments env : values()) {
      if (env.getEnvName().equalsIgnoreCase(envName)) {
        return env;
      }
    }
    throw new IllegalArgumentException("Unknown env: " + envName);
  }


}
