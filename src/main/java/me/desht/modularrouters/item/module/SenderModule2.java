package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class SenderModule2 extends TargetedSender {
    public enum TargetValidation {
        OK,
        ROUTER_MISSING,
        OUT_OF_RANGE,
        NOT_LOADED,
        NOT_INVENTORY;

        public boolean isOK() {
            return this == OK;
        }
    }

    @Override
    protected SenderModule1.SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        TargetedSender.DimensionPos target = settings.getTarget();
        SenderModule2 module = (SenderModule2) settings.getModule();

        if (!module.validateTarget(router, target).isOK()) {
            return null;
        }

        WorldServer w = DimensionManager.getWorld(target.dimId);
        IItemHandler handler = InventoryUtils.getInventory(w, target.pos, target.face);
        return handler == null ? null : new SenderModule1.SenderTarget(target.pos, handler);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addBasicInformation(itemstack, player, list, par4);
        DimensionPos dpos = getTarget(itemstack);
        if (dpos != null) {
            list.add(I18n.translateToLocalFormatted("itemText.misc.target", dpos.dimId, dpos.pos.getX(), dpos.pos.getY(), dpos.pos.getZ(), dpos.face.getName()));
            WorldServer w = DimensionManager.getWorld(dpos.dimId);
            if (w != null) {
                list.add("        (" + getBlockName(w, dpos.pos, w.getBlockState(dpos.pos)) + ")");
            }
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
                TargetValidation val = validateTarget(router, dpos);
                if (!val.isOK()) {
                    list.add(I18n.translateToLocal("itemText.targetValidation." + val));
                }
            }
        }
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] { Config.Defaults.SENDER2_BASE_RANGE, Config.Defaults.SENDER2_MAX_RANGE };
    }

    private String getBlockName(World w, BlockPos pos, IBlockState state) {
        if (state.getBlock().isAir(state, w, pos)) {
            return "-";
        } else {
            ItemStack stack = state.getBlock().getItem(w, pos, state);
            if (stack != null) {
                return stack.getDisplayName();
            } else {
                return state.getBlock().getLocalizedName();
            }
        }
    }

    protected TargetValidation validateTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos) {
        if (router == null) {
            return TargetValidation.ROUTER_MISSING;
        }
        if (router.getWorld().provider.getDimension() != dimPos.dimId) {
            return TargetValidation.OUT_OF_RANGE;
        }
        if (router.getPos().distanceSq(dimPos.pos.getX(), dimPos.pos.getY(), dimPos.pos.getZ()) > maxDistanceSq(router)) {
            return TargetValidation.OUT_OF_RANGE;
        }
        WorldServer w = DimensionManager.getWorld(dimPos.dimId);
        if (w == null || !w.getChunkProvider().chunkExists(dimPos.pos.getX() >> 4, dimPos.pos.getZ() >> 4)) {
            return TargetValidation.NOT_LOADED;
        }
        if (w.getTileEntity(dimPos.pos) == null) {
            return TargetValidation.NOT_INVENTORY;
        }
        return TargetValidation.OK;
    }

    private int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Math.min(Config.sender2BaseRange + router.getRangeUpgrades(), Config.sender2MaxRange);
        return r * r;
    }
}
