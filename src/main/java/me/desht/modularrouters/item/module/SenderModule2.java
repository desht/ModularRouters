package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class SenderModule2 extends TargetedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule2(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER2),
                ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1), Items.ENDER_EYE);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[]{Config.Defaults.SENDER2_BASE_RANGE, Config.Defaults.SENDER2_MAX_RANGE};
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public TargetValidation validateTarget(TileEntityItemRouter router, RouterTarget src, RouterTarget dst, boolean validateBlocks) {
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

    protected boolean isRangeLimited() {
        return true;
    }

    public static int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Math.min(Config.sender2BaseRange + (router == null ? 0 : router.getRangeUpgrades()), Config.sender2MaxRange);
        return r * r;
    }
}
