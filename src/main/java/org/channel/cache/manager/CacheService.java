package org.channel.cache.manager;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author zhangchanglu
 * @since 2018/09/26 15:15.
 */
@Service
@CacheConfig(cacheNames = "cacheService")
public class CacheService {
    @Cacheable(key = "#id",cacheNames = {"aaa"})
    @CacheEvict(key = "#name")
    public String get(String id, String name) {
        return id;
    }

    @Cacheable(key = "#name")
    public String getName(String name) {
        return name;
    }
}
