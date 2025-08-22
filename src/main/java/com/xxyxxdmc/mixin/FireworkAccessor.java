package com.xxyxxdmc.mixin;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface FireworkAccessor {
    @Accessor("lifeTime")
    void setLifeTime(int lifeTime);
}
