package me.desht.modularrouters.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.FilterHandler;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.item.module.AbstractModule.FilterSettings;
import me.desht.modularrouters.item.module.AbstractModule.RelativeDirection;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiModule extends GuiContainerBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/module.png");
    private static final int DIRECTION_BUTTON_ID = FilterSettings.values().length + 1;
    public static final int GUI_HEIGHT = 165;
    public static final int GUI_WIDTH = 176;

//    private final FilterHandler filterHandler;
    private final ItemStack moduleItemStack;
    private ModuleToggleButton[] buttons = new ModuleToggleButton[FilterSettings.values().length];
    private RelativeDirection facing;

    public GuiModule(ModuleContainer containerItem) {
        super(containerItem);
//        filterHandler = containerItem.filterHandler;
        moduleItemStack = containerItem.filterHandler.getModuleItemStack();
        facing = AbstractModule.getDirectionFromNBT(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        addButton(FilterSettings.BLACKLIST, this.guiLeft + 64, this.guiTop + 16);
        addButton(FilterSettings.IGNORE_META, this.guiLeft + 82, this.guiTop + 16);
        addButton(FilterSettings.IGNORE_NBT, this.guiLeft + 64, this.guiTop + 34);
        addButton(FilterSettings.IGNORE_OREDICT, this.guiLeft + 82, this.guiTop + 34);
        addButton(FilterSettings.TERMINATE, this.guiLeft + 64, this.guiTop + 52);

        String label = I18n.format("guiText.label." + facing.name());
        this.buttonList.add(new GuiButton(DIRECTION_BUTTON_ID, this.guiLeft + 114, this.guiTop + 40, 50, 20, label));
    }

    private void addButton(FilterSettings setting, int x, int y) {
        int id = setting.ordinal();
        buttons[id] = new ModuleToggleButton(id, x, y);
        buttons[id].setToggled(AbstractModule.checkFlag(moduleItemStack, setting));
        this.buttonList.add(buttons[id]);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 0 && button.id < FilterSettings.values().length) {
            buttons[button.id].toggle();
        } else if (button.id == DIRECTION_BUTTON_ID) {
            int n = facing.ordinal() + (isShiftKeyDown() ? -1 : 1);
            if (n < 0) {
                n = RelativeDirection.values().length - 1;
            } else if (n >= RelativeDirection.values().length) {
                n = 0;
            }
            facing = RelativeDirection.values()[n];
            button.displayString = I18n.format("guiText.label." + facing.name());
        }
        byte flags = 0;
        for (FilterSettings setting : FilterSettings.values()) {
            if (buttons[setting.ordinal()].isToggled()) {
                flags |= setting.getMask();
            }
        }
        flags |= facing.ordinal() << 4;
        CommonProxy.network.sendToServer(new ModuleSettingsMessage(flags));
    }

    @Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
//		String title = this.filterHandler.hasCustomName() ? this.filterHandler.getName() : I18n.format(this.filterHandler.getName());
        String title = moduleItemStack.getDisplayName();
		this.fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("guiText.label.direction"), 114, 30, 0x404040);
		this.fontRendererObj.drawString(I18n.format("container.filterHandler"), 8, this.ySize - 96 + 4, 0x404040);
	}

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(textureLocation);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

}
