package org.lehirti.mindcraft.intro;

import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.res.images.ImgChange;
import org.lehirti.engine.res.text.CommonText;

public class Masturbate2 extends EventNode<NullState> {
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setFG(Intro.MASTURBATE);
  }
  
  @Override
  protected void doEvent() {
    setText(Intro.MASTURBATE);
    set(Bool.YOU_ARE_HORNY, false);
    
    addOption(CommonText.OPTION_LEAVE_HOUSE, new HomeVillage());
  }
}
