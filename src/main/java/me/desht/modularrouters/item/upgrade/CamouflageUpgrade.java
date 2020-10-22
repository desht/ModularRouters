package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModSounds;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class CamouflageUpgrade extends ItemUpgrade {
    public static final String NBT_STATE_NAME = "BlockStateName";

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        CompoundNBT tag = itemstack.getChildTag(ModularRouters.MODID);
        if (tag != null && tag.contains(NBT_STATE_NAME)) {
            list.add(ClientUtil.xlate("modularrouters.itemText.camouflage.held")
                    .appendString(TextFormatting.AQUA.toString())
                    .append(getCamoStateDisplayName(itemstack)));
        }
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);

        router.setCamouflage(getCamoState(stack));
    }

    private static void setCamoState(ItemStack stack, BlockState camoState) {
        stack.getOrCreateChildTag(ModularRouters.MODID).put(NBT_STATE_NAME, NBTUtil.writeBlockState(camoState));
    }

    private static BlockState getCamoState(ItemStack stack) {
        CompoundNBT tag = stack.getChildTag(ModularRouters.MODID);
        return tag != null ? NBTUtil.readBlockState(tag.getCompound(NBT_STATE_NAME)) : null;
    }

    private static ITextComponent getCamoStateDisplayName(ItemStack stack) {
        BlockState state = getCamoState(stack);
        return state != null ? new ItemStack(state.getBlock().asItem()).getDisplayName() : new StringTextComponent("<?>");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        assert player != null;
        ItemStack stack = ctx.getItem();

        BlockState state = ctx.getWorld().getBlockState(ctx.getPos());
        if (isBlockOKForCamo(state)) {
            setCamoState(stack, state);
            if (!ctx.getWorld().isRemote) {
                player.sendStatusMessage(new TranslationTextComponent("modularrouters.itemText.camouflage.held")
                        .appendString(TextFormatting.AQUA.toString())
                        .append(getCamoStateDisplayName(stack))
                        .mergeStyle(TextFormatting.YELLOW), false);
            } else {
                player.playSound(ModSounds.SUCCESS.get(), 1.0f, 1.5f);
            }
            return ActionResultType.SUCCESS;
        } else if (ctx.getWorld().isRemote) {
            player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    private static boolean isBlockOKForCamo(BlockState state) {
        // trying to camo a router as itself = recursion hell
        return state.getRenderType() == BlockRenderType.MODEL && state.getBlock() != ModBlocks.ITEM_ROUTER.get();
    }
}
