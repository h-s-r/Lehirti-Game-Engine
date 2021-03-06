package lge.state;

import javax.annotation.Nonnull;

import lge.gui.EngineMain;
import lge.res.ResourceCache;
import lge.res.text.CommonText;
import lge.res.text.TextKey;
import lge.res.text.TextWrapper;

public enum DateTime implements IntState {
  FIRST_DAY,
  
  DAY,
  
  HOUR,
  MINUTE,
  SECOND;
  
  public enum DayOfWeek implements TextKey {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;
  }
  
  public enum DayPhase {
    DAYLIGHT,
    DUSK_DAWN,
    NIGHT;
  }
  
  public static void setFirstDayOfWeek(final DayOfWeek firstDay) {
    State.set(FIRST_DAY, firstDay.ordinal());
  }
  
  public static DayOfWeek getFirstDayOfWeek() {
    return DayOfWeek.values()[(int) State.get(FIRST_DAY)];
  }
  
  @Nonnull
  public static DayOfWeek getCurrentDayOfWeek() {
    int currentDayOfWeek = (int) State.get(FIRST_DAY);
    currentDayOfWeek += (int) State.get(DAY);
    final DayOfWeek[] daysOfWeek = DayOfWeek.values();
    currentDayOfWeek %= daysOfWeek.length;
    return daysOfWeek[currentDayOfWeek];
  }
  
  public static void init(final DayOfWeek firstDay, final int day, final int hour, final int minute, final int second) {
    setFirstDayOfWeek(firstDay);
    State.set(DAY, day);
    State.set(HOUR, hour);
    State.set(MINUTE, minute);
    State.set(SECOND, second);
    updateScreen();
  }
  
  public static void advanceBy(final int DDhhmmss) {
    final int seconds = DDhhmmss % 100;
    final int DDhhmm = DDhhmmss / 100;
    int minutes = DDhhmm % 100;
    final int DDhh = DDhhmm / 100;
    int hours = DDhh % 100;
    int days = DDhh / 100;
    
    long newSeconds = State.get(SECOND) + seconds;
    while (newSeconds >= 60) {
      newSeconds -= 60;
      minutes++;
    }
    State.set(SECOND, newSeconds);
    
    long newMinutes = State.get(MINUTE) + minutes;
    while (newMinutes >= 60) {
      newMinutes -= 60;
      hours++;
    }
    State.set(MINUTE, newMinutes);
    
    long newHours = State.get(HOUR) + hours;
    while (newHours >= 24) {
      newHours -= 24;
      days++;
    }
    State.set(HOUR, newHours);
    
    State.set(DAY, State.get(DAY) + days);
    
    updateScreen();
  }
  
  /**
   * advance time to next hhmmss, possible advancing the day; will do nothing if current hhmmss equals parameter hhmmss
   * 
   * @param hhmmss
   */
  public static void advanceTo(final int hhmmss) {
    final long currenthhmmss = (State.get(HOUR) * 10000) + (State.get(MINUTE) * 100) + State.get(SECOND);
    if (hhmmss <= currenthhmmss) {
      State.set(DAY, State.get(DAY) + 1);
    }
    
    final int seconds = hhmmss % 100;
    final int hhmm = hhmmss / 100;
    final int minutes = hhmm % 100;
    final int hours = hhmm / 100;
    State.set(HOUR, hours);
    State.set(MINUTE, minutes);
    State.set(SECOND, seconds);
    updateScreen();
  }
  
  /**
   * advance time to next week day toDay and time hhmmss; will do nothing if current datetime equals parameter datetime
   * 
   * @param toDay
   * @param hhmmss
   */
  public static void advanceTo(final DayOfWeek toDay, final int hhmmss) {
    advanceTo(hhmmss);
    while (toDay != getCurrentDayOfWeek()) {
      State.set(DAY, State.get(DAY) + 1);
    }
    updateScreen();
  }
  
  private static void updateScreen() {
    EngineMain.STATS_AREA.repaint();
  }
  
  public static String getDateFormatedForStatsArea() {
    if (State.get(DAY) == 0L && State.get(HOUR) == 0L && State.get(MINUTE) == 0L && State.get(SECOND) == 0L) {
      return "";
    }
    
    final TextWrapper day = ResourceCache.get(CommonText.DAY);
    day.addParameter(String.valueOf(State.get(DAY)));
    final StringBuilder sb = new StringBuilder(day.getValue());
    sb.append(ResourceCache.get(getCurrentDayOfWeek()).getValue());
    sb.append(ResourceCache.get(CommonText.BLANK).getValue());
    final TextWrapper time = ResourceCache.get(CommonText.TIME_FORMAT);
    time.addParameter(padTo2Digits(State.get(HOUR)));
    time.addParameter(padTo2Digits(State.get(MINUTE)));
    time.addParameter(padTo2Digits(State.get(SECOND)));
    sb.append(time.getValue());
    return sb.toString();
  }
  
  private static String padTo2Digits(final long l) {
    return l < 10 ? "0" + l : String.valueOf(l);
  }
  
  public static int gethhmmss() {
    int hhmmss = 0;
    hhmmss += State.get(HOUR);
    hhmmss *= 100;
    hhmmss += State.get(MINUTE);
    hhmmss *= 100;
    hhmmss += State.get(SECOND);
    return hhmmss;
  }
  
  public static int getDay() {
    return (int) State.get(DAY);
  }
  
  public static int getDDhhmmss() {
    int DDhhmmss = 0;
    DDhhmmss += State.get(DAY);
    DDhhmmss *= 1000000;
    DDhhmmss += gethhmmss();
    return DDhhmmss;
  }
  
  public static DayPhase getDayPhase() {
    final int hhmmss = gethhmmss();
    if (hhmmss < 60000) {
      return DayPhase.NIGHT;
    } else if (hhmmss < 80000) {
      return DayPhase.DUSK_DAWN;
    } else if (hhmmss < 180000) {
      return DayPhase.DAYLIGHT;
    } else if (hhmmss < 200000) {
      return DayPhase.DUSK_DAWN;
    } else {
      return DayPhase.NIGHT;
    }
  }
}
