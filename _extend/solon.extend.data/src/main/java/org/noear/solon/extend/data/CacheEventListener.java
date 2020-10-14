package org.noear.solon.extend.data;

import org.noear.solon.XUtil;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.cache.CacheService;
import org.noear.solon.core.XEventListener;

class CacheEventListener implements XEventListener<BeanWrap> {
    @Override
    public void onEvent(BeanWrap bw) {
        if (bw.raw() instanceof CacheService) {
            if (XUtil.isEmpty(bw.name())) {
                CacheLib.cacheServiceAdd("", bw.raw());
            } else {
                CacheLib.cacheServiceAddIfAbsent(bw.name(), bw.raw());

                if (bw.typed()) {
                    CacheLib.cacheServiceAdd("", bw.raw());
                }
            }
        }
    }
}
