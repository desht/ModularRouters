package me.desht.modularrouters.integration;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
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
    private static final boolean[] SOLID = new boolean[XPCollectionType.values().length];

    public static void detectXPTypes() {
        for (XPCollectionType type : XPCollectionType.values()) {
            AVAILABLE[type.ordinal()] = !getIconForResource(type.registryName).isEmpty();
            SOLID[type.ordinal()] = ForgeRegistries.ITEMS.containsKey(type.registryName);
        }
        Arrays.fill(ICONS, null);  // null is OK here; it means "not queried yet"
    }

    private static ItemStack getIconForResource(ResourceLocation resource) {
        Item item = ForgeRegistries.ITEMS.getValue(resource);
        if (item != null) {
            return new ItemStack(item);
        } else {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resource);
            return fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
        }
    }

    // note: bottles o' enchanting are worth 3-11 experience, with an average of 7
    public enum XPCollectionType {
        BOTTLE_O_ENCHANTING(7, "minecraft:experience_bottle"),  // vanilla bottles o' enchanting
        SOLIDIFIED_EXPERIENCE(8, "actuallyadditions:item_solidified_experience");  // AA solidified experience

        // TODO 1.14 other mod exp levels... should be in config or data pack, really
//        XPJUICE(20, "xpjuice"),  // Enderio/Openblocks/Cyclic/Reliquary
//        KNOWLEDGE(20, "experience"), // CoFH Essence of Knowledge
//        ESSENCE(20, "essence"); // Industrial Foregoing essence - TODO: respect IF config "essenceMultiplier"

        private final int xpRatio;  // XP points per item or mB of fluid
        private final ResourceLocation registryName;

        XPCollectionType(int xpRatio, String registryName) {
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
            return SOLID[this.ordinal()];
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
            if (ICONS[ordinal()] == null) {
                ICONS[ordinal()] = getIconForResource(registryName);
            }
            return ICONS[ordinal()];
        }

        public ITextComponent getDisplayName() {
            return getIconForResource(registryName).getDisplayName();
        }
    }
}
