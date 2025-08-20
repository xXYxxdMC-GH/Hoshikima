package com.xxyxxdmc.init.plugin;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import snownee.jade.api.*;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ChainMineComponentProvider(), Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ChainMineComponentProvider(), Block.class);
    }
}
