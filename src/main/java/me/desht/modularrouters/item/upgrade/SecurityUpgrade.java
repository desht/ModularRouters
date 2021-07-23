package me.desht.modularrouters.item.upgrade;

import com.google.common.collect.Sets;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.IPlayerOwned;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class SecurityUpgrade extends UpgradeItem implements IPlayerOwned {
    private static final String NBT_PLAYERS = "Players";
    private static final int MAX_PLAYERS = 6;

    @Override
    public void addExtraInformation(ItemStack itemstack,  List<Component> list) {
        String owner = getOwnerName(itemstack);
        if (owner == null) owner = "-";
        list.add(ClientUtil.xlate("modularrouters.itemText.security.owner", ChatFormatting.AQUA + owner));
        Set<String> names = getPlayerNames(itemstack);
        if (!names.isEmpty()) {
            list.add(ClientUtil.xlate("modularrouters.itemText.security.count", names.size(), MAX_PLAYERS));
            list.addAll(names.stream()
                    .map(name -> " \u2022 " + ChatFormatting.YELLOW + name)
                    .sorted()
                    .map(TextComponent::new)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        super.onCompiled(stack, router);
        router.addPermittedIds(getPlayerIDs(stack));
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(64, 64, 255);
    }

    private Set<UUID> getPlayerIDs(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            return Collections.emptySet();
        }

        Set<UUID> res = Sets.newHashSet();
        UUID ownerID = getOwnerID(stack);
        if (ownerID == null) return Collections.emptySet();
        res.add(ownerID);

        if (compound.contains(NBT_PLAYERS)) {
            CompoundTag p = compound.getCompound(NBT_PLAYERS);
            res.addAll(p.getAllKeys().stream().map(UUID::fromString).collect(Collectors.toList()));
        }
        return res;
    }

    /**
     * Get a items of player names added to this security upgrade, not including the owner.
     *
     * @param stack the upgrade itemstack
     * @return set of (displayable) player names
     */
    private static Set<String> getPlayerNames(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(NBT_PLAYERS)) {
            CompoundTag p = compound.getCompound(NBT_PLAYERS);
            return p.getAllKeys().stream().map(p::getString).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            return Collections.emptySet();
        }
    }

    private static Result addPlayer(ItemStack stack, String id, String name) {
        CompoundTag compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new CompoundTag());
            }
            CompoundTag p = compound.getCompound(NBT_PLAYERS);
            if (p.contains(id)) {
                return Result.ALREADY_ADDED;  // already there, do nothing
            }
            if (p.size() >= MAX_PLAYERS) {
                return Result.FULL;  // items full
            }
            p.putString(id, name);
            return Result.ADDED;
        }
        return Result.ERROR;
    }

    private static Result removePlayer(ItemStack stack, String id) {
        CompoundTag compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new CompoundTag());
            }
            CompoundTag p = compound.getCompound(NBT_PLAYERS);
            if (p.contains(id)) {
                p.remove(id);
                return Result.REMOVED;
            } else {
                return Result.NOT_PRESENT;
            }
        }
        return Result.ERROR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getCommandSenderWorld().isClientSide && player.isSteppingCarefully()) {
            setOwner(stack, player);
            player.displayClientMessage(new TranslatableComponent("modularrouters.itemText.security.owner", player.getDisplayName().getString()), false);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof Player) {
            Player targetPlayer = (Player)entity;
            String id = targetPlayer.getUUID().toString();
            String name = targetPlayer.getDisplayName().toString();
            Result res = player.isSteppingCarefully() ? removePlayer(stack, id) : addPlayer(stack, id, name);
            if (player.level.isClientSide) {
                player.playSound(res.isError() ? ModSounds.ERROR.get() : ModSounds.SUCCESS.get(), 1.0f, 1.0f);
            } else {
                player.displayClientMessage(new TranslatableComponent("modularrouters.chatText.security." + res.toString(), name), false);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    enum Result {
        ADDED, REMOVED, FULL, ALREADY_ADDED, ERROR, NOT_PRESENT;

        boolean isError() {
            return this != ADDED && this != REMOVED;
        }
    }

}
