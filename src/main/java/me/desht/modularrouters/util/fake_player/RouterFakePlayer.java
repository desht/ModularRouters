package me.desht.modularrouters.util.fake_player;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class RouterFakePlayer extends FakePlayer {
    public RouterFakePlayer(ServerWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
        // silence annoying sound effects when fake player equips the buffer item
    }

    @Override
    public double getPosYEye() {
        return getPosY();
    }
}
