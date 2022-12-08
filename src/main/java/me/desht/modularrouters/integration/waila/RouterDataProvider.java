package me.desht.modularrouters.integration.waila;

public class RouterDataProvider /*implements IServerDataProvider<BlockEntity>*/ {
//    @Override
//    public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
//        if (blockEntity instanceof ModularRouterBlockEntity router) {
//            if (router.isPermitted(serverPlayer)) {
//                compoundTag.putInt("ModuleCount", router.getModuleCount());
//                compoundTag.putInt("RedstoneMode", router.getRedstoneBehaviour().ordinal());
//                compoundTag.putBoolean("EcoMode", router.getEcoMode());
//                compoundTag.put("Upgrades", getUpgrades(router));
//            } else {
//                compoundTag.putBoolean("Denied", true);
//            }
//        }
//    }
//
//    private CompoundTag getUpgrades(ModularRouterBlockEntity router) {
//        IItemHandler handler = router.getUpgrades();
//        Map<Item, Integer> counts = new HashMap<>();
//        for (int i = 0; i < handler.getSlots(); i++) {
//            ItemStack stack = handler.getStackInSlot(i);
//            if (stack.getItem() instanceof UpgradeItem) {
//                counts.put(stack.getItem(), counts.getOrDefault(stack.getItem(), 0) + stack.getCount());
//            }
//        }
//        CompoundTag upgrades = new CompoundTag();
//        counts.forEach((k, v) -> upgrades.putInt(k.getDescriptionId(), v));
//        return upgrades;
//    }
//
//    @Override
//    public ResourceLocation getUid() {
//        return RL("router");
//    }
}
