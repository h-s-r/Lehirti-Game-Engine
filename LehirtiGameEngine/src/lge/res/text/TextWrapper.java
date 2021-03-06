package lge.res.text;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lge.res.ResourceCache;
import lge.res.ResourceState;
import lge.state.BoolState;
import lge.state.IntState;
import lge.state.ObjState;
import lge.state.State;
import lge.state.StringState;
import lge.util.ClassFinder;
import lge.util.ClassFinder.ClassWorker;
import lge.util.ClassFinder.SuperClass;
import lge.util.FileUtils;
import lge.util.PathFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextWrapper implements Externalizable {
  private static final long serialVersionUID = 1L;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TextWrapper.class);
  
  private static final String TEXT_DELIMITER = "\n\\$\n";
  
  private static final Collection<TextParameterNPCResolver> TEXT_PARAMETER_RESOLVER;
  
  /**
   * When replacing parameters with their respective values, the first character will be capitalized, if the previous
   * non-whitespace character in the string is one in this Collection.
   */
  private static final Collection<Character> SENTENCE_END_CHARACTERS;
  static {
    final Set<Character> s = new HashSet<>();
    s.add(null); // special case for "first sentence in text"
    s.add(Character.valueOf('.'));
    s.add(Character.valueOf(':'));
    s.add(Character.valueOf('!'));
    s.add(Character.valueOf('?'));
    SENTENCE_END_CHARACTERS = Collections.unmodifiableCollection(s);
  }
  
  static {
    final List<TextParameterNPCResolver> c = new LinkedList<>();
    ClassFinder.workWithClasses(new ClassWorker() {
      
      @Override
      public void doWork(final List<Class<?>> npcs) {
        for (final Class<?> npc : npcs) {
          if (!Modifier.isAbstract(npc.getModifiers())) {
            try {
              c.add((TextParameterNPCResolver) npc.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
      }
      
      @Override
      public SuperClass getSuperClass() {
        return SuperClass.TEXT_PARAMETER_NPC_RESOLVER;
      }
      
      @Override
      public String getDescription() {
        return "[Text Parameter Resolver Loader]";
      }
    });
    
    TEXT_PARAMETER_RESOLVER = Collections.unmodifiableCollection(c);
  }
  
  private TextKey key;
  
  /** as stored on disc; may contain alternative texts and be different from what's on screen */
  private String[] rawValues = new String[1];
  /** selected text alternative; possibly with place holders */
  private String rawValue;
  private int selectedRawValue = 0;
  
  private final List<Object> parameters = new LinkedList<>();
  
  private final ResourceState state;
  
  // for saving/loading
  public TextWrapper() {
    this.state = ResourceState.LOADED;
  }
  
  public TextWrapper(final TextKey key, final boolean logModCoreDiff) {
    this.key = key;
    // for now, store text directly in the res dir
    final File modFile = PathFinder.getModFile(this.key);
    if (logModCoreDiff) {
      if (modFile.canRead()) {
        System.out.println(this.key.getClass().getName() + "." + this.key.name());
        this.rawValues = splitTextAlternatives(FileUtils.readContentAsString(modFile));
        this.selectedRawValue = State.DIE.nextInt(this.rawValues.length);
        this.state = ResourceState.MOD;
        final File coreFile = PathFinder.getCoreFile(this.key);
        if (coreFile.canRead()) {
          final String[] coreRawValues = splitTextAlternatives(FileUtils.readContentAsString(coreFile));
          final int maxSize = coreRawValues.length > this.rawValues.length ? coreRawValues.length
              : this.rawValues.length;
          for (int i = 0; i < maxSize; i++) {
            if (coreRawValues.length > i) {
              System.out.println("CORE: " + coreRawValues[i].replaceAll("\\n", "\\\\n"));
            }
            if (this.rawValues.length > i) {
              System.out.println("MOD:  " + this.rawValues[i].replaceAll("\\n", "\\\\n"));
            }
            if (coreRawValues.length > i && this.rawValues.length > i) {
              final String coreRawValue = coreRawValues[i];
              final String modRawValue = this.rawValues[i];
              final int length = coreRawValue.length() < modRawValue.length() ? coreRawValue.length() : modRawValue
                  .length();
              final StringBuilder sb = new StringBuilder();
              for (int k = 0; k < length; k++) {
                if (coreRawValue.charAt(k) == modRawValue.charAt(k)) {
                  sb.append(' ');
                } else {
                  sb.append('*');
                }
              }
              final int lengthDiff = Math.abs(modRawValue.length() - coreRawValue.length());
              for (int k = 0; k < lengthDiff; k++) {
                sb.append('+');
              }
              if (sb.toString().trim().length() == 0) {
                System.out.println("NO DIFF");
              } else {
                System.out.println("DIFF: " + sb.toString());
              }
            }
          }
        } else {
          for (final String modRawValue : this.rawValues) {
            System.out.println("NEW:  " + modRawValue.replaceAll("\\n", "\\\\n"));
          }
        }
        System.out.println();
      } else {
        final File coreFile = PathFinder.getCoreFile(this.key);
        if (coreFile.canRead()) {
          this.rawValues = splitTextAlternatives(FileUtils.readContentAsString(coreFile));
          this.selectedRawValue = State.DIE.nextInt(this.rawValues.length);
          this.state = ResourceState.CORE;
        } else {
          this.selectedRawValue = 0;
          this.rawValues[this.selectedRawValue] = "Ctrl-t: " + key.getClass().getSimpleName() + "." + key.name()
              + "\n\n";
          this.state = ResourceState.MISSING;
        }
      }
    } else {
      if (modFile.canRead()) {
        this.rawValues = splitTextAlternatives(FileUtils.readContentAsString(modFile));
        this.selectedRawValue = State.DIE.nextInt(this.rawValues.length);
        this.state = ResourceState.MOD;
      } else {
        final File coreFile = PathFinder.getCoreFile(this.key);
        if (coreFile.canRead()) {
          this.rawValues = splitTextAlternatives(FileUtils.readContentAsString(coreFile));
          this.selectedRawValue = State.DIE.nextInt(this.rawValues.length);
          this.state = ResourceState.CORE;
        } else {
          this.selectedRawValue = 0;
          this.rawValues[this.selectedRawValue] = "Ctrl-t: " + key.getClass().getSimpleName() + "." + key.name()
              + "\n\n";
          this.state = ResourceState.MISSING;
        }
      }
    }
    this.rawValue = this.rawValues[this.selectedRawValue];
  }
  
  private static String[] splitTextAlternatives(final String rawTextsFromFile) {
    return rawTextsFromFile.split("\\Q" + TEXT_DELIMITER + "\\E");
  }
  
  private static String joinTextAlternatives(final String[] rawValuesToJoin) {
    final StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (final String value : rawValuesToJoin) {
      if (value != null) {
        if (!isFirst) {
          sb.append(TEXT_DELIMITER);
        } else {
          isFirst = false;
        }
        sb.append(value);
      }
    }
    return sb.toString();
  }
  
  public String getRawValue() {
    return this.rawValue;
  }
  
  public String[] getRawValues() {
    return this.rawValues;
  }
  
  public ResourceState getResourceState() {
    return this.state;
  }
  
  public String getValue() {
    String val = this.rawValue;
    int fromIndex = 0;
    while ((fromIndex = val.indexOf("{", fromIndex)) != -1) {
      final int toIndex = val.indexOf("}", fromIndex + 1);
      final String parameter = val.substring(fromIndex + 1, toIndex);
      String replacement;
      try {
        replacement = getParameterReplacement(parameter, this.parameters, toString());
        replacement = toUppercaseFirstCharacterIfNeccessary(replacement, val, fromIndex);
        LOGGER.debug("Parameter \"{}\" found in \"{}\"", parameter, val);
        val = val.replace("{" + parameter + "}", replacement);
      } catch (final TextParameterResolutionException e) {
        val = val.replace("{" + parameter + "}", e.getMessage());
      }
    }
    return val;
  }
  
  private static String toUppercaseFirstCharacterIfNeccessary(final String replacement, final String val,
      final int fromIndex) {
    final Character prevNonWhitespaceChar = getPreviousNonWhitespaceCharacter(val, fromIndex - 1);
    if (SENTENCE_END_CHARACTERS.contains(prevNonWhitespaceChar)) {
      return toUppercaseFirstCharacter(replacement);
    } else {
      return replacement;
    }
  }
  
  private static Character getPreviousNonWhitespaceCharacter(final String string, final int index) {
    if (index < 0) {
      return null;
    }
    final char charAt = string.charAt(index);
    if (Character.isWhitespace(charAt)) {
      return getPreviousNonWhitespaceCharacter(string, index - 1);
    }
    return Character.valueOf(charAt);
  }
  
  private static String toUppercaseFirstCharacter(final String string) {
    if (string.length() == 0) {
      return string; // nothing to do
    } else if (string.length() == 1) {
      return string.toUpperCase(); // only one character, so just toUpper the whole string
    } else {
      return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
  }
  
  public static String getParameterReplacement(final String parameter, final List<Object> parameters,
      final String idForLogging) throws TextParameterResolutionException {
    // try explicitly set parameters
    try {
      final int parsedInt = Integer.parseInt(parameter);
      if (parsedInt >= 0 && parsedInt < parameters.size()) {
        final Object param = parameters.get(parsedInt);
        if (param instanceof String) {
          return (String) param;
        } else {
          return ((TextWrapper) param).getValue();
        }
      } else {
        LOGGER.error("{} is missing explicitly set parameter #{}", idForLogging, Integer.valueOf(parsedInt));
        return "[MISSING PARAMETER #" + parameter + "]"; // we can't check this statically, so no exception here
      }
    } catch (final NumberFormatException ignore) {
      // is not an explicitly set parameter; go on trying other possibilities
    }
    
    // try text parameter resolvers
    final int indexOfHash = parameter.indexOf("#");
    if (indexOfHash != -1) {
      final String prefix = parameter.substring(0, indexOfHash + 1);
      TextParameterNPCResolver textParameterResolver = null;
      if (prefix.equals("#")) {
        textParameterResolver = TextParameterNPCResolver.Current.get();
      } else {
        for (final TextParameterNPCResolver resolver : TEXT_PARAMETER_RESOLVER) {
          if (resolver.getParameterPrefix().equals(prefix)) {
            textParameterResolver = resolver;
            break;
          }
        }
      }
      if (textParameterResolver != null) {
        return textParameterResolver.resolveParameter(parameter.substring(indexOfHash + 1));
      } else {
        if (prefix.equals("#")) {
          // "Current" NPC is not statically checkable
          LOGGER.error("{} is refering to Current NPC, which is currently not set", idForLogging);
          return "[Current NPC not set; cannot resolve " + parameter + "]";
        } else {
          // Specific NPC is missing
          LOGGER.error("{} is refering to non-existent NPC {}", idForLogging, prefix);
          throw new TextParameterResolutionException("[Text Parameter Resolver " + prefix + " is missing]");
        }
      }
    }
    
    String parameterWOPrefix;
    boolean mustBeVariable;
    if (parameter.startsWith("V:")) {
      parameterWOPrefix = parameter.substring(2);
      mustBeVariable = true;
    } else {
      parameterWOPrefix = parameter;
      mustBeVariable = false;
    }
    
    // try expanded FQCN
    final int endOfClassname = parameterWOPrefix.lastIndexOf(".");
    if (endOfClassname != -1) {
      final String className = parameterWOPrefix.substring(0, endOfClassname);
      final String name = parameterWOPrefix.substring(endOfClassname + 1);
      try {
        final Enum parameterValue = Enum.valueOf((Class<? extends Enum>) Class.forName(className), name);
        if (parameterValue instanceof TextKey && !mustBeVariable) {
          return ResourceCache.get((TextKey) parameterValue).getValue();
        } else if (parameterValue instanceof StringState) {
          return State.get((StringState) parameterValue);
        } else if (parameterValue instanceof IntState) {
          return String.valueOf(State.get((IntState) parameterValue));
        } else if (parameterValue instanceof ObjState) {
          return State.get((ObjState) parameterValue).toString();
        } else if (parameterValue instanceof BoolState) {
          return String.valueOf(State.is((BoolState) parameterValue));
        }
      } catch (final ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    LOGGER.error("Unable to resolve parameter \"{}\" for {}", parameter, idForLogging);
    throw new TextParameterResolutionException("[CANNOT RESOLVE PARAMETER \"" + parameter + "\"]");
  }
  
  public void setRawValues(final String[] rawValues, final String contentDir) throws TextParameterResolutionException {
    
    // check for static errors in texts
    for (final String val : rawValues) {
      int fromIndex = 0;
      while ((fromIndex = val.indexOf("{", fromIndex)) != -1) {
        final int toIndex = val.indexOf("}", fromIndex + 1);
        final String parameter = val.substring(fromIndex + 1, toIndex);
        getParameterReplacement(parameter, this.parameters, toString());
        fromIndex++;
      }
    }
    
    this.rawValues = rawValues;
    this.selectedRawValue = State.DIE.nextInt(this.rawValues.length);
    this.rawValue = this.rawValues[this.selectedRawValue];
    FileUtils.writeContentToFile(PathFinder.getModFile(this.key, contentDir), joinTextAlternatives(this.rawValues));
  }
  
  public void addParameter(final String param) {
    this.parameters.add(param);
  }
  
  public void addParameter(final TextWrapper param) {
    this.parameters.add(param);
  }
  
  /**
   * @return this TextWrapper and all (recursively) contained (as parameters) TextWrappers
   */
  public List<TextWrapper> getAllTexts() {
    final List<TextWrapper> allTexts = new LinkedList<>();
    allTexts.add(this);
    for (final Object param : this.parameters) {
      if (param instanceof TextWrapper) {
        allTexts.addAll(((TextWrapper) param).getAllTexts());
      }
    }
    return allTexts;
  }
  
  public TextKey getTextKey() {
    return this.key;
  }
  
  @Override
  public int hashCode() {
    return this.key.hashCode();
  }
  
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof TextWrapper)) {
      return false;
    }
    final TextWrapper other = (TextWrapper) o;
    return this.key.equals(other.key);
  }
  
  @Override
  public String toString() {
    return this.key.getClass().getSimpleName() + "." + this.key.name() + "[" + this.rawValue + "]";
  }
  
  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    // TODO make loading more robust to missing key enum constant
    final String className = (String) in.readObject();
    final String name = (String) in.readObject();
    this.key = (TextKey) Enum.valueOf((Class<? extends Enum>) Class.forName(className), name);
    this.rawValues = (String[]) in.readObject();
    this.selectedRawValue = in.readInt();
    this.rawValue = (String) in.readObject();
    final int nrOfParams = in.readInt();
    this.parameters.clear();
    for (int i = 0; i < nrOfParams; i++) {
      this.parameters.add(in.readObject());
    }
  }
  
  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeObject(this.key.getClass().getName());
    out.writeObject(this.key.name());
    out.writeObject(this.rawValues);
    out.writeInt(this.selectedRawValue);
    out.writeObject(this.rawValue);
    out.writeInt(this.parameters.size());
    for (final Object param : this.parameters) {
      out.writeObject(param);
    }
  }
}
