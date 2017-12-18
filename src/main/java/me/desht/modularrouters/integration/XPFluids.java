package me.desht.modularrouters.integration;

import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.List;

public class XPFluids {
    public static final List<XPCollectionType> AVAILABLE_XP_COLLECTION_TYPES = new ArrayList<>();

    public static void detectXPFluids() {
        for (XPCollectionType type : XPCollectionType.values()) {
            if (type.getRegistryName().isEmpty() || FluidRegistry.isFluidRegistered(type.getRegistryName())) {
                AVAILABLE_XP_COLLECTION_TYPES.add(type);
            }
        }
    }

    public enum XPCollectionType {
        BOTTLE_O_ENCHANTING(0, ""),  // special case: vanilla bottles o' enchanting
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

        public String getModId() {
            return registryName.isEmpty() ? "minecraft" : FluidRegistry.getModId(FluidRegistry.getFluidStack(registryName, 1000));
        }
    }
}
