package org.lehirti.luckysurvivor.intro;

import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.gui.Key;
import org.lehirti.engine.res.images.ImgChange;
import org.lehirti.engine.res.text.TextKey;
import org.lehirti.luckysurvivor.crashsite.AreaCrash1_1;

public class TheCrash extends EventNode<NullState> {
  public static enum Text implements TextKey {
    THE_CRASH,
    OPTION_TUG_YOUR_HEAD;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBG(IntroImage.THE_CRASH);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.THE_CRASH);
    
    addOption(Key.OPTION_WEST, Text.OPTION_TUG_YOUR_HEAD, new AreaCrash1_1());
  }
}
