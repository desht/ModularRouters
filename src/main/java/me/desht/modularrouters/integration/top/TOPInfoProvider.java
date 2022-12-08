package me.desht.modularrouters.integration.top;

class TOPInfoProvider {
//    static void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
//        world.getBlockEntity(data.getPos(), ModBlockEntities.MODULAR_ROUTER.get()).ifPresent(router -> {
//            if (router.isPermitted(player)) {
//                IItemHandler modules = router.getModules();
//                IProbeInfo sub = probeInfo.horizontal();
//                for (int i = 0; i < modules.getSlots(); i++) {
//                    ItemStack stack = modules.getStackInSlot(i);
//                    if (!stack.isEmpty()) {
//                        sub.element(new ElementModule(stack));
//                    }
//                }
//                sub = probeInfo.horizontal();
//                IItemHandler upgrades = router.getUpgrades();
//                for (int i = 0; i < upgrades.getSlots(); i++) {
//                    ItemStack stack = upgrades.getStackInSlot(i);
//                    if (!stack.isEmpty()) {
//                        sub.item(stack);
//                    }
//                }
//
//                probeInfo.text(Component.literal(ChatFormatting.YELLOW.toString())
//                        .append(Component.translatable("modularrouters.guiText.tooltip.redstone.label"))
//                        .append(ChatFormatting.WHITE + ": ")
//                        .append(Component.translatable(router.getRedstoneBehaviour().getTranslationKey()))
//                );
//            } else {
//                probeInfo.text(Component.translatable("modularrouters.chatText.security.accessDenied"));
//            }
//        });
//    }
}
