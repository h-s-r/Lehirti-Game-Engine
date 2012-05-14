package org.lehirti.luckysurvivor.crashsite;

import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.events.SetFlagEvent;
import org.lehirti.engine.events.TextOnlyEvent;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.gui.Key;
import org.lehirti.engine.res.images.ImgChange;
import org.lehirti.engine.res.text.TextKey;
import org.lehirti.engine.state.BoolState;

public class Plane2_Fuselage extends EventNode<NullState> {
  public static enum Text implements TextKey {
    DESCRIPTION,
    
    OPTION_LOOK_FOR_OTHER_SURVIVORS,
    LOOK_FOR_OTHER_SURVIVORS,
    
    OPTION_TRY_TO_FREE_TRAPPED_WOMAN,
    LOOK_FOR_MORE_SURVIVORS,
    
    OPTION_RECOVER_BODIES_FROM_WRECKAGE,
    
    OPTION_BUILD_STRECHER,
    
    OPTION_SEARCH_FOR_SOMETHING_USEFULL,
    
    OPTION_EXAMINE_FUSELAGE,
    EXAMINE_FUSELAGE,
    
    OPTION_CLEAR_RUBBLE,
    CLEAR_RUBBLE,
    
    OPTION_SEAL_HULL,
    SEAL_HULL,
    
    OPTION_USE_FUSELAGE_AS_SHELTER,
    USE_FUSELAGE_AS_SHELTER,
    
    OPTION_GO_TO_COCKPIT,
    
    OPTION_LEAVE_PLANE,
    LEAVE_PLANE,
    
    NOTHING_OF_VALUE_WAS_FOUND;
  }
  
  public static enum Bool implements BoolState {
    HAS_LOOKED_FOR_SURVIVORS,
    HAS_TRIED_TO_RECOVER_BODIES,
    HAS_STRATCHER,
    FUSELAGE_EXAMINED,
    RUBBLE_CLEARED,
    HULL_SEALED;
    
    @Override
    public Boolean defaultValue() {
      return Boolean.FALSE;
    }
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBG(CrashSite.INSIDE_FUSELAGE);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.DESCRIPTION);
    
    if (is(Bool.HAS_LOOKED_FOR_SURVIVORS)) {
      addOption(Key.OPTION_NORTH, Text.OPTION_TRY_TO_FREE_TRAPPED_WOMAN, new FreeTrappedWoman());
    } else {
      addOption(Key.OPTION_NORTH, Text.OPTION_LOOK_FOR_OTHER_SURVIVORS, new SetFlagEvent(Bool.HAS_LOOKED_FOR_SURVIVORS,
          Text.LOOK_FOR_OTHER_SURVIVORS, new Plane2_Fuselage()));
    }
    
    addOption(Key.OPTION_SOUTH, Text.OPTION_RECOVER_BODIES_FROM_WRECKAGE, new RecoverBodiesFromPlane());
    
    if (is(Bool.HAS_TRIED_TO_RECOVER_BODIES) && !is(Bool.HAS_STRATCHER)) {
      addOption(Key.OPTION_EAST, Text.OPTION_BUILD_STRECHER, new BuildStrecher());
    }
    
    addOption(Key.OPTION_WEST, Text.OPTION_SEARCH_FOR_SOMETHING_USEFULL, new SearchFuselageForSomethingUsefull());
    
    if (is(Bool.FUSELAGE_EXAMINED)) {
      if (is(Bool.RUBBLE_CLEARED) && is(Bool.HULL_SEALED)) {
        addOption(Key.OPTION_A, Text.OPTION_USE_FUSELAGE_AS_SHELTER, new SetFlagEvent(
            org.lehirti.luckysurvivor.crashsite.Bool.SHELTER_HAS_BEEN_BUILT, Text.USE_FUSELAGE_AS_SHELTER,
            new Shelter()));
      } else {
        if (!is(Bool.RUBBLE_CLEARED)) {
          addOption(Key.OPTION_A, Text.OPTION_CLEAR_RUBBLE, new SetFlagEvent(Bool.RUBBLE_CLEARED, Text.CLEAR_RUBBLE,
              new Plane2_Fuselage()));
        }
        if (!is(Bool.HULL_SEALED)) {
          addOption(Key.OPTION_X, Text.OPTION_SEAL_HULL, new SetFlagEvent(Bool.HULL_SEALED, Text.SEAL_HULL,
              new Plane2_Fuselage()));
        }
      }
      
    } else {
      addOption(Key.OPTION_A, Text.OPTION_EXAMINE_FUSELAGE, new SetFlagEvent(Bool.FUSELAGE_EXAMINED,
          Text.EXAMINE_FUSELAGE, new Plane2_Fuselage()));
    }
    
    addOption(Key.OPTION_V, Text.OPTION_GO_TO_COCKPIT, new Plane2_Cockpit());
    
    addOption(Key.OPTION_LEAVE, Text.OPTION_LEAVE_PLANE, new TextOnlyEvent(Text.LEAVE_PLANE, new Outside1()));
  }
}