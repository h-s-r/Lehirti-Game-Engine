package org.lehirti.luckysurvivor.peninsulaisthmus;

import java.util.HashMap;
import java.util.Map;

import org.lehirti.engine.events.AbstractEvent;
import org.lehirti.engine.events.Event;
import org.lehirti.engine.events.EventHook;
import org.lehirti.engine.state.State;

public final class ArriveFirstTimeHook implements EventHook {
  static {
    AbstractEvent.registerHook(MapToPeninsulaIsthmus.class, new ArriveFirstTimeHook());
  }
  
  @Override
  public Map<Event<?>, Double> getCurrentEvents(final Event<?> previousEvent) {
    final Map<Event<?>, Double> events = new HashMap<Event<?>, Double>();
    
    // first time you come to MapToPeninsulaIsthmus, the ArriveFirstTime one-time event will show instead
    if (State.getEventCount(ArriveFirstTime.class) == 0) {
      events.put(new ArriveFirstTime(), Double.valueOf(EventHook.PROBABILITY_ALWAYS));
    }
    return events;
  }
}
