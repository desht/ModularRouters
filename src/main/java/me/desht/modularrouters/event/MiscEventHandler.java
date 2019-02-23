package me.desht.modularrouters.event;

import me.desht.modularrouters.block.BlockTemplateFrame;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MiscEventHandler {
    @SubscribeEvent
    public static void onDigSpeedCheck(PlayerEvent.BreakSpeed event) {
        if (event.getPos() != null) {
            IBlockState state = event.getEntityPlayer().getEntityWorld().getBlockState(event.getPos());
            if (state.getBlock() instanceof BlockTemplateFrame) {
                TileEntityTemplateFrame te = TileEntityTemplateFrame.getTileEntitySafely(event.getEntityPlayer().getEntityWorld(), event.getPos());
                if (te != null && te.getCamouflage() != null && te.extendedMimic()) {
                    IBlockState camoState = te.getCamouflage();
                    event.setNewSpeed(event.getEntityPlayer().getDigSpeed(camoState, null));
                }
            }
        }
    }
}
