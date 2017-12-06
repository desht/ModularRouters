package me.desht.modularrouters.util;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.UUID;

@Mod.EventBusSubscriber
public class FakePlayerManager {
    private static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes(ModularRouters.MODID.getBytes()), "[" + ModularRouters.MODNAME + "]");
    private static WeakReference<RouterFakePlayer> theFakePlayer = new WeakReference<>(null);

    /**
     * Get the fake player instance, putting it at the given position & world.
     *
     * @param world the world
     * @param pos position in the world
     * @return the fake player, or null; be prepared to deal with a null return value
     */
    public static RouterFakePlayer getFakePlayer(WorldServer world, BlockPos pos) {
        RouterFakePlayer fakePlayer = theFakePlayer.get();
        if (fakePlayer == null) {
            fakePlayer = new RouterFakePlayer(world, gameProfile);
            fakePlayer.connection = new NetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.SERVERBOUND), fakePlayer);
            theFakePlayer = new WeakReference<>(fakePlayer);
        }
        fakePlayer.world = world;
        fakePlayer.posX = pos.getX();
        fakePlayer.posY = pos.getY();
        fakePlayer.posZ = pos.getZ();

        return fakePlayer;
    }

    public static class RouterFakePlayer extends FakePlayer {
        RouterFakePlayer(WorldServer world, GameProfile name) {
            super(world, name);
        }

        @Override
        protected void playEquipSound(ItemStack stack) {
            // silence annoying sound effects when fake player equips the buffer item
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && theFakePlayer.get() != null) {
            theFakePlayer.get().getCooldownTracker().tick();
        }
    }
}
