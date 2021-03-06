package org.lehirti.tools;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lge.state.AbstractState;
import lge.state.BoolState;
import lge.state.IntState;
import lge.state.StringState;
import lge.util.ClassFinder;
import lge.util.FileUtils;
import npc.NPC;
import npc.NPCCommonStats;

public final class GenerateNPCCommon {
  private static final String PREFIX_NPCCommon = "  // BEGIN GENERATED BLOCK NPCCommon";
  private static final String POSTFIX_NPCCommon = "  // END GENERATED BLOCK NPCCommon";
  private static final Pattern NPCCommon_TO_BE_REPLACED = Pattern.compile(PREFIX_NPCCommon + ".*?" + POSTFIX_NPCCommon,
      Pattern.DOTALL);
  
  public static void main(final String[] args) {
    final File root = new File(args[0]);
    if (!root.isDirectory()) {
      System.err.println("invalid root dir: " + args[0]);
      return;
    }
    
    // only update final classes that implement the NPC interface
    final List<Class<?>> npcs = new ClassFinder().findSubclasses(NPC.class).get(NPC.class);
    for (final Class<?> npc : npcs) {
      if (Modifier.isFinal(npc.getModifiers())) {
        updateSource(npc.getName(), root);
      }
    }
  }
  
  private static void updateSource(final String fqcn, final File root) {
    final File sourceFile = new File(root, fqcn.replaceAll("\\.", File.separator) + ".java");
    if (!sourceFile.isFile()) {
      System.err.println(sourceFile.getAbsolutePath() + " is not a file");
    }
    String value = FileUtils.readContentAsString(sourceFile);
    value = transform(value);
    FileUtils.writeContentToFile(sourceFile, value);
  }
  
  private static String transform(final String npcSourceFileContent) {
    final Matcher matcher = NPCCommon_TO_BE_REPLACED.matcher(npcSourceFileContent);
    if (matcher.find()) {
      final String matchingBlock = matcher.group();
      final String newGeneratedBlock = buildGeneratedPart(matchingBlock);
      return matcher.replaceAll(newGeneratedBlock);
    } else {
      System.err.println("No markers " + NPCCommon_TO_BE_REPLACED.toString() + " found in\n" + npcSourceFileContent);
      return npcSourceFileContent;
    }
  }
  
  private static String buildGeneratedPart(final String matchingBlock) {
    final StringBuilder sb = new StringBuilder(PREFIX_NPCCommon + "\n");
    create(sb, matchingBlock, "Str", StringState.class);
    create(sb, matchingBlock, "Int", IntState.class);
    create(sb, matchingBlock, "Bool", BoolState.class);
    create(sb, matchingBlock, "Virtual", null);
    createStatLookupTable(sb);
    createTextResolveMethod(sb);
    sb.append(POSTFIX_NPCCommon);
    return sb.toString();
  }
  
  private static void createTextResolveMethod(final StringBuilder sb) {
    sb.append("  @Override\n");
    sb.append("  public String resolveParameter(final String parameter) throws TextParameterResolutionException {\n");
    sb.append("    return resolveParameter(parameter, STATE_BY_NAME_MAP);\n");
    sb.append("  }\n");
    sb.append("  \n");
  }
  
  private static void createStatLookupTable(final StringBuilder sb) {
    sb.append("  private final static Map<String, AbstractState> STATE_BY_NAME_MAP = new LinkedHashMap<>();\n");
    sb.append("  static {\n");
    createStatLookupTableForOneType(sb, "Str");
    createStatLookupTableForOneType(sb, "Int");
    createStatLookupTableForOneType(sb, "Bool");
    createStatLookupTableForVirtual(sb);
    sb.append("  }\n");
    sb.append("  \n");
  }
  
  private static void createStatLookupTableForOneType(final StringBuilder sb, final String key) {
    sb.append("    for (final AbstractState state : " + key + ".values()) {\n");
    sb.append("      if (STATE_BY_NAME_MAP.containsKey(state.name())) {\n");
    sb.append("        throw new ThreadDeath();\n");
    sb.append("      }\n");
    sb.append("      STATE_BY_NAME_MAP.put(state.name(), state);\n");
    sb.append("    }\n");
  }
  
  private static void createStatLookupTableForVirtual(final StringBuilder sb) {
    sb.append("    for (final Enum<?> state : Virtual.values()) {\n");
    sb.append("      if (STATE_BY_NAME_MAP.containsKey(state.name())) {\n");
    sb.append("        throw new ThreadDeath();\n");
    sb.append("      }\n");
    sb.append("      STATE_BY_NAME_MAP.put(state.name(), null);\n");
    sb.append("    }\n");
  }
  
  private static void create(final StringBuilder sb, final String matchingBlock, final String key,
      final Class<? extends AbstractState> stateClass) {
    sb.append("  public static enum " + key + (stateClass != null ? " implements " + stateClass.getSimpleName() : "")
        + " {\n");
    for (final NPCCommonStats stat : NPCCommonStats.values()) {
      if ((stateClass == null && stat.type == null) || (stateClass != null && stateClass.equals(stat.type))) {
        sb.append("    " + stat.name() + ",\n");
      }
    }
    final String prefix = "    // BEGIN MANUAL BLOCK " + key;
    final String postfix = "    // END MANUAL BLOCK " + key;
    final Pattern local_TO_BE_REPLACED = Pattern.compile(prefix + ".*?" + postfix, Pattern.DOTALL);
    final Matcher matcher = local_TO_BE_REPLACED.matcher(matchingBlock);
    if (matcher.find()) {
      sb.append(matcher.group());
      sb.append("\n");
    } else {
      sb.append("    // BEGIN MANUAL BLOCK " + key + "\n");
      sb.append("    // END MANUAL BLOCK " + key + "\n");
    }
    sb.append("  }\n");
    sb.append("  \n");
  }
}
