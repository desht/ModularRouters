package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.item.module.TargetedSender;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CompiledModuleSettings {
    private final Filter filter;
    private final AbstractModule module;
    private final AbstractModule.RelativeDirection direction;
    private final ModuleExecutor executor;
    private final TargetedSender.DimensionPos target;
    private final boolean termination;

    public CompiledModuleSettings(ItemStack stack) {
        if (!(stack.getItem() instanceof AbstractModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        filter = new Filter(stack);
        module = (AbstractModule) stack.getItem();
        direction = AbstractModule.getDirectionFromNBT(stack);
        executor = module.getExecutor();
        termination = AbstractModule.terminates(stack);
        target = TargetedSender.getTarget(stack);
    }

    public AbstractModule getModule() {
        return module;
    }

    public ModuleExecutor getExecutor() {
        return executor;
    }

    public Filter getFilter() {
        return filter;
    }

    public AbstractModule.RelativeDirection getDirection() {
        return direction;
    }

    public boolean execute(TileEntityItemRouter router) {
        return executor.execute(router, this);
    }

    public TargetedSender.DimensionPos getTarget() {
        return target;
    }

    public boolean termination() {
        return termination;
    }

    public static CompiledModuleSettings compile(ItemStack stack) {
        if (stack.getItem() instanceof AbstractModule) {
            Class<? extends CompiledModuleSettings> c = ((AbstractModule) stack.getItem()).getCompiler();
            Constructor<? extends CompiledModuleSettings> ctor;
            try {
                ctor = c.getConstructor(ItemStack.class);
                return ctor.newInstance(stack);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            throw new IllegalArgumentException(stack.getDisplayName() + " is not an item router module!");
        }
    }
}
