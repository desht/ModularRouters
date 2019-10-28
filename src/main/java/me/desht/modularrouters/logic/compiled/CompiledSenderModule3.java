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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.PacketDistributor;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    private static final TintColor particleColor = new TintColor(255, 0, 255);

    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean isRangeLimited() {
        return false;
    }

    @Override
    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE) < 2) {
            Vec3d vec = new Vec3d(router.getPos());
            Direction facing = router.getAbsoluteFacing(ItemModule.RelativeDirection.FRONT);
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, 32, router.getWorld().dimension.getType());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ItemBeamMessage(router.getPos(), router.getPos().offset(facing, 2), stack, particleColor.getRGB(), router.getTickRate()).withFadeout());
        }
    }
}
