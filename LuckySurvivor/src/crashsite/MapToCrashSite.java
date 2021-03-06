package crashsite;

import lge.events.AlternativeOneTimeEvents;
import lge.events.EventNode;
import lge.events.TextOnlyEvent;
import lge.events.Event.NullState;
import lge.gui.Key;
import lge.res.TextAndImageKeyWithFlag;
import lge.res.images.ImgChange;
import lge.res.text.CommonText;
import lge.res.text.TextKey;
import map.Map;
import map.Map.Location;
import npc.NPCSelectEvent;


import common.Rest;

public class MapToCrashSite extends EventNode<NullState> {
  public static enum Text implements TextKey {
    DESCRIPTION,
    
    OPTION_LOOK_FOR_OTHER_SURVIVORS,
    
    OPTION_HELP_OTHER_SURVIVORS,
    
    OPTION_EXAMINE_OTHER_SURVIVORS,
    EXAMINE_OTHER_SURVIVORS,
    
    OPTION_REST,
    REST,
    
    LEAVE_THE_AREA,
    
    OPTION_ENTER_FUSELAGE,
    ENTER_FUSELAGE,
    
    OPTION_ENTER_SHELTER,
    
    OPTION_TRY_TO_PEEK_ON_GIRLS,
    TRY_TO_PEEK_ON_GIRLS,
    
    OPTION_BURY_THE_DEAD;
  }
  
  public static enum TxtImg implements TextAndImageKeyWithFlag {
    LOOK_FOR_OTHER_SURVIVORS_DEFAULT,
    LOOK_FOR_OTHER_SURVIVORS_ALT_1,
    LOOK_FOR_OTHER_SURVIVORS_ALT_2,
    LOOK_FOR_OTHER_SURVIVORS_ALT_HAS_HELPED_1,
    LOOK_FOR_OTHER_SURVIVORS_ALT_HAS_HELPED_2,
    
    HELP_OTHER_SURVIVORS_DEFAULT,
    HELP_OTHER_SURVIVORS_ALT_1,
    HELP_OTHER_SURVIVORS_ALT_2,
    HELP_OTHER_SURVIVORS_ALT_HAS_HELPED_1,
    HELP_OTHER_SURVIVORS_ALT_HAS_HELPED_2;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBGAndFG(CrashSite.OUTSIDE_PLANE_NON_BURNING);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.DESCRIPTION);
    
    if (is(CrashSiteBool.SHELTER_HAS_BEEN_BUILT)) {
      addOption(Key.OPTION_SOUTH, Text.OPTION_TRY_TO_PEEK_ON_GIRLS, new TextOnlyEvent(Key.OPTION_SOUTH,
          Text.TRY_TO_PEEK_ON_GIRLS, new MapToCrashSite()));
      addOption(Key.OPTION_NORTH, Text.OPTION_ENTER_SHELTER, new Shelter());
    } else {
      if (is(Plane1_Fuselage.Bool.HAS_HELPED_OTHERS_OUT_OF_PLANE)) {
        addOption(Key.OPTION_NORTH, Text.OPTION_LOOK_FOR_OTHER_SURVIVORS, new AlternativeOneTimeEvents(
            new MapToCrashSite(), TxtImg.LOOK_FOR_OTHER_SURVIVORS_DEFAULT, TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_1,
            TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_2, TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_HAS_HELPED_1,
            TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_HAS_HELPED_2));
        addOption(Key.OPTION_SOUTH, Text.OPTION_HELP_OTHER_SURVIVORS, new AlternativeOneTimeEvents(
            new MapToCrashSite(), TxtImg.HELP_OTHER_SURVIVORS_DEFAULT, TxtImg.HELP_OTHER_SURVIVORS_ALT_1,
            TxtImg.HELP_OTHER_SURVIVORS_ALT_2, TxtImg.HELP_OTHER_SURVIVORS_ALT_HAS_HELPED_1,
            TxtImg.HELP_OTHER_SURVIVORS_ALT_HAS_HELPED_2));
      } else {
        addOption(Key.OPTION_NORTH, Text.OPTION_LOOK_FOR_OTHER_SURVIVORS, new AlternativeOneTimeEvents(
            new MapToCrashSite(), TxtImg.LOOK_FOR_OTHER_SURVIVORS_DEFAULT, TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_1,
            TxtImg.LOOK_FOR_OTHER_SURVIVORS_ALT_2));
        addOption(Key.OPTION_SOUTH, Text.OPTION_HELP_OTHER_SURVIVORS, new AlternativeOneTimeEvents(
            new MapToCrashSite(), TxtImg.HELP_OTHER_SURVIVORS_DEFAULT, TxtImg.HELP_OTHER_SURVIVORS_ALT_1,
            TxtImg.HELP_OTHER_SURVIVORS_ALT_2));
      }
      
      addOption(Key.OPTION_ENTER, Text.OPTION_ENTER_FUSELAGE, new TextOnlyEvent(Key.OPTION_ENTER, Text.ENTER_FUSELAGE,
          new Plane2_Fuselage()));
      addOption(Key.OPTION_WEST, Text.OPTION_REST, new Rest(Key.OPTION_WEST, Text.REST, null, new MapToCrashSite()));
      addOption(Key.OPTION_V, Text.OPTION_BURY_THE_DEAD, new BuryTheDead());
    }
    
    addOption(Key.OPTION_EAST, Text.OPTION_EXAMINE_OTHER_SURVIVORS, new NPCSelectEvent(
        CrashSite.OUTSIDE_PLANE_NON_BURNING, new MapToCrashSite(), CrashSiteNPCs.getNPCs(), 0));
    
    addOption(Key.OPTION_LEAVE, CommonText.OPTION_LEAVE_AREA, new TextOnlyEvent(Key.OPTION_LEAVE, Text.LEAVE_THE_AREA,
        new Map(Location.CRASH_SITE)));
  }
}
