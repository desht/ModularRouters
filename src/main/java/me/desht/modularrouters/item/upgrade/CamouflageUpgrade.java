package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
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

public class CamouflageUpgrade extends ItemUpgrade {
    public static final String NBT_STATE_NAME = "BlockStateName";

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);

        router.setCamouflage(getCamoState(stack));
    }

    private static void setCamoState(ItemStack stack, BlockState camoState) {
        stack.getOrCreateTagElement(ModularRouters.MODID).put(NBT_STATE_NAME, NBTUtil.writeBlockState(camoState));
    }

    private static BlockState getCamoState(ItemStack stack) {
        CompoundNBT tag = stack.getTagElement(ModularRouters.MODID);
        return tag != null ? NBTUtil.readBlockState(tag.getCompound(NBT_STATE_NAME)) : null;
    }

    private static ITextComponent getCamoStateDisplayName(ItemStack stack) {
        BlockState state = getCamoState(stack);
        return state != null ? getCamoStateDisplayName(state) : new StringTextComponent("<?>");
    }

    private static ITextComponent getCamoStateDisplayName(BlockState camoState) {
        return new ItemStack(camoState.getBlock().asItem()).getHoverName();
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        assert player != null;
        ItemStack stack = ctx.getItemInHand();

        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (isBlockOKForCamo(state)) {
            setCamoState(stack, state);
            if (!ctx.getLevel().isClientSide) {
                player.displayClientMessage(new TranslationTextComponent("modularrouters.itemText.camouflage.held")
                        .append(TextFormatting.AQUA.toString())
                        .append(getCamoStateDisplayName(stack))
                        .withStyle(TextFormatting.YELLOW), true);
            } else {
                player.playSound(ModSounds.SUCCESS.get(), 1.0f, 1.5f);
            }
            return ActionResultType.SUCCESS;
        } else if (ctx.getLevel().isClientSide) {
            player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        BlockState camoState = getCamoState(stack);
        ITextComponent disp = super.getName(stack);
        if (camoState != null) {
            return disp.copy().append(": ").append(getCamoStateDisplayName(camoState)).withStyle(TextFormatting.YELLOW);
        } else {
            return disp;
        }
    }

    private static boolean isBlockOKForCamo(BlockState state) {
        // trying to camo a router as itself = recursion hell
        return state.getRenderShape() == BlockRenderType.MODEL && state.getBlock() != ModBlocks.ITEM_ROUTER.get()
                && !state.getBlock().getRegistryName().getNamespace().equals("chiselsandbits");
    }
}
