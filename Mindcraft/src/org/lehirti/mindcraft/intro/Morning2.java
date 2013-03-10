package org.lehirti.mindcraft.intro;

import lge.events.EventNode;
import lge.events.Event.NullState;
import lge.res.images.ImgChange;
import lge.res.text.CommonText;
import lge.res.text.TextKey;

import org.lehirti.mindcraft.images.Background;

public class Morning2 extends EventNode<NullState> {
  public static enum Text implements TextKey {
    MORNING2,
    OPTION_MASTURBATE,
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBGAndFG(Background.VILLAGE_HOME_INSIDE);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.MORNING2);
    set(Bool.YOU_ARE_HORNY, true);
    
    addOption(Text.OPTION_MASTURBATE, new Masturbate2());
    addOption(CommonText.OPTION_LEAVE_HOUSE, new HomeVillage());
  }
}
