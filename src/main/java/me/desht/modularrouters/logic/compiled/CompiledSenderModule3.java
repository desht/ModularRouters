package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.ItemBeamMessage;
import me.desht.modularrouters.network.PacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    private static final TintColor PARTICLE_COLOR = new TintColor(255, 0, 255);

    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean isRangeLimited() {
        return false;
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            Vector3d vec = Vector3d.func_237489_a_(router.getPos());
            Direction facing = router.getAbsoluteFacing(ItemModule.RelativeDirection.FRONT);
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, 32, router.getWorld().func_234923_W_());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ItemBeamMessage(router, router.getPos().offset(facing, 2), false, stack, PARTICLE_COLOR.getRGB(), router.getTickRate()).withFadeout());
        }
    }
}
