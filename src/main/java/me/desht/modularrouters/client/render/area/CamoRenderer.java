package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public enum CamoRenderer {
    INSTANCE;

    private AreaRenderer camoPositionShower;
    private BlockPos lastPlayerPos;

    public static CamoRenderer getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Player player = Minecraft.getInstance().player;
        if (!MRConfig.Client.Misc.heldRouterShowsCamoRouters || player == null || !playerHoldingRouter(player)) {
            lastPlayerPos = null;
            return;
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack matrixStack = event.getMatrixStack();

        matrixStack.pushPose();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        highlightCamoRouters(matrixStack, buffer, player);
        matrixStack.popPose();
    }

    private static boolean playerHoldingRouter(Player player) {
        Item router = ModBlocks.MODULAR_ROUTER.get().asItem();
        return player.getMainHandItem().getItem() == router || player.getOffhandItem().getItem() == router;
    }

    private void highlightCamoRouters(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        // re-detect all nearby camo'd routers within 16 blocks if player has moved more than 3 blocks since last detection run
        if (lastPlayerPos == null || camoPositionShower == null
                || player.distanceToSqr(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.blockPosition();
            camoPositionShower = new AreaRenderer(getNearbyCamouflaged(player), 0x408080FF, 0.75f);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render(matrixStack, buffer);
        }
    }

    private Set<BlockPos> getNearbyCamouflaged(Player player) {
        Set<BlockPos> res = new HashSet<>();
        for (int x = -16; x <= 16; x += 16) {
            for (int z = -16; z <= 16; z += 16) {
                ChunkAccess c = player.getCommandSenderWorld().getChunk(lastPlayerPos.offset(x, 0, z));
                if (c instanceof LevelChunk lc) {
                    lc.getBlockEntities().forEach((pos, be) -> {
                        if (ICamouflageable.isCamouflaged(be) && pos.distSqr(lastPlayerPos) < 256) {
                            res.add(pos);
                        }
                    });
                }
            }
        }
        return res;
    }
}
