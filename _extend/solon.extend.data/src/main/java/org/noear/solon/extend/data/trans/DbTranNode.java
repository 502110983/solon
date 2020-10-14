package org.noear.solon.extend.data.trans;

import org.noear.solon.event.XEventBus;
import org.noear.solon.extend.data.TranNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据事务节点（用于生成事务树）
 *
 * @author noear
 * @since 1.0
 * */
public abstract class DbTranNode implements TranNode {
    protected DbTranNode parent;
    protected List<DbTranNode> children = new ArrayList<>();

    @Override
    public void add(TranNode slave) {
        if (slave instanceof DbTranNode) {
            DbTranNode node = (DbTranNode) slave;

            node.parent = this;
            this.children.add(node);
        }
    }

    public void commit() throws Throwable {
        for (DbTranNode n1 : children) {
            n1.commit();
        }
    }

    public void rollback() throws Throwable {
        //确保每个子处事，都有机会回滚
        //
        for (DbTranNode n1 : children) {
            try {
                n1.rollback();
            } catch (Throwable ex) {
                XEventBus.push(ex);
            }
        }
    }

    public void close() throws Throwable {
        //确保每个子处事，都有机会关闭
        //
        for (DbTranNode n1 : children) {
            try {
                n1.close();
            } catch (Throwable ex) {
                XEventBus.push(ex);
            }
        }
    }
}
