package me.desht.modularrouters.item.module.adapter;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public sealed interface IItemAdapter permits TargetedModuleAdapter, IItemAdapter.NoOp {
    default InteractionResult useOn(UseOnContext ctx) {
        return InteractionResult.PASS;
    }

    default InteractionResult onSneakRightClick(ItemStack stack, Level world, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    default void doModuleValidation(ItemStack stack, ServerPlayer player) {

    }

    default void addSettingsInformation(ItemStack stack, Consumer<Component> list) {

    }

    default void addUsageInformation(ItemStack itemstack, Consumer<Component> list) {}

    record NoOp() implements IItemAdapter {}
}
