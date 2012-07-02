package org.lehirti.luckysurvivor.peninsulabasinjungle;

import java.util.HashMap;
import java.util.Map;

import org.lehirti.engine.events.AbstractEvent;
import org.lehirti.engine.events.Event;
import org.lehirti.engine.events.hooks.EventHook;
import org.lehirti.engine.state.State;

public final class SusanVineRapeHook implements EventHook {
  static {
    AbstractEvent.registerHook(MapToPeninsulaBasinJungle.class, new SusanVineRapeHook());
  }
  
  @Override
  public Map<Event<?>, Double> getCurrentEvents(Event<?> previousEvent) {
    final Map<Event<?>, Double> events = new HashMap<Event<?>, Double>();
    
    // this is how you inject a one-time event (in this case into MapToPeninsulaBasinJungle)
    if (State.getEventCount(SusanVineRape.class) == 0) {
      events.put(new SusanVineRape(), Double.valueOf(EventHook.PROBABILITY_ALWAYS));
    }
    
    return events;
  }
  
}