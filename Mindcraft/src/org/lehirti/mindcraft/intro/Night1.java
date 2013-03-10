package org.lehirti.mindcraft.intro;

import lge.events.EventNode;
import lge.events.Event.NullState;
import lge.res.images.ImgChange;
import lge.res.text.CommonText;
import lge.res.text.TextKey;

import org.lehirti.mindcraft.images.TiffaniaWestwood;

public class Night1 extends EventNode<NullState> {
  public static enum Text implements TextKey {
    DREAM1
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setFG(TiffaniaWestwood.NIGHT_01);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.DREAM1);
    
    addOption(CommonText.OPTION_NEXT, new Morning1());
  }
}
