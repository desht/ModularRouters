package me.desht.modularrouters.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiBulkItemFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/bulkfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 156;

    private static final int CLEAR_BUTTON_ID = 1;
    private static final int MERGE_BUTTON_ID = 2;
    private static final int LOAD_BUTTON_ID = 3;
    private static final int BACK_BUTTON_ID = 100;

    private final SetofItemStack stacks;

    private int xPos, yPos;
    private ModuleTarget target;

    public GuiBulkItemFilter(ItemStack filterStack, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        super(filterStack, routerPos, moduleSlotIndex, filterSlotIndex, hand);

        this.stacks = BulkItemFilter.getFilterItems(filterStack);
    }

    @Override
    public void initGui() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        buttonList.clear();

        buttonList.add(new Buttons.ClearButton(CLEAR_BUTTON_ID, xPos + 8, yPos + 135));

        if (filterSlotIndex >= 0) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, xPos - 12, yPos));
        }
        if (moduleSlotIndex >= 0) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(mc.theWorld, routerPos);
            if (router != null) {
                ItemStack moduleStack = router.getModules().getStackInSlot(moduleSlotIndex);
                Module m = ItemModule.getModule(moduleStack);
                CompiledModule cm = m.compile(router, moduleStack);
                target = cm.getActualTarget(router);
                // This should work even if the target is in another dimension, since the target name
                // is stored in the module item NBT, which was set up server-side.
                // Using getActualTarget() here *should* ensure that we always see the right target...
                if (target != null && target.invName != null && !target.invName.isEmpty()) {
                    buttonList.add(new Buttons.MergeButton(MERGE_BUTTON_ID, xPos + 28, yPos + 135,
                            MiscUtil.locToString(target.dimId, target.pos), target.invName));
                    buttonList.add(new Buttons.LoadButton(LOAD_BUTTON_ID, xPos + 48, yPos + 135,
                            MiscUtil.locToString(target.dimId, target.pos), target.invName));
                }
            }
        }

        int c = 0;
        for (ItemStack stack : stacks.sortedList()) {
            int x = 8 + (c % 9) * 18;
            int y = 22 + (c / 9) * 18;
            buttonList.add(new FilterButton(200 + c, xPos + x, yPos + y, stack));
            c++;
        }
    }

    private static class FilterButton extends ItemStackButton {
        FilterButton(int buttonId, int x, int y, ItemStack stack) {
            super(buttonId, x, y, 18, 18, stack, true);
            tooltip1.add(stack.getDisplayName());
            tooltip1.add(I18n.format("guiText.tooltip.removeFilterHint"));
            tooltip1.add(TextFormatting.BLUE + TextFormatting.ITALIC.toString() + ModNameCache.getModName(stack));
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            if (isShiftKeyDown()) super.playPressSound(soundHandlerIn);
        }
    }

    private String getItemNameAtPos(BlockPos pos) {
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block b = state.getBlock();
        Item item = Item.getItemFromBlock(b);
        if (item != null) {
            return new ItemStack(item, 1, b.getMetaFromState(state)).getDisplayName();
        }
        return "";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRendererObj.drawString(title, xPos + GUI_WIDTH / 2 - this.fontRendererObj.getStringWidth(title) / 2, yPos + 6, 0x404040);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.buttonList.stream()
                .filter(button -> button.isMouseOver() && button instanceof ITooltipButton)
                .forEach(button -> drawHoveringText(((ITooltipButton) button).getTooltip(), mouseX, mouseY, fontRendererObj));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case CLEAR_BUTTON_ID:
                if (!stacks.isEmpty()) {
                    stacks.clear();
                    initGui();
                    if (routerPos != null) {
                        ModularRouters.network.sendToServer(new FilterSettingsMessage(
                                FilterSettingsMessage.Operation.CLEAR_ALL, routerPos, moduleSlotIndex, filterSlotIndex, null));
                    } else {
                        ModularRouters.network.sendToServer(new FilterSettingsMessage(
                                FilterSettingsMessage.Operation.CLEAR_ALL, hand, filterSlotIndex, null));
                    }
                }
                break;
            case MERGE_BUTTON_ID:
                if (target != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.MERGE, routerPos, moduleSlotIndex, filterSlotIndex, target.toNBT()));
                }
                break;
            case LOAD_BUTTON_ID:
                if (target != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.LOAD, routerPos, moduleSlotIndex, filterSlotIndex, target.toNBT()));
                }
                break;
            case BACK_BUTTON_ID:
                closeGUI();
                break;
            default:
                if (button instanceof FilterButton && isShiftKeyDown()) {
                    NBTTagCompound ext = ((FilterButton) button).getRenderStack().serializeNBT();
                    if (routerPos != null) {
                        ModularRouters.network.sendToServer(new FilterSettingsMessage(
                                FilterSettingsMessage.Operation.REMOVE_ITEM, routerPos, moduleSlotIndex, filterSlotIndex, ext));
                    } else {
                        ModularRouters.network.sendToServer(new FilterSettingsMessage(
                                FilterSettingsMessage.Operation.REMOVE_ITEM, hand, filterSlotIndex, ext));
                    }
                } else {
                    super.actionPerformed(button);
                }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void resync(ItemStack newStack) {
        stacks.clear();
        stacks.addAll(BulkItemFilter.getFilterItems(newStack));
        initGui();
    }
}
