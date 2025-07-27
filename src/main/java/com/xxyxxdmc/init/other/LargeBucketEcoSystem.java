package com.xxyxxdmc.init.other;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class LargeBucketEcoSystem {
    private List<NbtCompound> axolotls;
    private List<NbtCompound> pufferfishes;
    private List<NbtCompound> bufferEntities;
    private List<NbtCompound> victims;
    // 首先，这是一个十分复杂的逻辑，关乎到大桶生态系统的运行。
    public void refreshSystemMembers(List<NbtCompound> entities) {
        if (bufferEntities == null || !bufferEntities.equals(entities)) bufferEntities = entities;
        Iterator<NbtCompound> iterator = bufferEntities.iterator();
        while (iterator.hasNext()) {
            NbtCompound entity = iterator.next();
            if (!entity.contains("id")) {
                iterator.remove();
                continue;
            }
            switch (entity.getString("id", "minecraft:cod")) {
                case "minecraft:axolotl" -> this.axolotls.add(entity);
                case "minecraft:pufferfish" -> this.pufferfishes.add(entity);
                default -> this.victims.add(entity);
            }
        }
    }
    public List<NbtCompound> processEntitiesRelationship() {

        return bufferEntities;
    }
    public void processPufferfishState() {

    }

    /**
     * @param state 0 is killer, 1 is victim
     */
    public void processAxolotlState(NbtCompound axolotl, int state) {
        NbtCompound memories = axolotl.getCompound("Brain").flatMap(brain -> brain.getCompound("memories")).orElse(new NbtCompound());
        if (state == 0) {
            NbtCompound cooldown = new NbtCompound();
            cooldown.putBoolean("value", true);
            cooldown.putLong("ttl", 2 * 60 * 20);
            memories.put("minecraft:has_hunting_cooldown", cooldown);
        } else {
            float health = axolotl.getFloat("Health", 1.0F);
            if (health <= 7.0F && Math.random() < 0.3333333333333333) {
                NbtCompound playDead = new NbtCompound();
                playDead.putInt("value", 10 * 20);
                memories.put("minecraft:has_hunting_cooldown", playDead);
            }
        }
    }
    public void processAxolotlBehave(NbtCompound axolotl) {
        NbtCompound brain = axolotl.getCompoundOrEmpty("Brain");
        if (brain.isEmpty()) return;
        NbtCompound memories = brain.getCompoundOrEmpty("memories");
        if (memories.isEmpty()) return;
        if (memories.contains("minecraft:has_hunting_cooldown")
                && memories.getCompound("minecraft:has_hunting_cooldown").isPresent()) {

        }
    }
    public void attack(NbtCompound attacker, NbtCompound victim, boolean additionEffect) {
        float victimHealth = victim.getFloat("Health", 1.0F);
        float amount;
        if (additionEffect && attacker != null && attacker.contains("PuffState") && attacker.getString("id", "minecraft:cod").equals("minecraft:pufferfish")) {
            int puffState = attacker.getInt("PuffState", 0);
            if (puffState != 0) this.attackWithPoison(victim, (puffState == 2) ? 7 * 20 : (puffState == 1) ? 3 * 20 : 0);
            victim.putFloat("Health", victimHealth - 1.0F);
            amount = 1.0F;
        } else if (attacker != null && attacker.getString("id", "minecraft:cod").equals("minecraft:axolotl")) {
            victim.putFloat("Health", victimHealth - 2.0F);
            amount = 2.0F;
        } else {
            victim.putFloat("Health", victimHealth - 1.0F);
            amount = 1.0F;
        }
        if (victim.getString("id", "minecraft:cod").equals("minecraft:axolotl")) processAxolotlState(victim, 1);
        if (victimHealth - amount < 1.0F) victim.putBoolean("Died", true);
    }
    public void processPoisonEffect(NbtCompound victim) {
        NbtList active_effects;
        if (victim.contains("active_effects") && victim.getList("active_effects").isPresent()) active_effects = victim.getList("active_effects").get();
        else return;
        Iterator<NbtElement> iterator = active_effects.iterator();
        while (iterator.hasNext()) {
            NbtElement effect = iterator.next();
            if (effect.asCompound().isPresent()
                    && effect.asCompound().get().contains("id")
                    && effect.asCompound().get().getString("id", "minecraft:hunger").equals("minecraft:poison")) {
                attack(null, victim, false);
                int duration = effect.asCompound().get().getInt("duration", 0);
                if (duration - 25 <= 0) iterator.remove();
                else effect.asCompound().get().putInt("duration", duration - 25);
            }
        }
    }
    public void attackWithPoison(NbtCompound victim, int duration) {
        NbtList active_effects;
        if (victim.contains("active_effects") && victim.getList("active_effects").isPresent()) active_effects = victim.getList("active_effects").get();
        else active_effects = new NbtList();
        NbtCompound poison = new NbtCompound();
        poison.putString("id", "minecraft:poison");
        poison.putInt("duration", duration);
        poison.putByte("amplifier", (byte) 0);
        poison.putBoolean("show_icon", true);
        active_effects.add(poison);
        victim.put("active_effects", active_effects);
    }
}