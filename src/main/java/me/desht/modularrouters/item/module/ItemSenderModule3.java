package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.Sender3Executor;

public class ItemSenderModule3 extends ItemSenderModule2 {
    public ItemSenderModule3() {
        super("senderModule3");
    }

    protected boolean isRangeLimited() {
        return false;
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender3Executor();
    }
}
