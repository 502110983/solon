package webapp.demo2_mvc;

import org.noear.solon.annotation.XMapping;
import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XSingleton;
import org.noear.solon.web.XContext;

@XSingleton(false)
@XMapping("/demo2/mapping")
@XController
public class MappingController {
    //支持与父XMapping叠国路径
    @XMapping("a")
    public String test_a(XContext context) {
        return context.path();
    }

    //支持*一段路径匹配
    @XMapping("b/*")
    public String test_b(XContext context) {
        return context.path();
    }

    //支持**不限长度匹配
    @XMapping("c/**")
    public String test_c(XContext context) {
        return context.path();
    }

    //支持特征路径匹配1
    @XMapping("d1/**/$*")
    public String test_d1(XContext context) {
        return context.path();
    }

    //支持特征路径匹配2
    @XMapping("d1/**/@*")
    public String test_d2(XContext context) {
        return context.path();
    }

    //支持path var匹配
    @XMapping("e/{p_q}/{obj}/{id}")
    public String test_e(XContext context, String p_q, String obj, String id) {
        return context.path() + "::" + p_q + "-" + obj + "-" + id;
    }
}
