package org.channel.cache.manager;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zhangchanglu
 * @since 2018/09/26 15:20.
 */
@RestController
public class CacheController {
    @Resource
    private CacheService cacheService;

    @RequestMapping("/get/cache/{id}")
    public String get(@PathVariable String id) {
        return cacheService.get(id,"123");
    }
}
