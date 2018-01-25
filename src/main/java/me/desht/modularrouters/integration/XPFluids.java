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

public class XPFluids {
    private static final boolean[] AVAILABLE_XP_COLLECTION_TYPES = new boolean[XPCollectionType.values().length];
    private static final ItemStack[] ICONS = new ItemStack[XPCollectionType.values().length];

    public static void detectXPFluids() {
        Arrays.stream(XPCollectionType.values())
                .forEach(type -> AVAILABLE_XP_COLLECTION_TYPES[type.ordinal()] = !getIconForResource(type.registryName).isEmpty());
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
        BOTTLE_O_ENCHANTING(0, "minecraft:experience_bottle"),  // special case: vanilla bottles o' enchanting
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
            return AVAILABLE_XP_COLLECTION_TYPES[this.ordinal()];
        }

        public ItemStack getIcon() {
            if (ICONS[ordinal()] == null) {
                ICONS[ordinal()] = getIconForResource(registryName);
            }
            return ICONS[ordinal()];
        }
    }
}
