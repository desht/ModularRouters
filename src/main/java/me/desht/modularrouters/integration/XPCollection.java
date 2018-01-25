package me.desht.modularrouters.integration;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;

public class XPCollection {
    private static final boolean[] AVAILABLE = new boolean[XPCollectionType.values().length];
    private static final ItemStack[] ICONS = new ItemStack[XPCollectionType.values().length];
    private static final boolean[] SOLID = new boolean[XPCollectionType.values().length];

    public static void detectXPFluids() {
        Arrays.stream(XPCollectionType.values())
                .forEach(type -> AVAILABLE[type.ordinal()] = !getIconForResource(type.registryName).isEmpty());
        for (XPCollectionType type : XPCollectionType.values()) {
            AVAILABLE[type.ordinal()] = !getIconForResource(type.registryName).isEmpty();
            SOLID[type.ordinal()] = type.registryName.indexOf(':') >= 0;
        }
        Arrays.fill(ICONS, null);
    }

    private static ItemStack getIconForResource(String resource) {
        ItemStack stack;
        if (resource.contains(":")) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resource));
            stack = item == null ? ItemStack.EMPTY : new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(resource)));
        } else {
            Fluid fluid = FluidRegistry.getFluid(resource);
            stack = fluid == null ? ItemStack.EMPTY : FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
        }
        return stack;
    }

    public enum XPCollectionType {
        BOTTLE_O_ENCHANTING(7, "minecraft:experience_bottle"),  // vanilla bottles o' enchanting
        SOLIDIFIED_EXPERIENCE(8, "actuallyadditions:item_solidified_experience"),  // AA solidified experience
        XPJUICE(20, "xpjuice"),  // Enderio/Openblocks/Cyclic/Reliquary
        KNOWLEDGE(20, "experience"), // CoFH Essence of Knowledge
        ESSENCE(20, "essence"); // Industrial Foregoing essence - TODO: respect IF config "essenceMultiplier"

        private final int xpRatio;  // mB per XP
        private final String registryName;

        XPCollectionType(int xpRatio, String registryName) {
            this.xpRatio = xpRatio;
            this.registryName = registryName;
        }

        public int getXpRatio() {
            return xpRatio;
        }

        public String getRegistryName() {
            return registryName;
        }

        public boolean isSolid() {
            return SOLID[this.ordinal()];
        }

        public Fluid getFluid() {
            return FluidRegistry.getFluid(registryName);
        }

        public String getModId() {
            int idx = registryName.indexOf(':');
            return idx >= 0 ?
                    registryName.substring(0, idx) :
                    FluidRegistry.getModId(FluidRegistry.getFluidStack(registryName, 1000));
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
    }
}
