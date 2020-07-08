package me.desht.modularrouters.item.upgrade;

import com.google.common.collect.Sets;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SecurityUpgrade extends ItemUpgrade implements IPlayerOwned {
    private static final String NBT_PLAYERS = "Players";
    private static final int MAX_PLAYERS = 6;

    @Override
    public void addExtraInformation(ItemStack itemstack,  List<ITextComponent> list) {
        String owner = getOwnerName(itemstack);
        if (owner == null) owner = "-";
        list.add(MiscUtil.xlate("itemText.security.owner", TextFormatting.AQUA + owner));
        Set<String> names = getPlayerNames(itemstack);
        if (!names.isEmpty()) {
            list.add(MiscUtil.xlate("itemText.security.count", names.size(), MAX_PLAYERS));
            list.addAll(names.stream()
                    .map(name -> " \u2022 " + TextFormatting.YELLOW + name)
                    .sorted()
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);
        router.addPermittedIds(getPlayerIDs(stack));
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(64, 64, 255);
    }

    private Set<UUID> getPlayerIDs(ItemStack stack) {
        CompoundNBT compound = stack.getTag();
        if (compound == null) {
            return Collections.emptySet();
        }

        Set<UUID> res = Sets.newHashSet();
        UUID ownerID = getOwnerID(stack);
        if (ownerID == null) return Collections.emptySet();
        res.add(ownerID);

        if (compound.contains(NBT_PLAYERS)) {
            CompoundNBT p = compound.getCompound(NBT_PLAYERS);
            res.addAll(p.keySet().stream().map(UUID::fromString).collect(Collectors.toList()));
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
        CompoundNBT compound = stack.getTag();
        if (compound != null && compound.contains(NBT_PLAYERS)) {
            CompoundNBT p = compound.getCompound(NBT_PLAYERS);
            return Sets.newHashSet(p.keySet().stream().map(p::getString).sorted().collect(Collectors.toList()));
        } else {
            return Collections.emptySet();
        }
    }

    private static Result addPlayer(ItemStack stack, String id, String name) {
        CompoundNBT compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new CompoundNBT());
            }
            CompoundNBT p = compound.getCompound(NBT_PLAYERS);
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
        CompoundNBT compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new CompoundNBT());
            }
            CompoundNBT p = compound.getCompound(NBT_PLAYERS);
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
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.getEntityWorld().isRemote && player.isSteppingCarefully()) {
            setOwner(stack, player);
            player.sendStatusMessage(MiscUtil.xlate("itemText.security.owner", player.getDisplayName().getString()), false);
            return ActionResult.resultSuccess(stack);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity targetPlayer = (PlayerEntity)entity;
            String id = targetPlayer.getUniqueID().toString();
            String name = targetPlayer.getDisplayName().toString();
            Result res = player.isSteppingCarefully() ? removePlayer(stack, id) : addPlayer(stack, id, name);
            if (player.world.isRemote) {
                player.playSound(res.isError() ? ModSounds.ERROR.get() : ModSounds.SUCCESS.get(), 1.0f, 1.0f);
            } else {
                player.sendStatusMessage(MiscUtil.xlate("chatText.security." + res.toString(), name), false);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    enum Result {
        ADDED, REMOVED, FULL, ALREADY_ADDED, ERROR, NOT_PRESENT;

        boolean isError() {
            return this != ADDED && this != REMOVED;
        }
    }

}
