package org.channel.cache.manager.storage;

import org.channel.cache.manager.core.CacheManagerInterceptor;
import org.channel.cache.manager.dto.ClassCacheDto;

import java.util.Collection;
import java.util.List;

/**
 * @author zhangchanglu
 * @since 2018/09/30 15:58.
 */
public interface CacheStorage extends CacheManagerOperation {
    /**
     * 所有缓存配置
     *
     * @param cacheConfig 缓存配置集合
     */
    void allCacheConfig(Collection<ClassCacheDto> cacheConfig);

    /**
     * 获取所有缓存
     */
    Collection<ClassCacheDto> getAllCache();

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return 是否删除成功
     */
    boolean removeCacheName(String cacheName, String key);

    boolean removeClassName(String cacheName, String key);

    boolean removeCache(String key);
    void addCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts);

    void removeCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts);

}
