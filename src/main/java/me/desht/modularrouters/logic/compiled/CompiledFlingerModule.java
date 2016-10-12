package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class CompiledFlingerModule extends CompiledDropperModule {
    public static final String NBT_SPEED = "Speed";
    public static final String NBT_PITCH = "Pitch";
    public static final String NBT_YAW = "Yaw";

    private final float speed, pitch, yaw;

    public CompiledFlingerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = Module.validateNBT(stack);
        for (String key : new String[] { NBT_SPEED, NBT_PITCH, NBT_YAW }) {
            if (!compound.hasKey(key)) {
                compound.setFloat(key, 0.0f);
            }
        }

        speed = compound.getFloat(NBT_SPEED);
        pitch = compound.getFloat(NBT_PITCH);
        yaw = compound.getFloat(NBT_YAW);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    protected void setupItemVelocity(TileEntityItemRouter router, EntityItem item) {
        EnumFacing routerFacing = router.getAbsoluteFacing(Module.RelativeDirection.FRONT);
        float basePitch = 0.0f;
        float baseYaw;
        switch (getDirection()) {
            case UP:
                basePitch = 90.0f;
                baseYaw = yawFromFacing(routerFacing);
                break;
            case DOWN:
                basePitch = -90.0f;
                baseYaw = yawFromFacing(routerFacing);
                break;
            default:
                baseYaw = yawFromFacing(getFacing());
                break;
        }

        double yawRad = Math.toRadians(baseYaw + yaw), pitchRad = Math.toRadians(basePitch + pitch);

        double x = (Math.cos(yawRad) * Math.cos(pitchRad));   // east is positive X
        double y = Math.sin(pitchRad);
        double z = -(Math.sin(yawRad) * Math.cos(pitchRad));  // north is negative Z

        item.motionX = x * speed;
        item.motionY = y * speed;
        item.motionZ = z* speed;
    }

    private float yawFromFacing(EnumFacing absoluteFacing) {
        switch (absoluteFacing) {
            case EAST: return 0.0f;
            case NORTH: return 90.0f;
            case WEST: return 180.0f;
            case SOUTH: return 270.0f;
        }
        return 0;
    }
}
