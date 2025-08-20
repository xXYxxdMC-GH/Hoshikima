package com.xxyxxdmc.init.plugin;


import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.init.callback.IChainMineState;
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

public class ChainMineComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final Identifier COMPONENT_ID = Identifier.of(Hoshikima.MOD_ID, "chain_mine_info");

    public static final Identifier VERTICAL_LINE_TEXTURE = Identifier.of(Hoshikima.MOD_ID, "textures/gui/line.png");

    @Override
    public Identifier getUid() {
        return COMPONENT_ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound serverData = accessor.getServerData();

        if (serverData.contains("hoshikima.total_blocks")) {
            int totalBlocks = serverData.getInt("hoshikima.total_blocks").get();
            int skippedAirs = serverData.getInt("hoshikima.skipped_airs").get();

            ITextElement chainMineInfoText = createChainMineInfoElement(114, 514);

            IElement verticalLine = IElementHelper.get().sprite(VERTICAL_LINE_TEXTURE, 1, 40);

            tooltip.append(verticalLine);
            tooltip.append(chainMineInfoText);
        }
    }

    private ITextElement createChainMineInfoElement(int totalBlocks, int skippedAirs) {
        IElementHelper helper = IElementHelper.get();
        Text totalBlocksText = Text.translatable("hud.hoshikima.chain.mine.total.chain.block")
                .formatted(Formatting.GOLD)
                .append(Text.literal(": " + totalBlocks).formatted(Formatting.WHITE));
        Text skippedAirsText = Text.translatable("hud.hoshikima.chain.mine.skip.blocks.total")
                .formatted(Formatting.GOLD)
                .append(Text.literal(": " + skippedAirs).formatted(Formatting.WHITE));
        return helper.text(
                totalBlocksText.copy().append("\n").append(skippedAirsText)
        );
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
