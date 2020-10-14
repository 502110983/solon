package webapp.demo2_mvc;

import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.annotation.XSingleton;
import org.noear.solon.web.XContext;
import org.noear.solon.web.XFile;

@XSingleton(false)
@XMapping("/demo2/upload")
@XController
public class UploadController {

    //支持上传文件参数（file 变量名，与表单变量名保持一至）
    @XMapping("f1")
    public String test_f1(XContext context, XFile file) throws Exception{
        return context.path();
    }

    //支持上传文件参数
    @XMapping("f2")
    public String test_f2(XContext context) throws Exception{
        XFile file = context.file("file"); //（file 变量名，与表单变量名保持一至）

        return context.path();
    }

}
