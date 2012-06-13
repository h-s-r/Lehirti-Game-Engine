package org.lehirti.luckysurvivor.sss;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.lehirti.engine.events.Event;
import org.lehirti.engine.events.EventNode;
import org.lehirti.engine.events.Option;
import org.lehirti.engine.events.Event.NullState;
import org.lehirti.engine.res.images.ImgChange;
import org.lehirti.engine.res.text.TextWrapper;
import org.lehirti.luckysurvivor.npc.NPC;

public class ProposeSexActEvent extends EventNode<NullState> implements Externalizable {
  
  private NPC npc;
  private SexAct act;
  private SexToy toy;
  private Event<?> returnEvent;
  
  // for saving/loading
  public ProposeSexActEvent() {
    this(null, null, null, null);
  }
  
  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    this.npc = (NPC) in.readObject();
    this.act = (SexAct) in.readObject();
    this.toy = (SexToy) in.readObject();
    this.returnEvent = (Event<?>) in.readObject();
  }
  
  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(this.npc);
    out.writeObject(this.act);
    out.writeObject(this.toy);
    out.writeObject(this.returnEvent);
  }
  
  public ProposeSexActEvent(final NPC npc, final SexAct act, final SexToy toy, final Event<?> returnEvent) {
    this.npc = npc;
    this.act = act;
    this.toy = toy;
    this.returnEvent = returnEvent;
  }
  
  @Override
  protected ImgChange updateImageArea() {
    return ImgChange.setFG(this.npc.getReactionImage(this.act, this.toy));
  }
  
  @Override
  protected void doEvent() {
    setText(this.act);
    
    for (final TextWrapper txtWrp : this.npc.getReactionText(this.act, this.toy)) {
      addText(txtWrp);
    }
    
    for (final Option option : this.npc.getReactionOptions(this.act, this.toy, this.returnEvent)) {
      addOption(option.key, option.text, option.event);
    }
  }
}