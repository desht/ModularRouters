package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.TargetedSender;
import net.minecraft.item.ItemStack;

public class CompiledModuleSettings {
    private final Filter filter;
    private final Module module;
    private final Module.RelativeDirection direction;
    private final TargetedSender.DimensionPos target;
    private final boolean termination;

    public CompiledModuleSettings(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        filter = new Filter(stack);
        module = ItemModule.getModule(stack);
        direction = Module.getDirectionFromNBT(stack);
        termination = Module.terminates(stack);
        target = TargetedSender.getTarget(stack);
    }

    public Module getModule() {
        return module;
    }

    public Filter getFilter() {
        return filter;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }

    public boolean execute(TileEntityItemRouter router) {
        return module.execute(router, this);
    }

    public TargetedSender.DimensionPos getTarget() {
        return target;
    }

    public boolean termination() {
        return termination;
    }

//    public static CompiledModuleSettings compile(ItemStack stack) {
//        if (stack.getItem() instanceof ItemModule) {
//            Class<? extends CompiledModuleSettings> c = ((AbstractModule) stack.getItem()).getCompiler();
//            Constructor<? extends CompiledModuleSettings> ctor;
//            try {
//                ctor = c.getConstructor(ItemStack.class);
//                return ctor.newInstance(stack);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            return null;
//        } else {
//            throw new IllegalArgumentException(stack.getDisplayName() + " is not an item router module!");
//        }
//    }
}
