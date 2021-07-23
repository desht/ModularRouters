package me.desht.modularrouters.util.fake_player;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public class RouterFakePlayer extends FakePlayer {
    private final ModularRouterBlockEntity router;
    private ItemStack prevHeldStack = ItemStack.EMPTY;

    public RouterFakePlayer(ModularRouterBlockEntity router, ServerLevel world, GameProfile profile) {
        super(world, profile);
        this.router = router;
    }

    @Override
    public Vec3 position() {
        return new Vec3(getX(), getY(), getZ());
    }

    @Override
    protected void equipEventAndSound(ItemStack stack) {
        // silence annoying sound effects when fake player equips the buffer item
    }

    @Override
    public double getEyeY() {
        return getY();
    }

    @Override
    public void tick() {
        attackStrengthTicker++;
        if (router.caresAboutItemAttributes() && !ItemStack.matches(prevHeldStack, getMainHandItem())) {
            getAttributes().removeAttributeModifiers(prevHeldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            getAttributes().addTransientAttributeModifiers(getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND));
            prevHeldStack = getMainHandItem().copy();
        }
    }
}
