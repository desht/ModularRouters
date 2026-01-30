//package me.desht.modularrouters.integration.top;
//
//import mcjty.theoneprobe.api.IElement;
//import mcjty.theoneprobe.api.IElementFactory;
//import me.desht.modularrouters.item.module.ModuleItem;
//import me.desht.modularrouters.logic.settings.RelativeDirection;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.item.ItemStack;
//import org.apache.commons.lang3.Validate;
//
//public class ElementModule implements IElement {
//    private final ItemStack stack;
//    private final RelativeDirection dir;
//
//    public ElementModule(ItemStack stack) {
//        Validate.isTrue(stack.getItem() instanceof ModuleItem, "provided item stack is not an ItemModule!");
//        this.stack = stack;
//        this.dir = ModuleItem.getCommonSettings(stack).facing();
//    }
//
//    public ElementModule(RegistryFriendlyByteBuf buf) {
//        this.stack = ItemStack.STREAM_CODEC.decode(buf);
//        this.dir = buf.readEnum(RelativeDirection.class);
//    }
//
//    @Override
//    public void render(GuiGraphics graphics, int x, int y) {
//        graphics.renderItem(stack, x + (getWidth() - 18) / 2, y + (getHeight() - 18) / 2);
//        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + (getWidth() - 18) / 2, y + (getHeight() - 18) / 2, dir.getSymbol());
//    }
//
//    @Override
//    public int getWidth() {
//        return 20;
//    }
//
//    @Override
//    public int getHeight() {
//        return 20;
//    }
//
//    @Override
//    public void toBytes(RegistryFriendlyByteBuf buffer) {
//        ItemStack.STREAM_CODEC.encode(buffer, stack);
//        buffer.writeEnum(dir);
//    }
//
//    @Override
//    public Identifier getID() {
//        return TOPCompatibility.ELEMENT_MODULE_ITEM;
//    }
//
//    public static class Factory implements IElementFactory {
//        @Override
//        public IElement createElement(RegistryFriendlyByteBuf buf) {
//            return new ElementModule(buf);
//        }
//
//        @Override
//        public Identifier getId() {
//            return TOPCompatibility.ELEMENT_MODULE_ITEM;
//        }
//    }
//}
