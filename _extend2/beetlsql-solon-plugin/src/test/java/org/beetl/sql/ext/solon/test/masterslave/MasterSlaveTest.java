package org.beetl.sql.ext.solon.test.masterslave;

import org.noear.solon.annotation.XInject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.test.SolonTest;
import org.noear.solon.test.SolonJUnit4ClassRunner;

@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(MasterSlaveApp.class)
public class MasterSlaveTest {
    @XInject
    MasterSlaveService service;

    @Test
    public void test(){
        service.test();
    }
}