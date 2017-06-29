package me.desht.modularrouters.item.module;

import com.google.common.base.Joiner;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

@Mod.EventBusSubscriber
public class ItemModule extends ItemSubTypes<ItemModule.ModuleType> {

    // add new types at the end!
    public enum ModuleType {
        BREAKER,
        DROPPER,
        PLACER,
        PULLER,
        SENDER1,
        SENDER2,
        SENDER3,
        VACUUM,
        VOID,
        DETECTOR,
        FLINGER,
        PLAYER,
        EXTRUDER,
        FLUID,
        PULLER2,
        EXTRUDER2;

        public static ModuleType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemModule ? values()[stack.getItemDamage()] : null;
        }
    }

    public ItemModule() {
        super("module", ModuleType.class);

        register(ModuleType.BREAKER, new BreakerModule());
        register(ModuleType.DROPPER, new DropperModule());
        register(ModuleType.PLACER, new PlacerModule());
        register(ModuleType.PULLER, new PullerModule());
        register(ModuleType.SENDER1, new SenderModule1());
        register(ModuleType.SENDER2, new SenderModule2());
        register(ModuleType.SENDER3, new SenderModule3());
        register(ModuleType.VACUUM, new VacuumModule());
        register(ModuleType.VOID, new VoidModule());
        register(ModuleType.DETECTOR, new DetectorModule());
        register(ModuleType.FLINGER, new FlingerModule());
        register(ModuleType.PLAYER, new PlayerModule());
        register(ModuleType.EXTRUDER, new ExtruderModule());
        register(ModuleType.FLUID, new FluidModule());
        register(ModuleType.PULLER2, new PullerModule2());
        register(ModuleType.EXTRUDER2, new ExtruderModule2());
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == Side.CLIENT) {
            Module mod = getModule(event.getItemStack());
            if (!(mod instanceof TargetedModule)) {
                return;
            }
            if (InventoryUtils.getInventory(event.getWorld(), event.getPos(), event.getFace()) != null) {
                return;
            }
            // We're right-clicking an ordinary block; canceling this prevents the onArmSwing() method
            // being called, and allows the GUI to be opened normally.
            event.setCanceled(true);
        }
    }

    public static Module getModule(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule) || stack.getMetadata() >= ModuleType.values().length) {
            return null;
        }
        return getModule(ModuleType.values()[stack.getMetadata()]);
    }

    public static Module getModule(ModuleType type) {
        return (Module) RegistrarMR.MODULE.getHandler(type);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ModuleHelper.validateNBT(stack);
        if (!player.isSneaking()) {
            if (!world.isRemote) {
                int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_MODULE_HELD_MAIN : ModularRouters.GUI_MODULE_HELD_OFF;
                player.openGui(ModularRouters.instance, guiId, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            }
        } else {
            return getModule(stack).onSneakRightClick(stack, world, player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return getModule(stack).onEntitySwing(entityLiving, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        ItemStack stack = player.getHeldItem(hand);
        return getModule(stack).onItemUse(stack, player, world, pos, hand, face, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        Module module = getModule(itemstack);
        if (module == null) {
            return;
        }
        module.addBasicInformation(itemstack, player, list, advanced);

        if (GuiScreen.isCtrlKeyDown()) {
            module.addUsageInformation(itemstack, player, list, advanced);
        } else if (ConfigHandler.misc.alwaysShowSettings || GuiScreen.isShiftKeyDown()) {
            module.addExtraInformation(itemstack, player, list, advanced);
            list.add(I18n.format("itemText.misc.holdCtrl"));
        } else if (!ConfigHandler.misc.alwaysShowSettings){
            list.add(I18n.format("itemText.misc.holdShiftCtrl"));
        }
    }
}
