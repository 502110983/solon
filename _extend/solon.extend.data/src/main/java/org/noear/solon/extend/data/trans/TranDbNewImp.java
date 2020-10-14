package org.noear.solon.extend.data.trans;

import org.noear.solon.annotation.XTran;
import org.noear.solon.functions.RunnableEx;
import org.noear.solon.extend.data.TranNode;
import org.noear.solon.extend.data.TranManager;

public class TranDbNewImp extends DbTran implements TranNode {

    public TranDbNewImp(XTran meta) {
        super(meta);
    }

    @Override
    public void apply(RunnableEx runnable) throws Throwable {
        //尝试挂起事务
        //
        DbTran tran = TranManager.trySuspend();

        try {
            super.execute(() -> {
                runnable.run();
            });
        } finally {
            //尝试恢复事务
            TranManager.tryResume(tran);
        }
    }
}