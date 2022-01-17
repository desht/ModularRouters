package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule.MatchType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class BreakerModuleScreen extends AbstractModuleScreen {
    private static final ItemStack BLOCK_STACK = new ItemStack(Blocks.IRON_BLOCK);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.IRON_INGOT);
    private static final ItemStack[] STACKS = new ItemStack[] {
            ITEM_STACK, BLOCK_STACK
    };

    private MatchBlockButton matchBlockButton;

    public BreakerModuleScreen(ContainerModule container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledBreakerModule cbm = new CompiledBreakerModule(null, moduleItemStack);

        addRenderableWidget(matchBlockButton = new MatchBlockButton(leftPos + 147, topPos + 20, 20, 20, true, STACKS, cbm.getMatchType(), this));

        getMouseOverHelp().addHelpRegion(leftPos + 146, topPos + 19, leftPos + 165, topPos + 38, "modularrouters.guiText.popup.breaker.matchType");
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag compound = super.buildMessageData();
        compound.putInt(CompiledBreakerModule.NBT_MATCH_TYPE, matchBlockButton.getState().ordinal());
        return compound;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);

        this.blit(poseStack, leftPos + 147, topPos + 20, BUTTON_XY.x(), BUTTON_XY.y(), 18, 18);  // match type "button" background
    }

    private static class MatchBlockButton extends ItemStackCyclerButton<MatchType> {
        private final List<List<Component>> tips = Lists.newArrayList();

        public MatchBlockButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, MatchType initialVal, ISendToServer dataSyncer) {
            super(x, y, width, height, flat, stacks, initialVal, dataSyncer);

            for (MatchType type : MatchType.values()) {
                tips.add(Collections.singletonList(xlate(type.getTranslationKey())));
            }
        }

        @Override
        public List<Component> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }
}
