package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.stream.Collectors;

public enum CamoRenderer {
    INSTANCE;

    private AreaRenderer camoPositionShower;
    private BlockPos lastPlayerPos;

    public static CamoRenderer getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (!MRConfig.Client.Misc.heldRouterShowsCamoRouters || player == null || player.getHeldItemMainhand().getItem() != ModBlocks.ITEM_ROUTER.get().asItem()) {
            lastPlayerPos = null;
            return;
        }

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.push();
        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        highlightCamoRouters(matrixStack, buffer, player);
        matrixStack.pop();
    }

    private void highlightCamoRouters(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        if (lastPlayerPos == null || camoPositionShower == null || player.getDistanceSq(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.getPosition();
            Set<BlockPos> s = player.getEntityWorld().loadedTileEntityList.stream()
                    .filter(te -> te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null && te.getPos().distanceSq(lastPlayerPos) < 256)
                    .map(TileEntity::getPos)
                    .collect(Collectors.toSet());
            camoPositionShower = new AreaRenderer(s, 0x408080FF, 0.75f);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render(matrixStack, buffer);
        }
    }
}
