package me.desht.modularrouters.integration.top;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IItemStyle;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.rendering.RenderHelper;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public class ElementModule implements IElement {
    private static final String ARROWS = " ▼▲◀▶▣▤";

    private final ItemModule.ModuleType type;
    private final Module.RelativeDirection dir;

    public ElementModule(ItemStack stack) {
        this.type = ItemModule.ModuleType.values()[stack.getItemDamage()];
        this.dir = Module.getDirectionFromNBT(stack);
    }

    public ElementModule(ByteBuf buf) {
        this.type = ItemModule.ModuleType.values()[buf.readByte()];
        this.dir = Module.RelativeDirection.values()[buf.readByte()];
    }

    @Override
    public void render(int x, int y) {
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        ItemStack stack = ItemModule.makeItemStack(type);
        IItemStyle style = new ItemStyle().width(getWidth()).height(getHeight());
        String dirStr = String.valueOf(ARROWS.charAt(dir.ordinal()));
        RenderHelper.renderItemStack(Minecraft.getMinecraft(), itemRender, stack, x + (style.getWidth() - 18) / 2, y + (style.getHeight() - 18) / 2, dirStr);
    }

    @Override
    public int getWidth() {
        return 20;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeByte(dir.ordinal());
    }

    @Override
    public int getID() {
        return TOPCompatibility.ELEMENT_MODULE_ITEM;
    }
}
