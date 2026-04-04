package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class EnergyDistributorModuleScreen extends ModuleScreen {
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private DistributorModuleScreen.DirectionButton db;

    public EnergyDistributorModuleScreen(ModuleMenu container, Inventory inv, Component displayText) {
        super(container, inv, displayText);
    }

    @Override
    public void init() {
        super.init();

        CompiledEnergyDistributorModule cdm = new CompiledEnergyDistributorModule(null, moduleItemStack);

        addRenderableWidget(db = new DistributorModuleScreen.DirectionButton(leftPos + 147, topPos + 43, cdm.isPulling(), this));

        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 41, leftPos + 165, topPos + 61,
                xlate("modularrouters.guiText.popup.distributor.direction").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.item(ROUTER_STACK, leftPos + 127, topPos + 43);
    }

    @Override
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        super.buildComponentPatch(builder);
        builder.set(ModDataComponents.DISTRIBUTOR_SETTINGS.get(), new CompiledDistributorModule.DistributorSettings(
                        DistributionStrategy.ROUND_ROBIN,
                        db.isToggled() ? TransferDirection.TO_ROUTER : TransferDirection.FROM_ROUTER
                )
        );
    }
}
