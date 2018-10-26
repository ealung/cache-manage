package org.channel.cache.manager.web;

import org.channel.cache.manager.dto.ClassCacheDto;
import org.channel.cache.manager.storage.CacheStorage;
import org.channel.cache.manager.storage.SearchParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * @author zhangchanglu
 * @since 2018/10/12 19:04.
 */
@Controller
public class CacheManagerController {
    @Resource
    private CacheStorage cacheStorage;

    @RequestMapping("/cache/manager/all")
    @ResponseBody
    public Collection<ClassCacheDto> allCache() {
        return cacheStorage.getAllCache();
    }

    @RequestMapping("/cache/manager/search")
    @ResponseBody
    public Collection<ClassCacheDto> search(SearchParam searchParam) {
        return cacheStorage.search(searchParam);
    }

    @RequestMapping("/cache/manager/remove")
    @ResponseBody
    public Object cacheRemove(String key) {
        return cacheStorage.removeCache(key);
    }

    @RequestMapping("/cache/manager/remove/cacheName")
    @ResponseBody
    public Object cacheRemoveConfig(SearchParam searchParam) {
        if (searchParam.getModel() == 1) {
            return cacheStorage.removeClassName(searchParam.getClassName(), searchParam.getCacheKey());
        } else {
            return cacheStorage.removeCacheName(searchParam.getClassName(), searchParam.getCacheKey());
        }
    }
}
