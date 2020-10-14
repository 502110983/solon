package org.noear.solon.extend.data.trans;

import org.noear.solon.XUtil;
import org.noear.solon.annotation.XTran;
import org.noear.solon.event.XEventBus;
import org.noear.solon.function.RunnableEx;
import org.noear.solon.extend.data.TranNode;
import org.noear.solon.extend.data.TranManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据事务
 *
 * @author noear
 * @since 1.0
 * */
public abstract class DbTran extends DbTranNode implements TranNode {
    private final XTran meta;
    private final Map<DataSource, Connection> conMap = new HashMap<>();

    public XTran getMeta() {
        return meta;
    }

    public Connection getConnection(DataSource ds) throws SQLException {
        if (conMap.containsKey(ds)) {
            return conMap.get(ds);
        } else {
            Connection con = ds.getConnection();
            con.setAutoCommit(false);
            if (meta.isolation().level > 0) {
                con.setTransactionIsolation(meta.isolation().level);
            }

            conMap.putIfAbsent(ds, con);
            return con;
        }
    }

    public DbTran(XTran meta) {
        this.meta = meta;
    }

    public void execute(RunnableEx runnable) throws Throwable {
        try {
            //conMap 此时，还是空的
            //
            TranManager.currentSet(this);

            //conMap 会在run时产生
            //
            runnable.run();

            if (parent == null) {
                commit();
            }
        } catch (Throwable ex) {
            if (parent == null) {
                rollback();
            }

            throw XUtil.throwableUnwrap(ex);
        } finally {
            TranManager.currentRemove();

            if (parent == null) {
                close();
            }
        }
    }

    @Override
    public void commit() throws Throwable {
        super.commit();

        for (Map.Entry<DataSource, Connection> kv : conMap.entrySet()) {
            kv.getValue().commit();
        }
    }

    @Override
    public void rollback() throws Throwable {
        super.rollback();
        for (Map.Entry<DataSource, Connection> kv : conMap.entrySet()) {
            kv.getValue().rollback();
        }
    }

    @Override
    public void close() throws Throwable {
        super.close();
        for (Map.Entry<DataSource, Connection> kv : conMap.entrySet()) {
            //kv.getValue().setAutoCommit(true);
            try {
                if (kv.getValue().isClosed() == false) {
                    kv.getValue().close();
                }
            } catch (Throwable ex) {
                XEventBus.push(ex);
            }
        }
    }
}
