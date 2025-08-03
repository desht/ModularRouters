package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRoutersTags;
import me.desht.modularrouters.block.CamouflageableBlock;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class CamouflageUpgrade extends UpgradeItem {
    public static final String NBT_STATE_NAME = "BlockStateName";

    public CamouflageUpgrade(Properties properties) {
        super(properties);
    }

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        super.onCompiled(stack, router);

        router.setCamouflage(getCamoState(stack));
    }

    private static void setCamoState(ItemStack stack, BlockState camoState) {
        stack.set(ModDataComponents.CAMOUFLAGE, camoState);
    }

    private static BlockState getCamoState(ItemStack stack) {
        return stack.get(ModDataComponents.CAMOUFLAGE);
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
                player.playSound(ModSounds.SUCCESS.get(), ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.5f);
            }
            return InteractionResult.SUCCESS;
        } else if (ctx.getLevel().isClientSide) {
            player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.badCamoBlock").withStyle(ChatFormatting.RED), true);
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
        // trying to camo a router as another camo block = recursion hell
        return state.getRenderShape() == RenderShape.MODEL && !(state.getBlock() instanceof CamouflageableBlock)
                && !state.is(ModularRoutersTags.Blocks.CAMO_BLACKLIST)
                && !BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("chiselsandbits");
    }
}
