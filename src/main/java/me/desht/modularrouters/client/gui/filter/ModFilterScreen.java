package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.filter.Buttons.DeleteButton;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.network.messages.FilterUpdateMessage;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ModFilterScreen extends AbstractFilterContainerScreen {
    private static final Identifier TEXTURE_LOCATION = MiscUtil.RL("textures/gui/modfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 252;

    private final List<String> mods = Lists.newArrayList();
    private final List<DeleteButton> deleteButtons = Lists.newArrayList();

    private ItemStack prevInSlot = ItemStack.EMPTY;
    private String modId = "";
    private String modName = "";

    public ModFilterScreen(AbstractSmartFilterMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName, GUI_WIDTH, GUI_HEIGHT);

        mods.addAll(ModFilter.getModList(filterStack));
    }

    @Override
    public void init() {
        super.init();

        if (menu.getLocator().filterSlot() >= 0) {
            addRenderableWidget(new BackButton(leftPos - 12, topPos, p -> closeGUI()));
        }
        addRenderableWidget(new Buttons.AddButton(leftPos + 154, topPos + 19, p -> {
            if (!modId.isEmpty()) {
                Set<String> updatedMods = new LinkedHashSet<>(mods);
                updatedMods.add(modId);
                sendModsToServer(updatedMods);
                getMenu().slots.getFirst().set(ItemStack.EMPTY);
            }
        }));
        deleteButtons.clear();
        for (int i = 0; i < ModFilter.MAX_SIZE; i++) {
            DeleteButton b = new DeleteButton(leftPos + 8, topPos + 44 + i * 19, i, button -> {
                sendModsToServer(button.removeFromList(new ArrayList<>(mods)));
            });
            addRenderableWidget(b);
            deleteButtons.add(b);
        }
        updateDeleteButtonVisibility();
    }

    private void sendModsToServer(Collection<String> newMods) {
        ItemStack newStack = Util.make(filterStack.copy(), s -> ModFilter.setModList(s, List.copyOf(newMods)));
        ClientPacketDistributor.sendToServer(new FilterUpdateMessage(menu.getLocator(), newStack));
    }

    private void updateDeleteButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < mods.size();
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Component txt = filterStack.getHoverName().copy().append(" ").append(
                menu.getRouter() != null ? xlate("modularrouters.guiText.label.installed") : Component.empty()
        );
        graphics.text(font, txt, this.imageWidth / 2 - font.width(txt) / 2, 8, 0x404040, false);

        if (!modName.isEmpty()) {
            graphics.text(font, modName, 29, 23, 0x404040, false);
        }

        for (int i = 0; i < mods.size(); i++) {
            String mod = ModNameCache.getModName(mods.get(i));
            graphics.text(font, mod, 28, 47 + i * 19, 0x404080, false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack inSlot = getMenu().getItems().getFirst();
        if (inSlot.isEmpty() && !prevInSlot.isEmpty()) {
            modId = modName = "";
        } else if (!inSlot.isEmpty() && (prevInSlot.isEmpty() || !MiscUtil.sameItemStackIgnoreDurability(inSlot, prevInSlot))) {
            modId = BuiltInRegistries.ITEM.getKey(inSlot.getItem()).getNamespace();
            modName = ModNameCache.getModName(modId);
        }
        prevInSlot = inSlot;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void resync(ItemStack filterStack) {
        mods.clear();
        mods.addAll(ModFilter.getModList(filterStack));
        updateDeleteButtonVisibility();
    }
}
