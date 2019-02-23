package me.desht.modularrouters.item.upgrade;

import com.google.common.collect.Sets;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.desht.modularrouters.core.ObjectRegistry.SOUND_ERROR;
import static me.desht.modularrouters.core.ObjectRegistry.SOUND_SUCCESS;

public class SecurityUpgrade extends ItemUpgrade {
    private static final String NBT_PLAYERS = "Players";
    private static final int MAX_PLAYERS = 6;

    public SecurityUpgrade(Properties props) {
        super(props);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack,  List<ITextComponent> list) {
        list.add(new TextComponentTranslation("itemText.security.owner", TextFormatting.YELLOW + getOwnerName(itemstack)));
        Set<String> names = getPlayerNames(itemstack);
        if (!names.isEmpty()) {
            list.add(new TextComponentTranslation("itemText.security.count", names.size(), MAX_PLAYERS));
            list.addAll(names.stream()
                    .map(name -> " \u2022 " + TextFormatting.YELLOW + name)
                    .sorted()
                    .map(TextComponentString::new)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);
        router.addPermittedIds(getPlayerIDs(stack));
    }

    @Override
    public Color getItemTint() {
        return new Color(64, 64, 255);
    }

    private static Set<UUID> getPlayerIDs(ItemStack stack) {
        NBTTagCompound compound = stack.getTag();
        if (compound == null) {
            return Collections.emptySet();
        }

        Set<UUID> res = Sets.newHashSet();
        Pair<String, UUID> owner = ModuleHelper.getOwnerNameAndId(stack);
        res.add(owner.getRight());

        if (compound.contains(NBT_PLAYERS)) {
            NBTTagCompound p = compound.getCompound(NBT_PLAYERS);
            res.addAll(p.keySet().stream().map(UUID::fromString).collect(Collectors.toList()));
        }
        return res;
    }

    /**
     * Get this security upgrade's owner.
     *
     * @param stack the upgrade itemstack
     * @return (displayable) owner name
     */
    private static String getOwnerName(ItemStack stack) {
        Pair<String, UUID> owner = ModuleHelper.getOwnerNameAndId(stack);
        return owner.getLeft().isEmpty() ? "???" : owner.getLeft();
    }

    /**
     * Get a items of player names added to this security upgrade, not including the owner.
     *
     * @param stack the upgrade itemstack
     * @return set of (displayable) player names
     */
    private static Set<String> getPlayerNames(ItemStack stack) {
        NBTTagCompound compound = stack.getTag();
        if (compound != null && compound.contains(NBT_PLAYERS)) {
            NBTTagCompound p = compound.getCompound(NBT_PLAYERS);
            return Sets.newHashSet(p.keySet().stream().map(p::getString).sorted().collect(Collectors.toList()));
        } else {
            return Collections.emptySet();
        }
    }

    private static Result addPlayer(ItemStack stack, String id, String name) {
        NBTTagCompound compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new NBTTagCompound());
            }
            NBTTagCompound p = compound.getCompound(NBT_PLAYERS);
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
        NBTTagCompound compound = stack.getTag();
        if (compound != null) {
            if (!compound.contains(NBT_PLAYERS)) {
                compound.put(NBT_PLAYERS, new NBTTagCompound());
            }
            NBTTagCompound p = compound.getCompound(NBT_PLAYERS);
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.getEntityWorld().isRemote && player.isSneaking()) {
            ModuleHelper.setOwner(stack, player);
            player.sendStatusMessage(new TextComponentTranslation("itemText.security.owner", player.getDisplayName().toString()), false);
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer)entity;
            String id = targetPlayer.getUniqueID().toString();
            String name = targetPlayer.getDisplayName().toString();
            Result res = player.isSneaking() ? removePlayer(stack, id) : addPlayer(stack, id, name);
            if (player.world.isRemote) {
                player.playSound(res.isError() ? SOUND_ERROR : SOUND_SUCCESS, 1.0f, 1.0f);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("chatText.security." + res.toString(), name), false);
            }
            return true;
        }
        return false;
    }

    enum Result {
        ADDED, REMOVED, FULL, ALREADY_ADDED, ERROR, NOT_PRESENT;

        boolean isError() {
            return this != ADDED && this != REMOVED;
        }
    }

}
