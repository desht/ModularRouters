package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class GuiModulePlayer extends GuiModule {
    private static final int OP_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int SECT_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;

    private CompiledPlayerModule.Operation operation;
    private CompiledPlayerModule.Section section;

    public GuiModulePlayer(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModulePlayer(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, moduleItemStack);
        operation = cpm.getOperation();
        section = cpm.getSection();
    }

    @Override
    public void initGui() {
        super.initGui();

        String label = I18n.format("guiText.label.playerOp." + operation);
        buttonList.add(new GuiButton(OP_BUTTON_ID, guiLeft + 130, guiTop + 22, 50, 20, label));

        label = I18n.format("guiText.label.playerSect." + section);
        buttonList.add(new GuiButton(SECT_BUTTON_ID, guiLeft + 130, guiTop + 44, 50, 20, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case OP_BUTTON_ID:
                operation = operation.toggle();
                button.displayString = I18n.format("guiText.label.playerOp." + operation);
                sendModuleSettingsToServer();
                break;
            case SECT_BUTTON_ID:
                section = section.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
                button.displayString = I18n.format("guiText.label.playerSect." + section);
                sendModuleSettingsToServer();
                break;
            default:
                super.actionPerformed(button);
                break;
        }
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setInteger(CompiledPlayerModule.NBT_OPERATION, operation.ordinal());
        compound.setInteger(CompiledPlayerModule.NBT_SECTION, section.ordinal());
        return compound;
    }
}
