package webapp;

import org.noear.solon.core.util.ThrowableUtil;
import org.noear.solon.extend.socketd.SocketD;
import org.noear.solon.Solon;
import webapp.demoh_socketd.HelloRpcService;

public class Test2App {
    public static void main(String[] args) {
        Solon.start(TestApp.class, args, x -> x.enableHttp(false));

        int _port = 8080 + 20000;

        HelloRpcService rpc = SocketD.create("tcp://localhost:"+_port, HelloRpcService.class);

        while (true) {
            try {
                Thread.sleep(100);
                test_rpc_api(rpc);
            } catch (Throwable e) {
                ThrowableUtil.throwableUnwrap(e).printStackTrace();
            }
        }
    }

    public static void test_rpc_api(HelloRpcService rpc) throws Throwable {
        String rst = rpc.hello("noear");

        System.out.println(rst);

        assert "name=noear".equals(rst);
    }
}
