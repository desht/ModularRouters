package me.desht.modularrouters.util;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class FakePlayer {
    private static final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes(ModularRouters.modId.getBytes()), "[" + ModularRouters.name + "]");
    private static WeakReference<EntityPlayer> theFakePlayer = new WeakReference<>(null);

    private static WeakReference<EntityPlayer> createNewPlayer(WorldServer world) {
        EntityPlayer player = FakePlayerFactory.get(world, gameProfile);
        return new WeakReference<>(player);
    }

    private static WeakReference<EntityPlayer> createNewPlayer(WorldServer world, BlockPos pos) {
        EntityPlayer player = FakePlayerFactory.get(world, gameProfile);
        player.posX = pos.getX();
        player.posY = pos.getY();
        player.posZ = pos.getZ();
        return new WeakReference<>(player);
    }

    public static WeakReference<EntityPlayer> getFakePlayer(WorldServer world, BlockPos pos) {
        if (theFakePlayer.get() == null) {
            theFakePlayer = createNewPlayer(world, pos);
        } else {
            theFakePlayer.get().worldObj = world;
            theFakePlayer.get().posX = pos.getX();
            theFakePlayer.get().posY = pos.getY();
            theFakePlayer.get().posZ = pos.getZ();
        }

        return theFakePlayer;
    }

}
