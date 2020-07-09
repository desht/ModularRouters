package me.desht.modularrouters.integration.top;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

class TOPInfoProvider {
    static void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntityItemRouter.getRouterAt(world, data.getPos()).ifPresent(router -> {
            if (router.isPermitted(player)) {
                IItemHandler modules = router.getModules();
                IProbeInfo sub = probeInfo.horizontal();
                for (int i = 0; i < modules.getSlots(); i++) {
                    ItemStack stack = modules.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        sub.element(new ElementModule(stack));
                    }
                }
                sub = probeInfo.horizontal();
                IItemHandler upgrades = router.getUpgrades();
                for (int i = 0; i < upgrades.getSlots(); i++) {
                    ItemStack stack = upgrades.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        sub.item(stack);
                    }
                }

                probeInfo.text(new StringTextComponent(TextFormatting.YELLOW.toString())
                        .func_230529_a_(new TranslationTextComponent("guiText.tooltip.redstone.label"))
                        .func_240702_b_(TextFormatting.WHITE + ": ")
                        .func_230529_a_(new TranslationTextComponent(router.getRedstoneBehaviour().getTranslationKey()))
                );
            } else {
                probeInfo.text(new TranslationTextComponent("chatText.security.accessDenied"));
            }
        });
    }
}
