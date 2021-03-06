package intro;

import lge.events.EventNode;
import lge.events.Event.NullState;
import lge.gui.Key;
import lge.res.images.ImgChange;
import lge.res.text.TextKey;

public class PlaneInThunderstorm extends EventNode<NullState> {
  public static enum Text implements TextKey {
    PLANE_IN_THUNDERSTORM,
    OPTION_SECURE_OWN_MASK;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBGAndFG(IntroImage.PLANE_IN_THUNDERSTORM);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.PLANE_IN_THUNDERSTORM);
    
    addOption(Key.OPTION_WEST, Text.OPTION_SECURE_OWN_MASK, new TheCrash());
  }
}
