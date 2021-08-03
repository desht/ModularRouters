package me.desht.modularrouters.integration.top;

import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;

public class ElementModule implements IElement {
    private final ItemStack stack;
    private final ModuleItem.RelativeDirection dir;

    public ElementModule(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ModuleItem, "provided item stack is not an ItemModule!");
        this.stack = stack;
        this.dir = ModuleHelper.getRelativeDirection(stack);
    }

    public ElementModule(FriendlyByteBuf buf) {
        FriendlyByteBuf pb = new FriendlyByteBuf(buf);
        this.stack = pb.readItem();
        this.dir = pb.readEnum(ModuleItem.RelativeDirection.class);
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y) {
        GuiUtil.renderItemStack(matrixStack, Minecraft.getInstance(), stack, x + (getWidth() - 18) / 2, y + (getHeight() - 18) / 2, dir.getSymbol());
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
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeItem(stack);
        buffer.writeEnum(dir);
    }

    @Override
    public ResourceLocation getID() {
        return TOPCompatibility.ELEMENT_MODULE_ITEM;
    }

    public static class Factory implements IElementFactory {
        @Override
        public IElement createElement(FriendlyByteBuf buf) {
            return new ElementModule(buf);
        }

        @Override
        public ResourceLocation getId() {
            return TOPCompatibility.ELEMENT_MODULE_ITEM;
        }
    }
}
