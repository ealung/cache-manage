package org.channel.cache.manager.dto;

import lombok.Data;
import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.Set;

/**
 * @author zhangchanglu
 * @since 2018/10/12 11:14.
 */
@Data
public class CacheManagerDto {
    //类名称
    private String className;
    //方法名称
    private String methodName;
    //缓存配置名称（cacheConfig
    private Set<String> cacheNames;
    //缓存配置的key
    private String cacheConfigKey;
    //缓存操作类型
    private String cacheOperation;
    //当前key已经缓存的key
    private Collection<String> cacheKeys;
    private Collection<? extends Cache> caches;
}
