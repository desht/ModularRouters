package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.filter.Buttons.DeleteButton;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.FilterSettingsMessage.Operation;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiModFilter extends GuiFilterContainer {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ModularRouters.MODID, "textures/gui/modfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 252;

    private final List<String> mods = Lists.newArrayList();
    private final List<DeleteButton> deleteButtons = Lists.newArrayList();

    private ItemStack prevInSlot = ItemStack.EMPTY;
    private String modId = "";
    private String modName = "";

    public GuiModFilter(ContainerSmartFilter container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        mods.addAll(ModFilter.getModList(filterStack));
        mods.forEach(s -> ModularRouters.LOGGER.info("mod: " + s));
    }

    @Override
    public void init() {
        super.init();

        if (menu.getLocator().filterSlot >= 0) {
            addButton(new BackButton(leftPos - 12, topPos, p -> closeGUI()));
        }
        addButton(new Buttons.AddButton(leftPos + 154, topPos + 19, p -> {
            if (!modId.isEmpty()) {
                CompoundNBT ext = new CompoundNBT();
                ext.putString("ModId", modId);
                PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.ADD_STRING, menu.getLocator(), ext));
                getMenu().slots.get(0).set(ItemStack.EMPTY);
            }
        }));
        deleteButtons.clear();
        for (int i = 0; i < ModFilter.MAX_SIZE; i++) {
            DeleteButton b = new DeleteButton(leftPos + 8, topPos + 44 + i * 19, i, button -> {
                CompoundNBT ext = new CompoundNBT();
                ext.putInt("Pos", ((DeleteButton) button).getId());
                PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.REMOVE_AT, menu.getLocator(), ext));
            });
            addButton(b);
            deleteButtons.add(b);
        }
        updateDeleteButtonVisibility();
    }

    private void updateDeleteButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < mods.size();
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        String title = filterStack.getHoverName().getString() + (menu.getRouter() != null ? I18n.get("modularrouters.guiText.label.installed") : "");
        font.draw(matrixStack, title, this.imageWidth / 2f - font.width(title) / 2f, 8, 0x404040);

        if (!modName.isEmpty()) {
            font.draw(matrixStack, modName, 29, 23, 0x404040);
        }

        for (int i = 0; i < mods.size(); i++) {
            String mod = ModNameCache.getModName(mods.get(i));
            font.draw(matrixStack, mod, 28, 47 + i * 19, 0x404080);
        }
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack inSlot = getMenu().getItems().get(0);
        if (inSlot.isEmpty() && !prevInSlot.isEmpty()) {
            modId = modName = "";
        } else if (!inSlot.isEmpty() && (prevInSlot.isEmpty() || !inSlot.sameItemStackIgnoreDurability(prevInSlot))) {
            modId = inSlot.getItem().getRegistryName().getNamespace();
            modName = ModNameCache.getModName(modId);
        }
        prevInSlot = inSlot;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        minecraft.getTextureManager().bind(TEXTURE_LOCATION);
        blit(matrixStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void resync(ItemStack filterStack) {
        mods.clear();
        mods.addAll(ModFilter.getModList(filterStack));
        updateDeleteButtonVisibility();
    }
}
