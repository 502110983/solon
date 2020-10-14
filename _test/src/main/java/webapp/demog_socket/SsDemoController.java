package webapp.demog_socket;

import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.web.XContext;
import org.noear.solon.core.XMethod;

@XController
public class SsDemoController {

    @XMapping(value = "/demog/**", method = XMethod.SOCKET)
    public void test(XContext ctx) throws Exception {
        if (ctx == null) {
            return;
        }

        String msg = ctx.body();

        if (msg.equals("close")) {
            ctx.output("它叫我关了：" + msg);
            System.out.println("它叫我关了：" + msg + "!!!");
            ctx.close();//关掉
        } else {
            ctx.output("我收到了：" + msg);
        }
    }
}

