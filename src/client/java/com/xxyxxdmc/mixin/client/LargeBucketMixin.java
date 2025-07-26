package com.xxyxxdmc.mixin.client;

import com.xxyxxdmc.init.item.LargeBucket;
import com.xxyxxdmc.init.item.client.LargeBucketTooltip;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(LargeBucket.class)
public abstract class LargeBucketMixin extends Item {

    public LargeBucketMixin(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        LargeBucketTooltip.append(stack, textConsumer);
    }
}
