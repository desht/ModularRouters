package me.desht.modularrouters.client;

import com.google.common.collect.ImmutableSet;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AreaShowManager {
    INSTANCE;

    private final Map<BlockPos, AreaShowHandler> showHandlers = new HashMap<>();
    private World world;

    public static AreaShowManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        ItemStack curItem = player.getHeldItemMainhand();
        IPositionProvider positionProvider = getPositionProvider(curItem);
        if (positionProvider != null) {
            List<BlockPos> posList = positionProvider.getStoredPositions(curItem);
            if (posList != null) {
                for (int i = 0; i < posList.size(); i++) {
                    if (posList.get(i) != null && positionProvider.getRenderColor(i) != 0) {
                        GlStateManager.disableDepth();
                        new AreaShowHandler(ImmutableSet.of(posList.get(i)), positionProvider.getRenderColor(i)).render();
                        GlStateManager.enableDepth();
                    }
                }
            }
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private IPositionProvider getPositionProvider(ItemStack stack) {
        if (stack.getItem() instanceof IPositionProvider) {
            return (IPositionProvider) stack.getItem();
        } else if (ItemModule.getModule(stack) instanceof IPositionProvider) {
            return (IPositionProvider) ItemModule.getModule(stack);
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        EntityPlayer player = ModularRouters.proxy.getPlayer();
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
}
