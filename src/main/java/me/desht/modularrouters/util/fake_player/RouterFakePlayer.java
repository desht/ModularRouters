package me.desht.modularrouters.util.fake_player;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class RouterFakePlayer extends FakePlayer {
    public RouterFakePlayer(ServerWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public Vector3d position() {
        return new Vector3d(getX(), getY(), getZ());
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
        // silence annoying sound effects when fake player equips the buffer item
    }

    @Override
    public double getEyeY() {
        return getY();
    }
}
