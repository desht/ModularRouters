package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.GuiModule;
import me.desht.modularrouters.gui.GuiModuleFlinger;
import me.desht.modularrouters.logic.CompiledFlingerModule;
import me.desht.modularrouters.logic.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class FlingerModule extends DropperModule {
    @Override
    protected void setupItemVelocity(TileEntityItemRouter router, EntityItem item, CompiledModule settings) {
        CompiledFlingerModule fs = (CompiledFlingerModule) settings;

        EnumFacing facing = router.getAbsoluteFacing(RelativeDirection.FRONT);
        float basePitch = 0.0f;
        float baseYaw;
        switch (settings.getDirection()) {
            case UP:
                basePitch = 90.0f;
                baseYaw = yawFromFacing(facing);
                break;
            case DOWN:
                basePitch = -90.0f;
                baseYaw = yawFromFacing(facing);
                break;
            default:
                baseYaw = yawFromFacing(router.getAbsoluteFacing(settings.getDirection()));
                break;
        }

        double yawRad = Math.toRadians(baseYaw + fs.getYaw()), pitchRad = Math.toRadians(basePitch + fs.getPitch());

        double x = (Math.cos(yawRad) * Math.cos(pitchRad));   // east is positive X
        double y = Math.sin(pitchRad);
        double z = -(Math.sin(yawRad) * Math.cos(pitchRad));  // north is negative Z

        float speed = fs.getSpeed();
        item.motionX = x * speed;
        item.motionY = y * speed;
        item.motionZ = z* speed;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(itemstack, player, list, par4);
        CompiledFlingerModule fs = new CompiledFlingerModule(null, itemstack);
        list.add(I18n.format("itemText.misc.flingerDetails", fs.getSpeed(), fs.getPitch(), fs.getYaw()));
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledFlingerModule(tileEntityItemRouter, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleFlinger.class;
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
