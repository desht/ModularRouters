package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class SenderModule2 extends TargetedSender {

    @Override
    protected SenderModule1.SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModule settings) {
        RouterTarget target = settings.getTarget();
        SenderModule2 module = (SenderModule2) settings.getModule();

        if (!module.validateTarget(router, target, true).isOK()) {
            return null;
        }

        WorldServer w = DimensionManager.getWorld(target.dimId);
        if (!w.getChunkProvider().chunkExists(target.pos.getX() >> 4, target.pos.getZ() >> 4)) {
            return null;
        }
        IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
        return handler == null ? null : new SenderModule1.SenderTarget(target.pos, handler);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(itemstack, player, list, par4);
        RouterTarget target = getTarget(null, itemstack);
        if (target != null) {
            list.add(I18n.format("itemText.misc.target", target.dimId, target.pos.getX(), target.pos.getY(), target.pos.getZ(), target.face.getName()));
            if (target.dimId == player.getEntityWorld().provider.getDimension()) {
                String name = getBlockName(ModularRouters.proxy.theClientWorld(), target.pos);
                if (name != null) {
                    list.add("        (" + name + ")");
                }
            }
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
                TargetValidation val = validateTarget(router, target, false);
                if (val != TargetValidation.OK) {
                    list.add(I18n.format("itemText.targetValidation." + val));
                }
            }
        }
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] { Config.Defaults.SENDER2_BASE_RANGE, Config.Defaults.SENDER2_MAX_RANGE };
    }

    private String getBlockName(World w, BlockPos pos) {
        if (w == null) {
            return null;
        }
        IBlockState state = w.getBlockState(pos);
        if (state.getBlock().isAir(state, w, pos)) {
            return null;
        } else {
            ItemStack stack = state.getBlock().getItem(w, pos, state);
            if (stack != null) {
                return stack.getDisplayName();
            } else {
                return state.getBlock().getLocalizedName();
            }
        }
    }

    protected boolean isRangeLimited() {
        return true;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    protected TargetValidation validateTarget(TileEntityItemRouter router, RouterTarget src, RouterTarget dst, boolean validateBlocks) {
        if (isRangeLimited() && (src.dimId != dst.dimId || src.pos.distanceSq(dst.pos) > maxDistanceSq(router))) {
            return TargetValidation.OUT_OF_RANGE;
        }

        // validateBlocks will be true only when this is called server-side by left-clicking the module in hand,
        // or when the router is actually executing the module;
        // we can't reliably validate chunk loading or inventory presence on the client (for tooltip generation)
        if (validateBlocks) {
            WorldServer w = DimensionManager.getWorld(dst.dimId);
            if (w == null || !w.getChunkProvider().chunkExists(dst.pos.getX() >> 4, dst.pos.getZ() >> 4)) {
                return TargetValidation.NOT_LOADED;
            }
            if (w.getTileEntity(dst.pos) == null) {
                return TargetValidation.NOT_INVENTORY;
            }
        }
        return TargetValidation.OK;
    }

    protected TargetValidation validateTarget(TileEntityItemRouter router, RouterTarget dst, boolean validateBlocks) {
        return validateTarget(router, new RouterTarget(router.getWorld().provider.getDimension(), router.getPos(), null), dst, validateBlocks);
    }

    private int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Math.min(Config.sender2BaseRange + (router == null ? 0 : router.getRangeUpgrades()), Config.sender2MaxRange);
        return r * r;
    }
}
