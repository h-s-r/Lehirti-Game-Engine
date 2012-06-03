package org.lehirti.luckysurvivor.npc;

import java.util.LinkedList;
import java.util.List;

import org.lehirti.engine.state.NPC;
import org.lehirti.luckysurvivor.npc.hinata.Hinata;
import org.lehirti.luckysurvivor.npc.tifa.Tifa;

public class CrashSiteNPCs {
  public static List<NPC> getNPCs() {
    final List<NPC> list = new LinkedList<NPC>();
    list.add(new Tifa());
    list.add(new Hinata());
    return list;
  }
}
