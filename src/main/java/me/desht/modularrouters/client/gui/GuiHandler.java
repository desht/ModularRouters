package me.desht.modularrouters.client.gui;

public class GuiHandler {

//    public static Screen openGui(FMLPlayMessages.OpenContainer msg) {
//        PlayerEntity player = Minecraft.getInstance().player;
//        PacketBuffer xtra = msg.getAdditionalData();
//        switch (msg.getId().getPath()) {
//            case "module_held":
//                Hand hand = xtra.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
//                return ModuleGuiFactory.createGui(player, hand);
//            case "module_installed":
//                BlockPos routerPos = xtra.readBlockPos();
//                return ModuleGuiFactory.createGui(player, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos));
//            case "filter_held":
//                Hand hand1 = xtra.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
//                return FilterGuiFactory.createGui(player, hand1);
//            case "filter_installed":
//                BlockPos routerPos1 = xtra.readBlockPos();
//                return FilterGuiFactory.createGui(player, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos1));
//            case "item_router":
//                BlockPos routerPos2 = xtra.readBlockPos();
//                return new GuiItemRouter(player.inventory, TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos2));
//        }
//        return null;
//    }
}
