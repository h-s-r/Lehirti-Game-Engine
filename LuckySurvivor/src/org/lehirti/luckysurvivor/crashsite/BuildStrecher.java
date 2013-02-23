package org.lehirti.luckysurvivor.crashsite;

import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.events.SetFlagTextOnlyEvent;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.gui.Key;
import org.lehirti.engine.res.images.ImgChange;
import org.lehirti.engine.res.text.CommonText;
import org.lehirti.engine.res.text.TextKey;

public class BuildStrecher extends EventNode<NullState> {
  public static enum Text implements TextKey {
    HAS_MATERIALS_TO_BUILD_STRECHER,
    LACK_MATERIALS_TO_BUILD_STRECHER,
    OBTAINED_STRECHER;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.noChange();
  }
  
  @Override
  protected void doEvent() {
    if (is(CrashSiteBoolInventory.LONG_METAL_POLES) && is(CrashSiteBoolInventory.SHEETS_OF_FABRIC)) {
      setText(Text.HAS_MATERIALS_TO_BUILD_STRECHER);
      addOption(Key.OPTION_EAST, CommonText.OPTION_NEXT, new SetFlagTextOnlyEvent(CrashSiteBoolInventory.STRETCHER,
          Key.OPTION_ENTER, Text.OBTAINED_STRECHER, new Plane2_Fuselage()));
    } else {
      setText(Text.LACK_MATERIALS_TO_BUILD_STRECHER);
      addOption(Key.OPTION_EAST, CommonText.OPTION_NEXT, new Plane2_Fuselage());
    }
  }
}
