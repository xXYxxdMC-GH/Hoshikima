package com.xxyxxdmc.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Set;

public class SpecialEnderPearlEntity extends EnderPearlEntity {
    public SpecialEnderPearlEntity(World world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        for(int i = 0; i < 32; ++i) {
            this.getWorld().addParticleClient(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * (double)2.0F, this.getZ(), this.random.nextGaussian(), (double)0.0F, this.random.nextGaussian());
        }

        World var3 = this.getWorld();
        if (var3 instanceof ServerWorld serverWorld) {
            if (!this.isRemoved()) {
                Entity entity = this.getOwner();
                if (entity != null && canTeleportEntityTo(entity, serverWorld)) {
                    Vec3d vec3d = this.getLastRenderPos();
                    if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                        if (serverPlayerEntity.networkHandler.isConnectionOpen()) {
                            if (this.random.nextFloat() < 0.05F && serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
                                EndermiteEntity endermiteEntity = (EndermiteEntity) EntityType.ENDERMITE.create(serverWorld, SpawnReason.TRIGGERED);
                                if (endermiteEntity != null) {
                                    endermiteEntity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
                                    serverWorld.spawnEntity(endermiteEntity);
                                }
                            }

                            if (this.hasPortalCooldown()) {
                                entity.resetPortalCooldown();
                            }

                            ServerPlayerEntity serverPlayerEntity2 = serverPlayerEntity.teleportTo(new TeleportTarget(serverWorld, vec3d, Vec3d.ZERO, 0.0F, 0.0F, PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA), TeleportTarget.NO_OP));
                            if (serverPlayerEntity2 != null) {
                                serverPlayerEntity2.onLanding();
                                serverPlayerEntity2.clearCurrentExplosion();
                                serverPlayerEntity2.damage(serverPlayerEntity.getServerWorld(), this.getDamageSources().enderPearl(), 0.0F);
                            }

                            this.playTeleportSound(serverWorld, vec3d);
                        }
                    } else {
                        Entity entity2 = entity.teleportTo(new TeleportTarget(serverWorld, vec3d, entity.getVelocity(), entity.getYaw(), entity.getPitch(), TeleportTarget.NO_OP));
                        if (entity2 != null) {
                            entity2.onLanding();
                        }

                        this.playTeleportSound(serverWorld, vec3d);
                    }

                    this.discard();
                    return;
                }

                this.discard();
            }
        }

    }

    private void playTeleportSound(World world, Vec3d pos) {
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }

    private static boolean canTeleportEntityTo(Entity entity, World world) {
        if (entity.getWorld().getRegistryKey() == world.getRegistryKey()) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return entity.isAlive();
            } else {
                return livingEntity.isAlive() && !livingEntity.isSleeping();
            }
        } else {
            return entity.canUsePortals(true);
        }
    }
}
