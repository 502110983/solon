package org.noear.solon.boot.jdkhttp;


import com.sun.net.httpserver.HttpExchange;
import org.noear.solon.boot.jdkhttp.uploadfile.MultipartIterator;
import org.noear.solon.web.XFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class MultipartUtil {
    public static void buildParamsAndFiles(JdkHttpContext context) throws IOException{
        MultipartIterator parts = new MultipartIterator((HttpExchange) context.request());

        while (parts.hasNext()){
            MultipartIterator.Part part = parts.next();
            if(isFile(part) == false){
                context.paramSet(part.name, part.getString());
            }else{
                doBuildFiles(context, part);
            }
        }
    }

    private static void doBuildFiles(JdkHttpContext context, MultipartIterator.Part part) throws IOException{
        List<XFile> list = context._fileMap.get(part.getName());
        if(list == null){
            list = new ArrayList<>();
            context._fileMap.put(part.getName(), list);

            XFile f = new XFile();
            f.contentType = part.getHeaders().get("Content-Type");
            f.content = read(part.getBody());
            f.name = part.getFilename();
            int idx = f.name.lastIndexOf(".");

            if (idx > 0) {
                f.extension = f.name.substring(idx + 1);
            }

            list.add(f);
        }
    }

    private static boolean isField(MultipartIterator.Part filePart){
        return filePart.getFilename() == null;
    }

    private static boolean isFile(MultipartIterator.Part filePart){
        return !isField(filePart);
    }

    private static ByteArrayInputStream read(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return new ByteArrayInputStream(output.toByteArray());
    }
}
