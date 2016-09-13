package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

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
            NBTTagList o = compound.getTagList("Owner", Constants.NBT.TAG_STRING);
            playerName = o.getStringTagAt(0);
            String s = o.getStringTagAt(1);
            playerId = s.isEmpty() ? null : UUID.fromString(s);
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
        if (!router.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @Override
    public void cleanup(TileEntityItemRouter router) {
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
}
