package lge.sex;

import static lge.sex.SexFeature.*;

public enum Sex {
  MALE(COCK, MOUTH, ASS, ANY),
  FEMALE(PUSSY, BREASTS, MOUTH, ASS, ANY),
  FUTA(COCK, PUSSY, BREASTS, MOUTH, ASS, ANY);
  
  private final SexFeature[] features;
  
  private Sex(final SexFeature... features) {
    this.features = features;
  }
  
  public boolean has(final SexFeature feature) {
    for (final SexFeature feature2 : this.features) {
      if (feature2 == feature) {
        return true;
      }
    }
    return false;
  }
}
