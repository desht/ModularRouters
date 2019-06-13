package me.desht.modularrouters.block;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTemplateFrame extends BlockCamo {
    private static final String BLOCK_NAME = "template_frame";

    public BlockTemplateFrame() {
        super(Properties.create(Material.SNOW));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return ImmutableList.of();
    }
}
