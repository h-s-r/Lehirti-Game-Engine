package org.lehirti.engine.events;

import java.io.Externalizable;
import java.io.File;

import org.lehirti.engine.Main;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.res.images.ImgChange;

public final class LoadGameEvent extends EventNode<NullState> implements Externalizable {
  private File sav;
  
  public LoadGameEvent() {
  }
  
  public LoadGameEvent(final File sav) {
    this.sav = sav;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.noChange();
  }
  
  @Override
  protected void doEvent() {
    Main.loadGame(this.sav);
    Main.setCurrentAreas(null);
  }
}