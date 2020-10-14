package webapp.demoa_interceptor;

import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XHandler;

@XMapping("/demoa/trigger")
@XController
public class demoa_handler implements XHandler {
    @Override
    public void handle(XContext context) throws Throwable {
        context.output(context.path());
    }
}
