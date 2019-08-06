package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.PlayerModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class CompiledPlayerModule extends CompiledModule {
    public static final String NBT_OPERATION = "Operation";
    public static final String NBT_SECTION = "Section";

    public enum Operation {
        EXTRACT, INSERT;

        public String getSymbol() { return this == INSERT ? "⟹" : "⟸"; }
    }

    public enum Section {
        MAIN, ARMOR, OFFHAND, ENDER
    }

    private final Operation operation;
    private final Section section;
    private final UUID playerId;
    private final String playerName;
    private WeakReference<PlayerEntity> playerRef;

    public CompiledPlayerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        CompoundNBT compound = stack.getTag();
        if (compound != null) {
            playerName = ((PlayerModule) stack.getItem()).getOwnerName(stack);
            playerId = ((PlayerModule) stack.getItem()).getOwnerID(stack);
            operation = Operation.values()[compound.getInt(NBT_OPERATION)];
            section = Section.values()[compound.getInt(NBT_SECTION)];
            if (router != null && !router.getWorld().isRemote) {
                PlayerEntity player = playerId == null ? null : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId);
                playerRef = new WeakReference<>(player);
            } else {
                playerRef = new WeakReference<>(null);
            }
        } else {
            operation = Operation.EXTRACT;
            section = Section.MAIN;
            playerId = null;
            playerName = null;
        }
    }

    @Override
    public boolean hasTarget() {
        return getPlayer() != null;
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        PlayerEntity player = getPlayer();  // will be non-null if we get here
        IItemHandler itemHandler = getHandler(player);
        if (itemHandler == null) {
            return false;
        }
        ItemStack bufferStack = router.getBufferItemStack();
        switch (operation) {
            case EXTRACT:
                if (bufferStack.getCount() < bufferStack.getMaxStackSize()) {
                    ItemStack taken = transferToRouter(itemHandler, router);
                    return !taken.isEmpty();
                }
                break;
            case INSERT:
                if (getFilter().test(bufferStack)) {
                    if (getSection() == CompiledPlayerModule.Section.ARMOR) {
                        return insertArmor(router, itemHandler, bufferStack);
                    } else {
                        int nToSend = getItemsPerTick(router);
                        if (getRegulationAmount() > 0) {
                            int existing = InventoryUtils.countItems(bufferStack, itemHandler, getRegulationAmount(), !getFilter().getFlags().isIgnoreDamage());
                            nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                            if (nToSend <= 0) {
                                return false;
                            }
                        }
                        int sent = InventoryUtils.transferItems(router.getBuffer(), itemHandler, 0, nToSend);
                        return sent > 0;
                    }
                }
                break;
            default: return false;
        }
        return false;
    }

    private PlayerEntity getPlayer() {
        return playerRef.get();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer().getUniqueID().equals(playerId)) {
            playerRef = new WeakReference<>(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().getUniqueID().equals(playerId)) {
            playerRef = new WeakReference<>(null);
        }
    }

    @Override
    public void onCompiled(TileEntityItemRouter router) {
        super.onCompiled(router);
        if (!router.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @Override
    public void cleanup(TileEntityItemRouter router) {
        super.cleanup(router);
        if (!router.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public Operation getOperation() {
        return operation;
    }

    public Section getSection() {
        return section;
    }

    private boolean insertArmor(TileEntityItemRouter router, IItemHandler itemHandler, ItemStack armorStack) {
        int slot = getSlotForArmorItem(armorStack);
        if (slot >= 0 && itemHandler.getStackInSlot(slot).isEmpty()) {
            ItemStack extracted = router.getBuffer().extractItem(0, 1, false);
            if (extracted.isEmpty()) {
                return false;
            }
            ItemStack res = itemHandler.insertItem(slot, extracted, false);
            return res.isEmpty();
        } else {
            return false;
        }
    }

    private int getSlotForArmorItem(ItemStack stack) {
        switch (MobEntity.getSlotForItemStack(stack)) {
            case HEAD: return 3;
            case CHEST: return 2;
            case LEGS: return 1;
            case FEET: return 0;
            default: return -1;
        }
    }

    private IItemHandler getHandler(PlayerEntity player) {
        switch (section) {
            case MAIN: return new PlayerMainInvWrapper(player.inventory);
            case ARMOR: return new PlayerArmorInvWrapper(player.inventory);
            case OFFHAND: return new PlayerOffhandInvWrapper(player.inventory);
            case ENDER: return new InvWrapper(player.getInventoryEnderChest());
            default: return null;
        }
    }
}
