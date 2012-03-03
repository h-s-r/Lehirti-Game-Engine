package org.lehirti.events;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.lehirti.Main;
import org.lehirti.res.images.ImageKey;
import org.lehirti.state.GameState;

public abstract class Location extends AbstractEvent {
  private static final Map<Class<Location>, Set<LocationDispatcher>> LOCATION_DISPATCHERS = new HashMap<Class<Location>, Set<LocationDispatcher>>();
  
  public static synchronized void registerDispatcher(final Class<Location> location, final LocationDispatcher dispatcher) {
    Set<LocationDispatcher> dispatchers = LOCATION_DISPATCHERS.get(location);
    if (dispatchers == null) {
      dispatchers = new HashSet<LocationDispatcher>();
      LOCATION_DISPATCHERS.put(location, dispatchers);
    }
    dispatchers.add(dispatcher);
  }
  
  @Override
  public void execute() {
    setBackgroundImage(getBackgroundImageToDisplay());
    
    final Set<LocationDispatcher> dispatchersForThisLocation = LOCATION_DISPATCHERS.get(this.getClass());
    
    final Map<Event, Double> probablityPerEventMap = new HashMap<Event, Double>();
    if (dispatchersForThisLocation != null) {
      for (final LocationDispatcher dispatcher : dispatchersForThisLocation) {
        probablityPerEventMap.putAll(dispatcher.getCurrentEvents());
      }
    }
    if (probablityPerEventMap.isEmpty()) {
      executeNullEvent();
    }
    final List<Event> probabilityAlwaysEvents = getProbabilityAlwaysEvents(probablityPerEventMap);
    if (!probabilityAlwaysEvents.isEmpty()) {
      executeRandomProbabilityAlwaysEvent(probabilityAlwaysEvents);
    } else {
      final Map<Event, Double> eventsToChooseFrom = removeRegularEvents(probablityPerEventMap);
      final double totalProbabilityOfRegularEvents = getTotalProbablility(eventsToChooseFrom.values());
      if (totalProbabilityOfRegularEvents < 100.0D) {
        eventsToChooseFrom.putAll(getDefaultEvents(probablityPerEventMap.keySet(),
            100.0D - totalProbabilityOfRegularEvents));
      } else if (totalProbabilityOfRegularEvents > 100.0D) {
        scaleTotalProbabilityTo100Percent(eventsToChooseFrom, totalProbabilityOfRegularEvents);
      }
      executeRandomRegularOrDefaultEvent(eventsToChooseFrom);
    }
  }
  
  /**
   * the total probability of all events must be totalProbabilityOfEvents (!= 0) before this method call and will be 100
   * after this method call
   * 
   * @param events
   * @param totalProbabilityOfEvents
   */
  private static void scaleTotalProbabilityTo100Percent(final Map<Event, Double> events,
      final double totalProbabilityOfEvents) {
    final double scalingFactor = 100.0D / totalProbabilityOfEvents;
    for (final Map.Entry<Event, Double> entry : events.entrySet()) {
      entry.setValue(Double.valueOf(entry.getValue().doubleValue() * scalingFactor));
    }
  }
  
  private static Map<Event, Double> getDefaultEvents(final Set<Event> defaultEvents, final double remainingProbability) {
    if (defaultEvents.isEmpty()) {
      return Collections.emptyMap();
    }
    final Double probabilityPerDefaultEvent = Double.valueOf(remainingProbability / defaultEvents.size());
    final Map<Event, Double> defaultEventsMap = new HashMap<Event, Double>();
    for (final Event defaultEvent : defaultEvents) {
      defaultEventsMap.put(defaultEvent, probabilityPerDefaultEvent);
    }
    return defaultEventsMap;
  }
  
  private static double getTotalProbablility(final Collection<Double> probabilities) {
    double total = 0.0D;
    for (final Double prob : probabilities) {
      total += prob;
    }
    return total;
  }
  
  /**
   * @param probablityPerEventMap
   * @return regular events
   */
  private static Map<Event, Double> removeRegularEvents(final Map<Event, Double> probablityPerEventMap) {
    final Map<Event, Double> regularEvents = new HashMap<Event, Double>();
    final Iterator<Map.Entry<Event, Double>> itr = probablityPerEventMap.entrySet().iterator();
    while (itr.hasNext()) {
      final Entry<Event, Double> eventEntry = itr.next();
      if (eventEntry.getValue().doubleValue() > LocationDispatcher.PROBABILITY_DEFAULT + 0.5 /* rounding errors */) {
        regularEvents.put(eventEntry.getKey(), eventEntry.getValue());
        itr.remove();
      }
    }
    return regularEvents;
  }
  
  private void executeRandomRegularOrDefaultEvent(final Map<Event, Double> eventsToChooseFrom) {
    double remainingProbabilityFromDieRoll = GameState.DIE.nextDouble() * 100.0D;
    for (final Map.Entry<Event, Double> entry : eventsToChooseFrom.entrySet()) {
      if (remainingProbabilityFromDieRoll < entry.getValue().doubleValue()) {
        Main.nextEvent = entry.getKey();
        return;
      }
      remainingProbabilityFromDieRoll -= entry.getValue().doubleValue();
    }
    executeNullEvent();
  }
  
  private static void executeRandomProbabilityAlwaysEvent(final List<Event> probabilityAlwaysEvents) {
    if (probabilityAlwaysEvents.size() > 1) {
      // TODO log warning; at least one PROBABILITY_ALWAYS has been blocked by another
    }
    Main.nextEvent = probabilityAlwaysEvents.get(GameState.DIE.nextInt(probabilityAlwaysEvents.size()));
  }
  
  private static List<Event> getProbabilityAlwaysEvents(final Map<Event, Double> probablityPerEventMap) {
    final List<Event> probAlwaysEvents = new LinkedList<Event>();
    for (final Map.Entry<Event, Double> entry : probablityPerEventMap.entrySet()) {
      if (entry.getValue().doubleValue() < LocationDispatcher.PROBABILITY_ALWAYS + 0.5 /* rounding errors */) {
        probAlwaysEvents.add(entry.getKey());
      }
    }
    return probAlwaysEvents;
  }
  
  protected void executeNullEvent() {
    // TODO abstract?
  }
  
  protected abstract ImageKey getBackgroundImageToDisplay();
}
