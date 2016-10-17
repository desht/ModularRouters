package me.desht.modularrouters.logic.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class Filter {
    private final Flags flags;
    private final List<IItemMatcher> matchers = Lists.newArrayList();

    public Filter() {
        flags = new Flags();
    }

    public Filter(ItemStack moduleStack) {
        if (moduleStack.getItem() instanceof ItemModule) {
            flags = new Flags(moduleStack);
            // it's safe to assume that the module actually has NBT data at this point...
            NBTTagList tagList = moduleStack.getTagCompound().getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                ItemStack filterStack = ItemStack.loadItemStackFromNBT(tagCompound);
                matchers.add(createMatcher(filterStack));
            }
        } else {
            flags = new Flags();
        }
    }

    private IItemMatcher createMatcher(ItemStack stack) {
        return new SimpleItemMatcher(stack);
    }

    public boolean pass(ItemStack stack) {
        for (IItemMatcher matcher : matchers) {
            if (matcher.matchItem(stack, flags)) {
                return !flags.isBlacklist();
            }
        }

        // no matches: pass if this is a blacklist, fail if a whitelist
        return flags.isBlacklist();
    }

    public class Flags {
        private final boolean blacklist;
        private final boolean ignoreMeta;
        private final boolean ignoreNBT;
        private final boolean ignoreOredict;

        public Flags(ItemStack moduleStack) {
            Validate.isTrue(moduleStack.getItem() instanceof ItemModule);

            Module module = ItemModule.getModule(moduleStack);
            blacklist = module.isBlacklist(moduleStack);
            ignoreMeta = module.ignoreMeta(moduleStack);
            ignoreNBT = module.ignoreNBT(moduleStack);
            ignoreOredict = module.ignoreOreDict(moduleStack);
        }

        public Flags() {
            blacklist = false;
            ignoreMeta = false;
            ignoreNBT = true;
            ignoreOredict = true;
        }

        public boolean isBlacklist() {
            return blacklist;
        }

        public boolean isIgnoreMeta() {
            return ignoreMeta;
        }

        public boolean isIgnoreNBT() {
            return ignoreNBT;
        }

        public boolean isIgnoreOredict() {
            return ignoreOredict;
        }

    }
}
