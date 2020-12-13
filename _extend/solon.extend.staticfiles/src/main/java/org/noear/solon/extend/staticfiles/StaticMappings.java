package org.noear.solon.extend.staticfiles;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.util.ResourceUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class StaticMappings extends ArrayList<StaticLocation> {
    private StaticMappings() {
        super();
    }

    private static StaticMappings _instance;

    public static StaticMappings instance() {
        if (_instance == null) {
            _instance = new StaticMappings();
        }

        return _instance;
    }


    public StaticMappings add(String start, String location) {
        StaticLocation mapping = new StaticLocation();
        mapping.start = start;
        if (location.endsWith("/")) {
            mapping.location = location.substring(0, location.length() - 1);
        } else {
            mapping.location = location;
        }


        if (Solon.cfg().isDebugMode()) {
            String dirroot = ResourceUtil.getResource("/")
                    .toString()
                    .replace("target/classes/", "");

            if (dirroot.startsWith("file:")) {
                mapping.locationDebug = dirroot + "src/main/resources" + location;
            }
        }

        this.add(mapping);

        return this;
    }

    protected URL find(String path) throws Exception {
        URL rst = null;

        for (StaticLocation m : this) {
            if (path.startsWith(m.start)) {
                if (m.locationDebug != null) {
                    URI uri = URI.create(m.locationDebug + path);
                    File file = new File(uri);

                    if (file.exists()) {
                        rst = uri.toURL();
                    }
                } else {
                    rst = ResourceUtil.getResource(m.location + path);
                }

                if (rst != null) {
                    return rst;
                }
            }
        }

        return null;
    }
}
