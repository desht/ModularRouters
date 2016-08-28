package me.desht.modularrouters.item.upgrade;

import com.google.common.collect.Sets;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SecurityUpgrade extends Upgrade {
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.security.tooltip");
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        list.add(I18n.format("itemText.security.owner", TextFormatting.YELLOW + getOwnerName(itemstack)));
        Set<String> names = getPlayerNames(itemstack);
        if (!names.isEmpty()) {
            list.add(I18n.format("itemText.security.count", names.size(), getMaxPlayers()));
            list.addAll(names.stream().map(name -> " \u2022 " + TextFormatting.YELLOW + name).sorted().collect(Collectors.toList()));
        }
    }

    @Override
    boolean hasExtraInformation() {
        return true;
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        Set<UUID> ids = getPlayerIDs(stack);
        router.addPermittedIds(ids);
    }

    public Set<UUID> getPlayerIDs(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return Collections.emptySet();
        }

        Set<UUID> res = Sets.newHashSet();
        NBTTagList o = compound.getTagList("Owner", Constants.NBT.TAG_STRING);
        res.add(UUID.fromString(o.getStringTagAt(1)));

        if (compound.hasKey("Players")) {
            NBTTagCompound p = compound.getCompoundTag("Players");
            res.addAll(p.getKeySet().stream().map(UUID::fromString).collect(Collectors.toList()));
        }
        return res;
    }

    /**
     * Get this security upgrade's owner.
     *
     * @param stack the upgrade itemstack
     * @return (displayable) owner name
     */
    public String getOwnerName(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return "???"; // shouldn't ever happen
        }
        NBTTagList o = compound.getTagList("Owner", Constants.NBT.TAG_STRING);
        return o.getStringTagAt(0);
    }

    /**
     * Get a list of player names added to this security upgrade, not including the owner.
     *
     * @param stack the upgrade itemstack
     * @return set of (displayable) player names
     */
    public Set<String> getPlayerNames(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey("Players")) {
            NBTTagCompound p = compound.getCompoundTag("Players");
            return Sets.newHashSet(p.getKeySet().stream().map(p::getString).sorted().collect(Collectors.toList()));
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Maximum number of players that can be added to one upgrade.
     * @return max players
     */
    public int getMaxPlayers() {
        return 6;
    }

    public Interacted.Result addPlayer(ItemStack stack, String id, String name) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            if (!compound.hasKey("Players")) {
                compound.setTag("Players", new NBTTagCompound());
            }
            NBTTagCompound p = compound.getCompoundTag("Players");
            if (p.hasKey(id)) {
                return Interacted.Result.ALREADY_ADDED;  // already there, do nothing
            }
            if (p.getSize() >= getMaxPlayers()) {
                return Interacted.Result.FULL;  // list full
            }
            p.setString(id, name);
            return Interacted.Result.ADDED;
        }
        return Interacted.Result.ERROR;
    }

    private Interacted.Result removePlayer(ItemStack stack, String id) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            if (!compound.hasKey("Players")) {
                compound.setTag("Players", new NBTTagCompound());
            }
            NBTTagCompound p = compound.getCompoundTag("Players");
            if (p.hasKey(id)) {
                p.removeTag(id);
                return Interacted.Result.REMOVED;
            } else {
                return Interacted.Result.NOT_PRESENT;
            }
        }
        return Interacted.Result.ERROR;
    }

    public static class Interacted {
        enum Result {
            ADDED, REMOVED, FULL, ALREADY_ADDED, ERROR, NOT_PRESENT;

            boolean isError() {
                return this != ADDED && this != REMOVED;
            }
        }

        @SubscribeEvent
        public static void onInteracted(PlayerInteractEvent.EntityInteract event) {
            if (event.getTarget() instanceof EntityPlayer) {
                ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
                if (ItemUpgrade.isType(stack, ItemUpgrade.UpgradeType.SECURITY)) {
                    SecurityUpgrade su = (SecurityUpgrade) ItemUpgrade.getUpgrade(stack);
                    EntityPlayer targetPlayer = (EntityPlayer) event.getTarget();
                    String id = targetPlayer.getUniqueID().toString();
                    String name = targetPlayer.getDisplayNameString();
                    Result res = event.getEntityPlayer().isSneaking() ? su.removePlayer(stack, id) : su.addPlayer(stack, id, name);
                    if (event.getWorld().isRemote) {
                        event.getEntityPlayer().playSound(res.isError() ? SoundEvents.BLOCK_NOTE_BASS : SoundEvents.BLOCK_NOTE_PLING, 1.0f, 1.0f);
                    } else {
                        event.getEntityPlayer().addChatMessage(new TextComponentTranslation("chatText.security." + res.toString(), name));
                    }
                }
            }
        }
    }

}
