package me.desht.modularrouters.client.render.area;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public enum AreaShowManager {
    INSTANCE;

    private final Map<BlockPos, AreaShowHandler> showHandlers = new HashMap<>();
    private World world;

    public static AreaShowManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        ItemStack curItem = Minecraft.getInstance().player.getHeldItemMainhand();
        IPositionProvider positionProvider = getPositionProvider(curItem);
        if (positionProvider != null) {
            CompiledPosition cp = new CompiledPosition(curItem, positionProvider);
            GlStateManager.disableDepthTest();
            new AreaShowHandler(cp).render();
            GlStateManager.enableDepthTest();
        }
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private IPositionProvider getPositionProvider(ItemStack stack) {
        if (stack.getItem() instanceof IPositionProvider) {
            return (IPositionProvider) stack.getItem();
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        PlayerEntity player = ModularRouters.proxy.getClientPlayer();
        if (player != null) {
            if (player.world != world) {
                world = player.world;
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> distBetweenSq(pos, player.posX, player.posY, player.posZ) < 32 * 32 && world.isAirBlock(pos));
                }
            }
        }
    }

    public static double distBetweenSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2);
    }

    public static double distBetweenSq(BlockPos pos1, double x1, double y1, double z1) {
        return distBetweenSq(pos1.getX(), pos1.getY(), pos1.getZ(), x1, y1, z1);
    }

    static class CompiledPosition {
        Map<BlockPos, FaceAndColour> positions = new HashMap<>();

        CompiledPosition(ItemStack stack, IPositionProvider provider) {
            List<ModuleTarget> targets = provider.getStoredPositions(stack);
            for (int i = 0; i < targets.size(); i++) {
                ModuleTarget target = targets.get(i);
                if (target.isSameWorld(Minecraft.getInstance().world)) {
                    BlockPos pos = target.gPos.getPos();
                    if (positions.containsKey(pos)) {
                        positions.get(pos).faces.set(target.face.ordinal());
                    } else {
                        FaceAndColour fc = new FaceAndColour(new BitSet(6), provider.getRenderColor(i));
                        fc.faces.set(target.face.ordinal());
                        positions.put(pos, fc);
                    }
                }
            }
        }

        Set<BlockPos> getPositions() {
            return positions.keySet();
        }

        boolean checkFace(BlockPos pos, Direction face) {
            return positions.containsKey(pos) && positions.get(pos).faces.get(face.getIndex());
        }

        int getColour(BlockPos pos) {
            return positions.containsKey(pos) ? positions.get(pos).colour : 0;
        }

        static class FaceAndColour {
            final BitSet faces;
            final int colour;

            FaceAndColour(BitSet faces, int colour) {
                this.faces = faces;
                this.colour = colour;
            }
        }
    }

}
