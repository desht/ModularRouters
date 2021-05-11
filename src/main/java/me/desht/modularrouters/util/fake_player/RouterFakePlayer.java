package me.desht.modularrouters.util.fake_player;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class RouterFakePlayer extends FakePlayer {
    private final TileEntityItemRouter router;
    private ItemStack prevHeldStack = ItemStack.EMPTY;

    public RouterFakePlayer(TileEntityItemRouter router, ServerWorld world, GameProfile profile) {
        super(world, profile);
        this.router = router;
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

    @Override
    public void tick() {
        attackStrengthTicker++;
        if (router.caresAboutItemAttributes() && !ItemStack.matches(prevHeldStack, getMainHandItem())) {
            getAttributes().removeAttributeModifiers(prevHeldStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            getAttributes().addTransientAttributeModifiers(getMainHandItem().getAttributeModifiers(EquipmentSlotType.MAINHAND));
            prevHeldStack = getMainHandItem().copy();
        }
    }
}
