package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class CamouflageUpgrade extends UpgradeItem {
    public static final String NBT_STATE_NAME = "BlockStateName";

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        super.onCompiled(stack, router);

        router.setCamouflage(getCamoState(stack));
    }

    private static void setCamoState(ItemStack stack, BlockState camoState) {
        stack.getOrCreateTagElement(ModularRouters.MODID).put(NBT_STATE_NAME, NbtUtils.writeBlockState(camoState));
    }

    private static BlockState getCamoState(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(ModularRouters.MODID);
        return tag != null ? NbtUtils.readBlockState(tag.getCompound(NBT_STATE_NAME)) : null;
    }

    private static Component getCamoStateDisplayName(ItemStack stack) {
        BlockState state = getCamoState(stack);
        return state != null ? getCamoStateDisplayName(state) : Component.literal("<?>");
    }

    private static Component getCamoStateDisplayName(BlockState camoState) {
        return new ItemStack(camoState.getBlock().asItem()).getHoverName();
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        assert player != null;
        ItemStack stack = ctx.getItemInHand();

        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (isBlockOKForCamo(state)) {
            setCamoState(stack, state);
            if (!ctx.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("modularrouters.itemText.camouflage.held")
                        .append(ChatFormatting.AQUA.toString())
                        .append(getCamoStateDisplayName(stack))
                        .withStyle(ChatFormatting.YELLOW), true);
            } else {
                player.playSound(ModSounds.SUCCESS.get(), 1.0f, 1.5f);
            }
            return InteractionResult.SUCCESS;
        } else if (ctx.getLevel().isClientSide) {
            player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        BlockState camoState = getCamoState(stack);
        Component disp = super.getName(stack);
        if (camoState != null) {
            return disp.copy().append(": ").append(getCamoStateDisplayName(camoState)).withStyle(ChatFormatting.YELLOW);
        } else {
            return disp;
        }
    }

    private static boolean isBlockOKForCamo(BlockState state) {
        // trying to camo a router as itself = recursion hell
        return state.getRenderShape() == RenderShape.MODEL && state.getBlock() != ModBlocks.MODULAR_ROUTER.get()
                && !MiscUtil.getRegistryName(state.getBlock()).orElseThrow().getNamespace().equals("chiselsandbits");
    }
}
