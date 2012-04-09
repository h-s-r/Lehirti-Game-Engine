package org.lehirti.mindcraft.intro;

import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.res.text.CommonText;
import org.lehirti.engine.res.text.TextKey;
import org.lehirti.mindcraft.images.Dita;

public class MeetDitaOutsideHerHouse extends EventNode<MeetDitaOutsideHerHouse.State> {
  public static enum Text implements TextKey {
    FIRST_MEETING,
    RECURRING_MEETING
  }
  
  public static enum State {
    FIRST_MEETING_DONE
  }
  
  @Override
  protected void doEvent() {
    final State eventState = getEventState();
    if (eventState == null) {
      setText(Text.FIRST_MEETING);
      setImage(Dita.OUTSIDE_HER_HOUSE);
      setEventState(State.FIRST_MEETING_DONE);
      addOption(CommonText.OPTION_LEAVE, new HomeVillage());
    } else {
      switch (getEventState()) {
      case FIRST_MEETING_DONE:
        setText(Text.RECURRING_MEETING);
        setImage(Dita.OUTSIDE_HER_HOUSE);
        addOption(CommonText.OPTION_LEAVE, new HomeVillage());
        break;
      }
    }
  }
}
