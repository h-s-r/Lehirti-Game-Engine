package org.lehirti.luckysurvivor;

public enum C {
  MAIN(3);
  
  public final int requiredVersion;
  public boolean available = false;
  
  private C(final int requiredVersion) {
    this.requiredVersion = requiredVersion;
  }
}
