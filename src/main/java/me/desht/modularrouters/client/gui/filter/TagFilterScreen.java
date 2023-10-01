package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.filter.Buttons.DeleteButton;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.smartfilter.TagFilter;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.FilterSettingsMessage.Operation;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TagFilterScreen extends AbstractFilterContainerScreen {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ModularRouters.MODID, "textures/gui/tagfilter.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 252;

    private final List<TagKey<Item>> addedTags = Lists.newArrayList();
    private final List<DeleteButton> deleteButtons = Lists.newArrayList();

    private ItemStack prevInSlot = ItemStack.EMPTY;
    private final List<TagKey<Item>> candidateTags = new ArrayList<>();
    private TagKey<Item> selectedTag = null;
    private boolean tagSelectorShowing = false;

    private ExtendedButton selectButton;
    private final List<ExtendedButton> optionButtons = new ArrayList<>();

    public TagFilterScreen(AbstractSmartFilterMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        addedTags.addAll(TagFilter.getTagList(filterStack));
    }

    @Override
    public void init() {
        super.init();

        if (menu.getLocator().filterSlot >= 0) {
            addRenderableWidget(new BackButton(leftPos - 12, topPos, p -> closeGUI()));
        }
        addRenderableWidget(new Buttons.AddButton(leftPos + 234, topPos + 19, p -> {
            if (selectedTag != null) {
                CompoundTag ext = new CompoundTag();
                ext.putString("Tag", selectedTag.location().toString());
                PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.ADD_STRING, menu.getLocator(), ext));
            }
        }));
        deleteButtons.clear();
        for (int i = 0; i < ModFilter.MAX_SIZE; i++) {
            DeleteButton b = new DeleteButton(leftPos + 8, topPos + 44 + i * 19, i, button -> {
                CompoundTag ext = new CompoundTag();
                ext.putInt("Pos", ((DeleteButton) button).getId());
                PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.REMOVE_AT, menu.getLocator(), ext));
            });
            addRenderableWidget(b);
            deleteButtons.add(b);
        }

        selectButton = new ExtendedButton(leftPos + 218, topPos + 20, 14, 14, Component.literal("▼"), p -> {
             tagSelectorShowing = !tagSelectorShowing;
             updateButtonVisibility();
        });
        addRenderableWidget(selectButton);
        selectButton.visible = false;

        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < addedTags.size() && !tagSelectorShowing;
        }
        optionButtons.forEach(b -> b.visible = tagSelectorShowing);
        selectButton.setMessage(Component.literal(tagSelectorShowing ? "▲" : "▼"));
        selectButton.visible = candidateTags.size() > 1;
    }

    private void rebuildOptionButtons() {
        optionButtons.forEach(this::removeWidget);
        optionButtons.clear();
        if (candidateTags.size() > 1) {
            for (int i = 0; i < candidateTags.size(); i++) {
                SelectorButton sb = new SelectorButton(leftPos + 8, topPos + 44 + i * (font.lineHeight + 5), candidateTags.get(i));
                optionButtons.add(sb);
                addRenderableWidget(sb);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String title = filterStack.getHoverName().getString() + (menu.getRouter() != null ? I18n.get("modularrouters.guiText.label.installed") : "");
        graphics.drawString(font, title, this.imageWidth / 2 - font.width(title) / 2, 8, 0x404040, false);

        if (selectedTag != null) {
            int maxW = selectButton.visible ? 190 : 205;
            graphics.drawString(font, ClientUtil.ellipsize(font, selectedTag.location().toString(), maxW), 29, 23, 0x404040, false);
        } else if (candidateTags.size() > 1) {
            graphics.drawString(font, ClientUtil.xlate("modularrouters.guiText.label.selectTag").withStyle(ChatFormatting.ITALIC), 29, 23, 0x808080, false);
        } else if (candidateTags.isEmpty()) {
            graphics.drawString(font, ClientUtil.xlate("modularrouters.guiText.label.noTags").withStyle(ChatFormatting.ITALIC), 29, 23, 0x808080, false);
        }

        if (!tagSelectorShowing) {
            for (int i = 0; i < addedTags.size(); i++) {
                String tag = addedTags.get(i).location().toString();
                graphics.drawString(font, ClientUtil.ellipsize(font, tag, 220), 28, 47 + i * 19, 0x404080, false);
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack inSlot = getMenu().getItems().get(0);
        if (inSlot.isEmpty() && !prevInSlot.isEmpty()) {
            candidateTags.clear();
            selectedTag = null;
            tagSelectorShowing = false;
            rebuildOptionButtons();
            updateButtonVisibility();
        } else if (!inSlot.isEmpty() && (prevInSlot.isEmpty() || !MiscUtil.sameItemStackIgnoreDurability(inSlot, prevInSlot))) {
            List<TagKey<Item>> l = MiscUtil.itemTags(inSlot.getItem()).stream().sorted(Comparator.comparing(TagKey::location)).toList();
            candidateTags.clear();
            candidateTags.addAll(l);
            selectedTag = candidateTags.size() == 1 ? l.get(0) : null;
            rebuildOptionButtons();
            updateButtonVisibility();
        }
        prevInSlot = inSlot;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(TEXTURE_LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void resync(ItemStack filterStack) {
        addedTags.clear();
        addedTags.addAll(TagFilter.getTagList(filterStack));
        tagSelectorShowing = false;
        updateButtonVisibility();
    }

    private class SelectorButton extends ExtendedButton {
        public SelectorButton(int xPos, int yPos, TagKey<Item> tag) {
            super(xPos, yPos, 239, font.lineHeight + 5, Component.literal(tag.location().toString()), p -> {
                selectedTag = tag;
                tagSelectorShowing = false;
                TagFilterScreen.this.updateButtonVisibility();
            });
        }
    }
}
