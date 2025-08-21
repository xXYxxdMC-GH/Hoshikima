package com.xxyxxdmc.init.plugin;


import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.init.api.IChainMineState;
import com.xxyxxdmc.init.api.ISolidColorElementFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.Tooltip.Line;

import java.util.ArrayList;
import java.util.List;

public class ChainMineComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final Identifier COMPONENT_ID = Identifier.of(Hoshikima.MOD_ID, "chain_mine_info");

    @Override
    public Identifier getUid() {
        return COMPONENT_ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound serverData = accessor.getServerData();
        IElementHelper helper = IElementHelper.get();
        ISolidColorElementFactory factory = Hoshikima.solidColorElementFactory;

        if (serverData.contains("hoshikima.total_blocks") && factory != null) {
            int totalBlocks = serverData.getInt("hoshikima.total_blocks").get();
            int skippedAirs = serverData.getInt("hoshikima.skipped_airs").get();

            Tooltip castedTooltip = (Tooltip) tooltip;
            List<Line> originalLines = castedTooltip.lines;

            int maxWidth = 0;
            for (Line line : originalLines) {
                int line_width = (int) line.size().x;
                if (line_width > maxWidth) {
                    maxWidth = line_width;
                }
            }

            List<Text> texts = new ArrayList<>();

            for (int i = 0; i < originalLines.size(); i++) {
                Line line = originalLines.get(i);

                List<IElement> elements = line.sortedElements();
                if (!elements.isEmpty() && elements.get(elements.size() - 1) instanceof ITextElement) {

                    int lineWidth = (int) line.size().x;
                    int spacerWidth = maxWidth - lineWidth;

                    IElement spacer = helper.spacer(spacerWidth + 3, 0);
                    IElement verticalLine = factory.create(1, 5, 0xFFFFFFFF);
                    ITextElement chainText = helper.text(Text.translatable("hud.hoshikima.chain.mine.skip.blocks.total")
                            .formatted(Formatting.GOLD)
                            .append(Text.literal(": " + skippedAirs).formatted(Formatting.WHITE)));

                    tooltip.append(i, spacer);
                    tooltip.append(i, verticalLine);
                    tooltip.append(i, helper.spacer(3, 0));
                    tooltip.append(i, chainText);
                }
            }
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        PlayerEntity player = accessor.getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer instanceof IChainMineState chainMineState && chainMineState.isChainMiningActive()) {
                int totalBlocks = chainMineState.getPendingBreakList().size();
                int skippedAirs = chainMineState.getPendingBreakList().size();

                data.putInt("hoshikima.total_blocks", totalBlocks);
                data.putInt("hoshikima.skipped_airs", skippedAirs);
            }
        }
    }
}
