package me.desht.modularrouters.event;

import me.desht.modularrouters.recipe.GuideBookRecipe;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.Scheduler;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Scheduler.client().tick();
        }
    }

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        if (event.getItemStack().getItem().getRegistryName().getNamespace().equals("patchouli")) {
            if (event.getItemStack().hasTag()) {
                if (event.getItemStack().getTag().getString(GuideBookRecipe.NBT_KEY).equals(GuideBookRecipe.NBT_VAL)) {
                    MiscUtil.appendMultilineText(event.getToolTip(), TextFormatting.GRAY, "jei.tooltip.guidebook");
                }
            }
        }
    }
}