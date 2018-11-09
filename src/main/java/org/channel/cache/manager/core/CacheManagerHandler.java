package org.channel.cache.manager.core;

import org.channel.cache.manager.storage.CacheStorage;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * @author zhangchanglu
 * @since 2018/09/26 16:21.
 */
public interface CacheManagerHandler extends Ordered {
    void setStorage(CacheStorage cacheStorage);

    void afterCacheInvoker(List<CacheManagerInterceptor.CacheManagerOperationContext> netEaseCacheOperationContexts);

    void afterCacheAble(List<CacheManagerInterceptor.CacheManagerOperationContext> netEaseCacheOperationContexts);

    void afterCacheEvict(List<CacheManagerInterceptor.CacheManagerOperationContext> netEaseCacheOperationContexts);

    void afterCachePut(List<CacheManagerInterceptor.CacheManagerOperationContext> netEaseCacheOperationContexts);
}
