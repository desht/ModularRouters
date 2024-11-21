package me.desht.modularrouters.item.module.adapter;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public sealed interface IItemAdapter permits TargetedModuleAdapter, IItemAdapter.NoOp {
    default InteractionResult useOn(UseOnContext ctx) {
        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> onSneakRightClick(ItemStack stack, Level world, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(stack);
    }

    default void doModuleValidation(ItemStack stack, ServerPlayer player) {

    }

    default void addSettingsInformation(ItemStack stack, List<Component> list) {

    }

    default void addUsageInformation(ItemStack itemstack, List<Component> list) {}

    record NoOp() implements IItemAdapter {}
}
