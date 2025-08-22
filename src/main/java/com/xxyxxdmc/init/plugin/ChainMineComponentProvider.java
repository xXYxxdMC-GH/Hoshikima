package com.xxyxxdmc.init.plugin;


import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.config.HoshikimaConfig;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChainMineComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final Identifier COMPONENT_ID = Identifier.of(Hoshikima.MOD_ID, "chain_mine_info");

    private static final HoshikimaConfig configGlobal = HoshikimaConfig.get();

    @Override
    public Identifier getUid() {
        return COMPONENT_ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound serverData = accessor.getServerData();
        IElementHelper helper = IElementHelper.get();
        ISolidColorElementFactory factory = Hoshikima.solidColorElementFactory;

        //⛏🔱🗡🏹❤🔥
        if (serverData.contains("hoshikima.total_blocks") && factory != null) {
            int totalBlocks = serverData.getInt("hoshikima.total_blocks").get();
            int skippedAirs = serverData.getInt("hoshikima.skipped_airs").get();
            boolean ableBreak = serverData.getBoolean("hoshikima.able_break").get();

            Tooltip castedTooltip = (Tooltip) tooltip;
            List<Line> originalLines = castedTooltip.lines;

            List<Text> texts = new ArrayList<>();
            texts.add(Text.translatable("hud.hoshikima.chain.mine.state").append(": ").append(!ableBreak ? Text.translatable("hud.hoshikima.chain.mine.inactive").formatted(Formatting.DARK_RED) : Text.translatable("hud.hoshikima.chain.mine.active").formatted(Formatting.GREEN)));
            texts.add(Text.translatable("config.hoshikima.chain.mine.chain.mode").append(": ").append(Text.translatable(getCurrentChainMode(configGlobal.chainMode))));
            if (ableBreak) {
                texts.add(Text.translatable("hud.hoshikima.chain.mine.total.chain.block").append(": ").append(String.valueOf(totalBlocks)));
                if (skippedAirs != 0) texts.add(Text.translatable("hud.hoshikima.chain.mine.skip.blocks.total").append(": ").append(String.valueOf(skippedAirs)));
            }

            if (!configGlobal.jadeLinkageOverwrite) {
                int maxWidth = 0;
                for (Line line : originalLines) {
                    int line_width = (int) line.size().x;
                    if (line_width > maxWidth) {
                        maxWidth = line_width;
                    }
                }

                for (int i = 0; i < texts.size(); i++) {
                    if (i < originalLines.size()) {
                        Line line = originalLines.get(i);
                        List<IElement> elements = line.sortedElements();

                        if (!elements.isEmpty() && elements.get(elements.size() - 1) instanceof ITextElement) {
                            int lineWidth = (int) line.size().x;
                            int spacerWidth = maxWidth - lineWidth;

                            IElement spacer = helper.spacer(spacerWidth + 3, 0);
                            IElement verticalLine = factory.create(1, 9, Color.GRAY.getRGB());
                            ITextElement chainText = helper.text(texts.get(i));

                            tooltip.append(i, spacer);
                            tooltip.append(i, verticalLine);
                            tooltip.append(i, helper.spacer(3, 0));
                            tooltip.append(i, chainText);
                        }
                    } else {
                        IElement spacer = helper.spacer(maxWidth + 3, 0);
                        IElement verticalLine = factory.create(1, 9, Color.GRAY.getRGB());
                        ITextElement chainText = helper.text(texts.get(i));
                        tooltip.append(i, spacer);
                        tooltip.append(i, verticalLine);
                        tooltip.append(i, helper.spacer(3, 0));
                        tooltip.append(i, chainText);
                    }
                }
            } else {
                tooltip.clear();
                for (int i = 0; i < texts.size(); i++) {
                    tooltip.append(i, helper.text(texts.get(i)));
                }
            }
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        if (configGlobal.hudDisplayWay != 1) return;
        PlayerEntity player = accessor.getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer instanceof IChainMineState chainMineState && chainMineState.isChainMiningActive()) {
                int totalBlocks = chainMineState.getPendingBreakList().size();
                int skippedAirs = chainMineState.getTotalSkipAirs();
                boolean ableBreak = chainMineState.enableBreak();

                data.putInt("hoshikima.total_blocks", totalBlocks);
                data.putInt("hoshikima.skipped_airs", skippedAirs);
                data.putBoolean("hoshikima.able_break", ableBreak);
            }
        }
    }

    private String getCurrentChainMode(int mode) {
        return switch (mode) {
            case 1 -> "config.hoshikima.chain.mine.mode.two";
            case 2 -> "config.hoshikima.chain.mine.mode.three";
            case 3 -> "config.hoshikima.chain.mine.mode.four";
            default -> "config.hoshikima.chain.mine.mode.one";
        };
    }
}
