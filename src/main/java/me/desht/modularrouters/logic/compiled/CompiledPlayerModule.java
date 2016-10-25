package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class CompiledPlayerModule extends CompiledModule {
    public static final String NBT_OPERATION = "Operation";
    public static final String NBT_SECTION = "Section";

    public enum Operation {
        EXTRACT, INSERT;
        public Operation toggle() {
            return this == INSERT ? EXTRACT : INSERT;
        }
        public String getSymbol() { return this == INSERT ? "⟹" : "⟸"; }
    }

    public enum Section {
        MAIN, ARMOR, OFFHAND, ENDER;
        public Section cycle(int dir) {
            int n = ordinal() + dir;
            if (n >= values().length) {
                n = 0;
            } else if (n < 0) {
                n = values().length - 1;
            }
            return values()[n];
        }
    }

    private final Operation operation;
    private final Section section;
    private final UUID playerId;
    private final String playerName;
    private WeakReference<EntityPlayer> playerRef;

    public CompiledPlayerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            Pair<String,UUID> owner = ItemModule.getOwnerNameAndId(stack);
            playerName = owner.getLeft();
            playerId = owner.getRight();
            operation = Operation.values()[compound.getInteger(NBT_OPERATION)];
            section = Section.values()[compound.getInteger(NBT_SECTION)];
            if (router != null && !router.getWorld().isRemote) {
                EntityPlayer player = playerId == null ? null : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);
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
    public boolean execute(TileEntityItemRouter router) {
        EntityPlayer player = getPlayer();
        if (player == null) {
            return false;
        }
        IItemHandler itemHandler = getHandler(player);
        if (itemHandler == null) {
            return false;
        }
        ItemStack bufferStack = router.getBufferItemStack();
        switch (operation) {
            case EXTRACT:
                if (bufferStack == null || bufferStack.stackSize < bufferStack.getMaxStackSize()) {
                    int taken = transferItems(itemHandler, router);
                    return taken > 0;
                }
                break;
            case INSERT:
                if (bufferStack != null && getFilter().pass(bufferStack)) {
                    if (getSection() == CompiledPlayerModule.Section.ARMOR) {
                        return insertArmor(router, itemHandler, bufferStack);
                    } else {
                        int sent = InventoryUtils.transferItems(router.getBuffer(), itemHandler, 0, router.getItemsPerTick());
                        return sent > 0;
                    }
                }
                break;
            default: return false;
        }
        return false;
    }

    public EntityPlayer getPlayer() {
        return playerRef.get();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.getUniqueID().equals(playerId)) {
            playerRef = new WeakReference<>(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.getUniqueID().equals(playerId)) {
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
        if (slot >= 0 && itemHandler.getStackInSlot(slot) == null) {
            ItemStack extracted = router.getBuffer().extractItem(0, 1, false);
            if (extracted == null) {
                return false;
            }
            ItemStack res = itemHandler.insertItem(slot, extracted, false);
            return res == null;
        } else {
            return false;
        }
    }

    private int getSlotForArmorItem(ItemStack stack) {
        switch (EntityLiving.getSlotForItemStack(stack)) {
            case HEAD: return 3;
            case CHEST: return 2;
            case LEGS: return 1;
            case FEET: return 0;
            default: return -1;
        }
    }

    private IItemHandler getHandler(EntityPlayer player) {
        switch (section) {
            case MAIN: return new PlayerMainInvWrapper(player.inventory);
            case ARMOR: return new PlayerArmorInvWrapper(player.inventory);
            case OFFHAND: return new PlayerOffhandInvWrapper(player.inventory);
            case ENDER: return new InvWrapper(player.getInventoryEnderChest());
            default: return null;
        }
    }
}
