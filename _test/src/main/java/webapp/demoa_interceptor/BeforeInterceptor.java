package webapp.demoa_interceptor;

import org.noear.solon.annotation.XInterceptor;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.web.XContext;


@XInterceptor
public class BeforeInterceptor {

    @XMapping(value = "/demoa/**",index = 1, before = true)
    public void call(XContext context, String sev) {
        context.output("XInterceptor1::你被我拦截了(/demoa/**)!!!\n");
    }

    @XMapping(value = "/demoa/**",index = 3, before = true)
    public void call2(XContext context, String sev) {
        context.output("XInterceptor3::你被我拦截了(/demoa/**)!!!\n");
    }

    @XMapping(value = "/demoa/**",index = 2, before = true)
    public void call3(XContext context, String sev) {
        context.output("XInterceptor2::你被我拦截了(/demoa/**)!!!\n");
    }

}
