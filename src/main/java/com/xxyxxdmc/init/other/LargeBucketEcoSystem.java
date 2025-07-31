package com.xxyxxdmc.init.other;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LargeBucketEcoSystem {
    public record EcosystemTickResult(List<NbtCompound> updatedEntities, List<NbtCompound> deadEntities, List<NbtCompound> allEntities) {}
    public record ClearResult(List<NbtCompound> allEntities, List<NbtCompound> deadEntities) {}
    private static final String ID_KEY = "id";
    private static final String HEALTH_KEY = "Health";
    private static final String BRAIN_KEY = "Brain";
    private static final String MEMORIES_KEY = "memories";
    private static final String ACTIVE_EFFECTS_KEY = "active_effects";
    private static final String DIED_KEY = "Died";
    private static final String PUFF_STATE_KEY = "PuffState";
    private static final String PUFF_COOLDOWN_KEY = "PuffCooldown";
    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";

    private static final String AXOLOTL_ID = "minecraft:axolotl";
    private static final String PUFFERFISH_ID = "minecraft:pufferfish";
    private static final String HUNTING_COOLDOWN_MEMORY = "minecraft:has_hunting_cooldown";
    private static final String PLAY_DEAD_TICKS_MEMORY = "minecraft:play_dead_ticks";
    private static final List<String> VICTIMS = List.of("minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", PUFFERFISH_ID, "minecraft:tadpole");

    public static final String EMPTY_ID = "hoshikima:empty";

    private static final int TICKS_PER_SECOND = 20;
    private static final int PUFF_UP_COOLDOWN_TICKS = 5 * TICKS_PER_SECOND;
    private static final int ATTACK_COOLDOWN_TICKS = TICKS_PER_SECOND;
    private static final double MOVE_CHANCE_PER_TICK = 0.05;

    private final Random random = new Random();

    public EcosystemTickResult processTick(List<NbtCompound> currentEntities, int capacity) {
        if (currentEntities.isEmpty()) {
            return new EcosystemTickResult(List.of(), List.of(), List.of());
        }
        if (capacity <= 0) {
            return new EcosystemTickResult(List.of(), new ArrayList<>(currentEntities), List.of());
        }

        List<NbtCompound> entitiesToProcess = new ArrayList<>(currentEntities);
        List<NbtCompound> ejectedEntities = new ArrayList<>();

        if (entitiesToProcess.size() > capacity) {
            ejectedEntities.addAll(entitiesToProcess.subList(capacity, entitiesToProcess.size()));
            entitiesToProcess = new ArrayList<>(entitiesToProcess.subList(0, capacity));
        }

        List<NbtCompound> slots = prepareSlots(entitiesToProcess, capacity);

        processMovement(slots);
        processPufferfishBehavior(slots);
        processAxolotlBehavior(slots);

        processEntityTimers(slots);

        ClearResult clearResult = cleanupDeadEntities(slots);

        List<NbtCompound> deadAndEjectedThisTick = clearResult.deadEntities();
        deadAndEjectedThisTick.addAll(ejectedEntities);

        List<NbtCompound> allFinalSlots = clearResult.allEntities();

        List<NbtCompound> finalLivingEntities = allFinalSlots.stream()
                .filter(s -> !isEmpty(s))
                .collect(Collectors.toList());

        return new EcosystemTickResult(finalLivingEntities, deadAndEjectedThisTick, allFinalSlots);
    }

    public static NbtCompound createEmptyEntity() {
        NbtCompound empty = new NbtCompound();
        empty.putString(ID_KEY, EMPTY_ID);
        return empty;
    }

    public static boolean isEmpty(@Nullable NbtCompound entity) {
        return entity == null || EMPTY_ID.equals(entity.getString(ID_KEY, ""));
    }

    private List<NbtCompound> prepareSlots(List<NbtCompound> currentEntities, int capacity) {
        List<NbtCompound> prepared = new ArrayList<>(currentEntities);

        while (prepared.size() < capacity) {
            prepared.add(createEmptyEntity());
        }
        return prepared;
    }

    private void processMovement(List<NbtCompound> slots) {
        List<Integer> emptySlotIndices = IntStream.range(0, slots.size())
                .filter(i -> isEmpty(slots.get(i)))
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        if (emptySlotIndices.isEmpty()) return;
        for (int i = 0; i < slots.size(); i++) {
            if (!isEmpty(slots.get(i)) && !slots.get(i).getBoolean(DIED_KEY, false) && random.nextDouble() < MOVE_CHANCE_PER_TICK && !slots.get(i).getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY).contains(PLAY_DEAD_TICKS_MEMORY)) {
                if (VICTIMS.contains(slots.get(i).getString("id", "")) && random.nextDouble() > 0.08) continue;
                int targetEmptyIndex = emptySlotIndices.get(random.nextInt(emptySlotIndices.size()));
                Collections.swap(slots, i, targetEmptyIndex);
                emptySlotIndices.remove(Integer.valueOf(targetEmptyIndex));
                emptySlotIndices.add(i);
            }
        }
    }

    private void processAxolotlBehavior(List<NbtCompound> slots) {
        List<NbtCompound> axolotls = slots.stream()
                .filter(e -> AXOLOTL_ID.equals(e.getString(ID_KEY, "")) && !e.getBoolean(DIED_KEY, false))
                .toList();
        if (axolotls.isEmpty()) return;

        List<NbtCompound> victims = slots.stream()
                .filter(e -> VICTIMS.contains(e.getString(ID_KEY, "")) && !e.getBoolean(DIED_KEY, false))
                .toList();

        for (NbtCompound axolotl : axolotls) {
            processAxolotlState(axolotl);
            if (victims.isEmpty()) return;
            if (!canAxolotlHunt(axolotl)) continue;
            victims.stream()
                    .findFirst()
                    .ifPresent(target -> {
                        attack(axolotl, target, false);
                        axolotl.putInt(ATTACK_COOLDOWN_KEY, ATTACK_COOLDOWN_TICKS);
                        if (target.getBoolean(DIED_KEY, false)) processAxolotlState(axolotl, 0);
                    });
        }
    }

    private void processAxolotlState(NbtCompound axolotl) {
        if (axolotl.contains(BRAIN_KEY)) {
            NbtCompound memories = axolotl.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY);
            if (memories.contains(HUNTING_COOLDOWN_MEMORY) && memories.getCompoundOrEmpty(HUNTING_COOLDOWN_MEMORY).getLong("ttl", 0) <= 0) memories.remove(HUNTING_COOLDOWN_MEMORY);
            if (memories.contains(PLAY_DEAD_TICKS_MEMORY) && memories.getCompoundOrEmpty(PLAY_DEAD_TICKS_MEMORY).getInt("value", 0) <= 0) memories.remove(PLAY_DEAD_TICKS_MEMORY);
        }
        int attackCooldown = axolotl.getInt(ATTACK_COOLDOWN_KEY, 0);
        if (attackCooldown > 0) axolotl.putInt(ATTACK_COOLDOWN_KEY, attackCooldown - 1);
    }

    private boolean canAxolotlHunt(NbtCompound axolotl) {
        if (axolotl.contains(BRAIN_KEY)) {
            NbtCompound memories = axolotl.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY);
            int attackCooldown = axolotl.getInt(ATTACK_COOLDOWN_KEY, 0);
            return !memories.contains(HUNTING_COOLDOWN_MEMORY) && !memories.contains(PLAY_DEAD_TICKS_MEMORY) && attackCooldown <= 0;
        }
        return true;
    }

    private void processPufferfishBehavior(List<NbtCompound> slots) {
        for (int i = 0; i < slots.size(); i++) {
            NbtCompound pufferfish = slots.get(i);
            if (!PUFFERFISH_ID.equals(pufferfish.getString(ID_KEY, "")) || pufferfish.getBoolean(DIED_KEY, false)) {
                continue;
            }

            int cooldown = pufferfish.getInt(PUFF_COOLDOWN_KEY, 0);
            if (cooldown > 0) {
                pufferfish.putInt(PUFF_COOLDOWN_KEY, cooldown - 1);
            }

            int attackCooldown = pufferfish.getInt(ATTACK_COOLDOWN_KEY, 0);
            if (attackCooldown > 0) pufferfish.putInt(ATTACK_COOLDOWN_KEY, attackCooldown - 1);

            NbtCompound leftNeighbor = findNeighbor(slots, i, -1);
            NbtCompound rightNeighbor = findNeighbor(slots, i, 1);

            boolean leftIsAxolotl = leftNeighbor != null && AXOLOTL_ID.equals(leftNeighbor.getString(ID_KEY, ""));
            boolean rightIsAxolotl = rightNeighbor != null && AXOLOTL_ID.equals(rightNeighbor.getString(ID_KEY, ""));
            boolean axolotlAdjacent = leftIsAxolotl || rightIsAxolotl;

            int puffState = pufferfish.getInt(PUFF_STATE_KEY, 0);

            if (axolotlAdjacent) {
                if (cooldown <= 0 && puffState < 2) {
                    puffState++;
                    pufferfish.putInt(PUFF_STATE_KEY, puffState);
                    pufferfish.putInt(PUFF_COOLDOWN_KEY, PUFF_UP_COOLDOWN_TICKS);
                }
                if (puffState > 0 && attackCooldown <= 0) {
                    if (leftIsAxolotl) {
                        attack(pufferfish, leftNeighbor, true);
                    }
                    if (rightIsAxolotl) {
                        attack(pufferfish, rightNeighbor, true);
                    }
                    pufferfish.putInt(ATTACK_COOLDOWN_KEY, ATTACK_COOLDOWN_TICKS);
                }
            } else {
                if (cooldown <= 0 && puffState > 0) {
                    pufferfish.putInt(PUFF_STATE_KEY, puffState - 1);
                    pufferfish.putInt(PUFF_COOLDOWN_KEY, PUFF_UP_COOLDOWN_TICKS);
                }
            }
        }
    }

    @Nullable
    private NbtCompound findNeighbor(List<NbtCompound> slots, int startIndex, int direction) {
        int currentIndex = startIndex + direction;
        while (currentIndex >= 0 && currentIndex < slots.size()) {
            NbtCompound neighbor = slots.get(currentIndex);
            if (!isEmpty(neighbor) && !neighbor.getBoolean(DIED_KEY, false)) {
                return neighbor;
            }
            currentIndex += direction;
        }
        return null;
    }

    /**
     * @param state 0 is killer, 1 is victim
     */
    public void processAxolotlState(NbtCompound axolotl, int state) {
        if (!axolotl.contains(BRAIN_KEY)) return;
        NbtCompound brain = axolotl.getCompoundOrEmpty(BRAIN_KEY);
        if (!brain.contains(MEMORIES_KEY)) return;
        NbtCompound memories = brain.getCompoundOrEmpty(MEMORIES_KEY);

        if (state == 0) {
            NbtCompound cooldown = new NbtCompound();
            cooldown.putBoolean("value", true);
            cooldown.putLong("ttl", (long) 2 * 60 * TICKS_PER_SECOND);
            memories.put(HUNTING_COOLDOWN_MEMORY, cooldown);
        } else {
            float health = axolotl.getFloat(HEALTH_KEY, 1.0F);
            if (health <= 7.0F && random.nextFloat() < 0.3333333333333333F && !memories.getCompoundOrEmpty("memories").contains(PLAY_DEAD_TICKS_MEMORY)) {
                NbtCompound playDead = new NbtCompound();
                playDead.putInt("value", 10 * TICKS_PER_SECOND);
                memories.put(PLAY_DEAD_TICKS_MEMORY, playDead);
                regenerationAxolotl(axolotl);
            }
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

        if (newHealth < 1.0F) {
            victim.putBoolean(DIED_KEY, true);
        }

        if (AXOLOTL_ID.equals(victim.getString(ID_KEY, "")) && !victim.getBoolean(DIED_KEY, false)) {
            processAxolotlState(victim, 1);
        }
    }

    public void attackWithPoison(NbtCompound victim, int duration) {
        if (duration <= 0) return;

        NbtList activeEffects = victim.getListOrEmpty(ACTIVE_EFFECTS_KEY).copy();
        if (!victim.contains(ACTIVE_EFFECTS_KEY)) {
            victim.put(ACTIVE_EFFECTS_KEY, activeEffects);
        }

        activeEffects.removeIf(element ->
                element instanceof NbtCompound compound && "minecraft:poison".equals(compound.getString(ID_KEY, ""))
        );

        NbtCompound poison = new NbtCompound();
        poison.putString(ID_KEY, "minecraft:poison");
        poison.putInt("duration", duration);
        poison.putByte("amplifier", (byte) 0);
        poison.putBoolean("show_icon", true);
        activeEffects.add(poison);
        victim.put(ACTIVE_EFFECTS_KEY, activeEffects);
    }

    public void regenerationAxolotl(NbtCompound axolotl) {
        NbtList activeEffects = axolotl.getListOrEmpty(ACTIVE_EFFECTS_KEY).copy();
        if (!axolotl.contains(ACTIVE_EFFECTS_KEY)) {
            axolotl.put(ACTIVE_EFFECTS_KEY, activeEffects);
        }

        activeEffects.removeIf(element ->
                element instanceof NbtCompound compound && "minecraft:regeneration".equals(compound.getString(ID_KEY, ""))
        );

        NbtCompound poison = new NbtCompound();
        poison.putString(ID_KEY, "minecraft:regeneration");
        poison.putInt("duration", 10 * TICKS_PER_SECOND);
        poison.putByte("amplifier", (byte) 0);
        poison.putBoolean("show_icon", true);
        activeEffects.add(poison);
        axolotl.put(ACTIVE_EFFECTS_KEY, activeEffects);
    }

    private ClearResult cleanupDeadEntities(List<NbtCompound> slots) {
        List<NbtCompound> deadEntities = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            NbtCompound entity = slots.get(i);
            if (!isEmpty(entity) && entity.getBoolean(DIED_KEY, false)) {
                deadEntities.add(entity);
                slots.set(i, createEmptyEntity());
            }
        }
        return new ClearResult(slots, deadEntities);
    }

    private void processEntityTimers(List<NbtCompound> slots) {
        for (NbtCompound entity : slots) {
            if (isEmpty(entity) || entity.getBoolean(DIED_KEY, false)) {
                continue;
            }

            if (AXOLOTL_ID.equals(entity.getString(ID_KEY, ""))) {
                processAxolotlMemories(entity);
            }

            processActiveEffects(entity);
        }
    }

    private void processAxolotlMemories(NbtCompound axolotl) {
        if (!axolotl.contains(BRAIN_KEY)) return;
        NbtCompound brain = axolotl.getCompoundOrEmpty(BRAIN_KEY);
        if (!brain.contains(MEMORIES_KEY)) return;
        NbtCompound memories = brain.getCompoundOrEmpty(MEMORIES_KEY);
        List<String> memoriesToRemove = new ArrayList<>();
        for (String key : memories.getKeys()) {
            if (memories.get(key) instanceof NbtCompound memoryData && memoryData.contains("ttl")) {
                long ttl = memoryData.getLong("ttl", 0L);
                ttl--;
                if (ttl <= 0) {
                    memoriesToRemove.add(key);
                } else {
                    memoryData.putLong("ttl", ttl);
                }
            }
        }
        memoriesToRemove.forEach(memories::remove);
    }

    private void processActiveEffects(NbtCompound entity) {
        if (!entity.contains(ACTIVE_EFFECTS_KEY)) {
            return;
        }

        NbtList activeEffects = entity.getListOrEmpty(ACTIVE_EFFECTS_KEY);
        Iterator<NbtElement> iterator = activeEffects.iterator();

        while (iterator.hasNext()) {
            NbtElement element = iterator.next();
            if (!(element instanceof NbtCompound effectNbt)) continue;
            int duration = effectNbt.getInt("duration", 0);
            if (duration <= 0) {
                iterator.remove();
                continue;
            }
            String effectId = effectNbt.getString(ID_KEY, "");
            switch (effectId) {
                case "minecraft:poison":
                    if (duration % 25 == 0 && entity.getFloat("Health", 1.0F) >= 2.0F) attack(null, entity, false);
                    break;
                case "minecraft:regeneration":
                    if (duration % 50 == 0) {
                        float health = entity.getFloat(HEALTH_KEY, 1.0F);
                        if (health + 1.0F <= 14.0F) entity.putFloat(HEALTH_KEY, health + 1.0F);
                        if (entity.getString(ID_KEY, "").equals(AXOLOTL_ID) && entity.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY).contains(PLAY_DEAD_TICKS_MEMORY)) {
                            NbtCompound memories = entity.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY).getCompoundOrEmpty(PLAY_DEAD_TICKS_MEMORY);
                            int playDeadTicks = memories.getInt("value", 0);
                            if (playDeadTicks - 50 > 0) memories.putInt("value", playDeadTicks - 50);
                            else memories.remove(PLAY_DEAD_TICKS_MEMORY);
                        }
                    }
                    break;
            }
            duration--;
            effectNbt.putInt("duration", duration);
            if (duration <= 0) {
                if (effectId.equals("minecraft:regeneration") && entity.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY).contains(PLAY_DEAD_TICKS_MEMORY)) entity.getCompoundOrEmpty(BRAIN_KEY).getCompoundOrEmpty(MEMORIES_KEY).remove(PLAY_DEAD_TICKS_MEMORY);
                iterator.remove();
            }
        }
        if (activeEffects.isEmpty()) entity.remove(ACTIVE_EFFECTS_KEY);
    }
}