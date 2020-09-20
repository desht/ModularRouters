package me.desht.modularrouters.integration;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public class XPCollection {
    private static final boolean[] AVAILABLE = new boolean[XPCollectionType.values().length];
    private static final ItemStack[] ICONS = new ItemStack[XPCollectionType.values().length];

    public static void detectXPTypes() {
        Arrays.fill(ICONS, null);  // null is OK here; it means "not queried yet"

        for (XPCollectionType type : XPCollectionType.values()) {
            AVAILABLE[type.ordinal()] = !getIconForResource(type).isEmpty();
        }
    }

    private static ItemStack getIconForResource(XPCollectionType type) {
        if (ICONS[type.ordinal()] == null) {
            if (type.isSolid()) {
                ICONS[type.ordinal()] = new ItemStack(ForgeRegistries.ITEMS.getValue(type.getRegistryName()));
            } else {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(type.getRegistryName());
                ICONS[type.ordinal()] = fluid == null || fluid == Fluids.EMPTY ?
                        ItemStack.EMPTY :
                        FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
            }
        }
        return ICONS[type.ordinal()];
    }

    public enum XPCollectionType {
        // note: bottles o' enchanting are randomly worth 3-11 experience, so let's use an average of 7
        SOLIDIFIED_EXPERIENCE(true, 8, "actuallyadditions:item_solidified_experience"),
        BOTTLE_O_ENCHANTING(true, 7, "minecraft:experience_bottle"),
        MEMORY_ESSENCE(false, 20, "pneumaticcraft:memory_essence"),
        CYCLIC_XP_JUICE(false, 20, "cyclic:xpjuice"),
        IF_ESSENCE(false, 20, "industrialforegoing:essence");

        // TODO 1.15 other mod exp levels... should be in config or data pack, really
//        XPJUICE(20, "xpjuice"),  // Enderio/Openblocks/Cyclic/Reliquary
//        KNOWLEDGE(20, "experience"), // CoFH Essence of Knowledge

        private final boolean solid;
        private final int xpRatio;  // XP points per item, or (for fluids) mB per XP
        private final ResourceLocation registryName;

        XPCollectionType(boolean solid, int xpRatio, String registryName) {
            this.solid = solid;
            this.xpRatio = xpRatio;
            this.registryName = new ResourceLocation(registryName);
        }

        public int getXpRatio() {
            return xpRatio;
        }

        public ResourceLocation getRegistryName() {
            return registryName;
        }

        public boolean isSolid() {
            return solid;
        }

        public Fluid getFluid() {
            return ForgeRegistries.FLUIDS.getValue(registryName);
        }

        public String getModId() {
            return registryName.getNamespace();
        }

        public boolean isAvailable() {
            return AVAILABLE[this.ordinal()];
        }

        public ItemStack getIcon() {
            return getIconForResource(this);
        }

        public ITextComponent getDisplayName() {
            return getIconForResource(this).getDisplayName();
        }
    }
}
