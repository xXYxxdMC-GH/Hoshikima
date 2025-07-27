package com.xxyxxdmc.init.other;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LargeBucketEcoSystem {
    private static final String ID_KEY = "id";
    private static final String HEALTH_KEY = "Health";
    private static final String BRAIN_KEY = "Brain";
    private static final String MEMORIES_KEY = "memories";
    private static final String ACTIVE_EFFECTS_KEY = "active_effects";
    private static final String DIED_KEY = "Died";
    private static final String PUFF_STATE_KEY = "PuffState";
    private static final String PUFF_COOLDOWN_KEY = "PuffCooldown"; // 新增：用于记录河豚状态切换的冷却

    private static final String AXOLOTL_ID = "minecraft:axolotl";
    private static final String PUFFERFISH_ID = "minecraft:pufferfish";
    private static final String HUNTING_COOLDOWN_MEMORY = "minecraft:has_hunting_cooldown";
    private static final String PLAY_DEAD_TICKS_MEMORY = "minecraft:play_dead_ticks";

    private static final int TICKS_PER_SECOND = 20;
    private static final int PUFF_UP_COOLDOWN_TICKS = 5 * TICKS_PER_SECOND;

    private List<NbtCompound> bufferEntities;
    private final List<NbtCompound> axolotls;
    private final List<NbtCompound> pufferfishes;
    private final List<NbtCompound> victims;

    public LargeBucketEcoSystem() {
        this.axolotls = new ArrayList<>();
        this.pufferfishes = new ArrayList<>();
        this.victims = new ArrayList<>();
    }
    // 首先，这是一个十分复杂的逻辑，关乎到大桶生态系统的运行。
    public void refreshSystemMembers(List<NbtCompound> entities) {
        if (bufferEntities != null && bufferEntities.equals(entities)) return;
        if (bufferEntities == null) bufferEntities = entities;
        Iterator<NbtCompound> iterator = bufferEntities.iterator();
        while (iterator.hasNext()) {
            NbtCompound entity = iterator.next();
            if (!entity.contains(ID_KEY)) {
                iterator.remove();
                continue;
            }
            switch (entity.getString(ID_KEY, "minecraft:cod")) {
                case AXOLOTL_ID -> this.axolotls.add(entity);
                case PUFFERFISH_ID -> this.pufferfishes.add(entity);
                default -> this.victims.add(entity);
            }
        }
    }
    private void processPufferfishBehavior() {
        for (int i = 0; i < this.bufferEntities.size(); i++) {
            NbtCompound currentEntity = this.bufferEntities.get(i);
            if (!PUFFERFISH_ID.equals(currentEntity.getString(ID_KEY, ""))) {
                continue;
            }

            int cooldown = currentEntity.getInt(PUFF_COOLDOWN_KEY, 0);
            if (cooldown > 0) {
                currentEntity.putInt(PUFF_COOLDOWN_KEY, cooldown - 1);
            }

            boolean axolotlAdjacent = i > 0 && AXOLOTL_ID.equals(this.bufferEntities.get(i - 1).getString(ID_KEY, ""));

            if (i < this.bufferEntities.size() - 1 && AXOLOTL_ID.equals(this.bufferEntities.get(i + 1).getString(ID_KEY, ""))) {
                axolotlAdjacent = true;
            }

            int puffState = currentEntity.getInt(PUFF_STATE_KEY, 0);
            if (axolotlAdjacent) {
                if (cooldown <= 0 && puffState < 2) {
                    currentEntity.putInt(PUFF_STATE_KEY, puffState + 1);
                    currentEntity.putInt(PUFF_COOLDOWN_KEY, PUFF_UP_COOLDOWN_TICKS);
                }
            } else {
                if (cooldown <= 0 && puffState > 0) {
                    currentEntity.putInt(PUFF_STATE_KEY, puffState - 1);
                    currentEntity.putInt(PUFF_COOLDOWN_KEY, PUFF_UP_COOLDOWN_TICKS);
                }
            }
        }
    }

    /**
     * @param state 0 is killer, 1 is victim
     */
    public void processAxolotlState(NbtCompound axolotl, int state) {
        if (!axolotl.contains(BRAIN_KEY, NbtElement.COMPOUND_TYPE)) return;
        NbtCompound brain = axolotl.getCompound(BRAIN_KEY);
        if (!brain.contains(MEMORIES_KEY, NbtElement.COMPOUND_TYPE)) return;
        NbtCompound memories = brain.getCompound(MEMORIES_KEY);

        if (state == 0) {
            NbtCompound cooldown = new NbtCompound();
            cooldown.putBoolean("value", true);
            cooldown.putLong("ttl", 2 * 60 * 20);
            memories.put(HUNTING_COOLDOWN_MEMORY, cooldown);
        } else {
            float health = axolotl.getFloat(HEALTH_KEY, 1.0F);
            if (health <= 7.0F && Math.random() < 0.3333333333333333) {
                NbtCompound playDead = new NbtCompound();
                playDead.putInt("value", 10 * 20);
                memories.put(PLAY_DEAD_TICKS_MEMORY, playDead);
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
    public void attack(@Nullable NbtCompound attacker, NbtCompound victim, boolean additionEffect) {
        if (victim.getBoolean(DIED_KEY, false)) return;

        float victimHealth = victim.getFloat(HEALTH_KEY, 0.0F);
        float damageAmount = 1.0F;

        if (attacker != null) {
            String attackerId = attacker.getString(ID_KEY, "");
            if (AXOLOTL_ID.equals(attackerId)) {
                damageAmount = 2.0F;
            } else if (PUFFERFISH_ID.equals(attackerId) && additionEffect) {
                int puffState = attacker.getInt(PUFF_STATE_KEY, 0);
                if (puffState > 0) {
                    int poisonDuration = (puffState == 2) ? 7 * TICKS_PER_SECOND : 3 * TICKS_PER_SECOND;
                    this.attackWithPoison(victim, poisonDuration);
                }
            }
        }

        float newHealth = victimHealth - damageAmount;
        victim.putFloat(HEALTH_KEY, newHealth);

        if (AXOLOTL_ID.equals(victim.getString(ID_KEY, ""))) {
            processAxolotlState(victim, 1);
        }

        if (newHealth < 1.0F) {
            victim.putBoolean(DIED_KEY, true);
        }
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
        if (duration <= 0) return;

        NbtList activeEffects = victim.contains(ACTIVE_EFFECTS_KEY)
                ? victim.getList(ACTIVE_EFFECTS_KEY).get()
                : new NbtList();

        activeEffects.removeIf(element ->
                element instanceof NbtCompound compound && "minecraft:poison".equals(compound.getString(ID_KEY, ""))
        );

        NbtCompound poison = new NbtCompound();
        poison.putString("id", "minecraft:poison");
        poison.putInt("duration", duration);
        poison.putByte("amplifier", (byte) 0);
        poison.putBoolean("show_icon", true);
        activeEffects.add(poison);
        victim.put("active_effects", activeEffects);
    }
}