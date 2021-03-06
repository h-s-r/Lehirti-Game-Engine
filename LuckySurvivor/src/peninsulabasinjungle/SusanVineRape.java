package peninsulabasinjungle;

import lge.events.EventNode;
import lge.events.Event.NullState;
import lge.gui.Key;
import lge.res.images.ImgChange;
import lge.res.text.TextKey;
import lge.state.State;

public class SusanVineRape extends EventNode<NullState> {
  
  public static enum Text implements TextKey {
    DESCRIPTION,
    OPTION_LOOK_AT_FACE,
    OPTION_LOOK_AT_BREASTS,
    OPTION_LOOK_AT_PUSSY,
    OPTION_LOOK_AT_ASS,
    OPTION_TRY_TO_HELP,
    TEXT_TRY_TO_HELP;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setBGAndFG(PeninsulaBasinJungle.PENINSULA_BASIN_JUNGLE, PeninsulaBasinJungle.SUSAN_VINE_RAPED);
  }
  
  @Override
  protected void doEvent() {
    setText(Text.DESCRIPTION);
    
    addOption(Key.OPTION_NORTH, Text.OPTION_LOOK_AT_FACE, new SusanVineRapeFace());
    addOption(Key.OPTION_WEST, Text.OPTION_LOOK_AT_BREASTS, new SusanVineRapeBreasts());
    addOption(Key.OPTION_SOUTH, Text.OPTION_LOOK_AT_PUSSY, new SusanVineRapePussy());
    addOption(Key.OPTION_EAST, Text.OPTION_LOOK_AT_ASS, new SusanVineRapeAss());
    
    if (State.getEventCount(SusanVineRapeHelp.class) == 0) {
      addOption(Key.OPTION_ENTER, Text.OPTION_TRY_TO_HELP, new SusanVineRapeHelp());
    }
  }
}
