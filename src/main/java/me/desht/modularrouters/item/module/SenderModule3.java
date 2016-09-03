package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.awt.*;

public class SenderModule3 extends SenderModule2 {
    @Override
    protected void playParticles(TileEntityItemRouter router, CompiledModule settings, BlockPos targetPos, float val) {
        double x = router.getPos().getX() + 0.5;
        double y = router.getPos().getY() + 0.5;
        double z = router.getPos().getZ() + 0.5;
        EnumFacing facing = router.getAbsoluteFacing(RelativeDirection.FRONT);
        double x2 = x + facing.getFrontOffsetX() * 1.5;
        double z2 = z + facing.getFrontOffsetZ() * 1.5;
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), x, y, z, 32);
        ModularRouters.network.sendToAllAround(new ParticleBeamMessage(x, y, z, x2, y, z2, Color.getHSBColor(0.83333f, 1.0f, 0.8f)), point);
    }

    @Override
    protected boolean isRangeLimited() {
        return false;
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER3),
                ItemModule.makeItemStack(ItemModule.ModuleType.SENDER2), Blocks.END_STONE, Blocks.ENDER_CHEST);
    }
}
