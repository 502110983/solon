package webapp.demo7_test;

import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.web.XContext;

@XController
public class TestController {
    @XMapping("/demo7/test")
    public void test(XContext c) throws Exception{
        c.output(c.path());
    }
}
