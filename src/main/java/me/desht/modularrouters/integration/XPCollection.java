package me.desht.modularrouters.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

// FIXME redo this completely to use datapacks, and use "forge:experience" fluid tag
public class XPCollection {
    private static final Set<XPCollectionType> AVAILABLE = EnumSet.noneOf(XPCollectionType.class);
    private static final Map<XPCollectionType, ItemStack> ICONS = new EnumMap<>(XPCollectionType.class);

    public static void detectXPTypes() {
        ICONS.clear();

        for (XPCollectionType type : XPCollectionType.values()) {
            if (!getIconForResource(type).isEmpty()) AVAILABLE.add(type);
        }
    }

    private static ItemStack getIconForResource(XPCollectionType type) {
        if (!ICONS.containsKey(type)) {
            if (type.isSolid()) {
                ICONS.put(type, new ItemStack(ForgeRegistries.ITEMS.getValue(type.getRegistryName())));
            } else {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(type.getRegistryName());
                ICONS.put(type, fluid == null || fluid == Fluids.EMPTY ?
                        ItemStack.EMPTY :
                        FluidUtil.getFilledBucket(new FluidStack(fluid, 1000)));
            }
        }
        return ICONS.getOrDefault(type, ItemStack.EMPTY);
    }

    public static XPCollectionType getXPType(int type) {
        XPCollectionType xpType = XPCollectionType.values()[type];
        if (!xpType.isAvailable()) {
            xpType = XPCollectionType.BOTTLE_O_ENCHANTING;
        }
        return xpType;
    }

    public enum XPCollectionType {
        // note: bottles o' enchanting are randomly worth 3-11 experience, so let's use an average of 7
        SOLIDIFIED_EXPERIENCE(true, 8, "actuallyadditions:item_solidified_experience"),
        BOTTLE_O_ENCHANTING(true, 7, "minecraft:experience_bottle"),
        NUGGET_OF_EXPERIENCE(true, 7, "create:experience_nugget"),
        MEMORY_ESSENCE(false, 20, "pneumaticcraft:memory_essence"),
        CYCLIC_XP_JUICE(false, 20, "cyclic:xpjuice"),
        IF_ESSENCE(false, 20, "industrialforegoing:essence"),
        SOPHISTICATED_EXPERIENCE(false, 9, "sophisticatedcore:xp_still");

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
            return AVAILABLE.contains(this);
        }

        public ItemStack getIcon() {
            return getIconForResource(this);
        }

        public Component getDisplayName() {
            return getIconForResource(this).getHoverName();
        }
    }
}
