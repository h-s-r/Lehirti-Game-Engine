package crashsite;

import lge.events.Event.NullState;
import lge.events.EventNode;
import lge.gui.Key;
import lge.res.images.ImgChange;
import lge.res.text.TextKey;
import lge.state.TimeInterval;

public class FirstDiscussion_Injurieslook extends EventNode<NullState> {
  public static enum Text implements TextKey {
    DESCRIPTION,
    OPTION_GO_ON;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBG(CrashSite.CLEAVAGE_LOOK);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.DESCRIPTION);
    addOption(Key.OPTION_ENTER, Text.OPTION_GO_ON, new FirstDiscussion());
  }
  
  @Override
  public TimeInterval getRequiredTimeInterval() {
    // advance time to the next 8:00:00 (format is hhmmss; 24h; but do not add leading zeros: 080000 would be wrong!)
    // TimeInterval.advanceTo(DayOfWeek.THURSDAY, 63221); <- would jump to the next Thursday 06:32:21
    return TimeInterval.advanceTo(80000);
    
  }
}
