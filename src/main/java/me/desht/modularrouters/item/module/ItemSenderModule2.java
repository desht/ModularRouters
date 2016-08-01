package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.Sender2Executor;
import me.desht.modularrouters.util.MiscUtil;
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

import java.util.List;

public class ItemSenderModule2 extends TargetedSender implements IRangeLimited {
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

    public ItemSenderModule2() {
        super("senderModule2");
    }

    ItemSenderModule2(String name) {
        super(name);
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender2Executor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        DimensionPos dpos = getTarget(itemstack);
        if (dpos != null) {
            WorldServer w = DimensionManager.getWorld(dpos.dimId);
            if (w != null) {
                list.add(I18n.translateToLocalFormatted("itemText.misc.target", dpos.dimId, dpos.pos.getX(), dpos.pos.getY(), dpos.pos.getZ(), dpos.face.getName()));
                list.add("        (" + getBlockName(w, dpos.pos, w.getBlockState(dpos.pos)) + ")");
            }
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).tileEntityItemRouter;
                TargetValidation val = validateTarget(router, dpos);
                if (!val.isOK()) {
                    list.add(I18n.translateToLocal("itemText.targetValidation." + val));
                }
            }
        }
        super.addInformation(itemstack, player, list, par4);
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

    @Override
    public TargetValidation validateTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos) {
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

    @Override
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + getUnlocalizedName(itemstack), Config.Defaults.SENDER2_BASE_RANGE, Config.Defaults.SENDER2_MAX_RANGE);
    }

    private int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Math.min(Config.sender2BaseRange + router.getRangeUpgrades(), Config.sender2MaxRange);
        return r * r;
    }
}
