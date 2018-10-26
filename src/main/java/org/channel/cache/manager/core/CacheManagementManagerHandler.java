package org.channel.cache.manager.core;

import org.channel.cache.manager.storage.CacheStorage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhangchanglu
 * @since 2018/09/26 17:05.
 */
@Component
public class CacheManagementManagerHandler implements CacheManagerHandler {
    private CacheStorage cacheStorage;

    @Override
    public void setStorage(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    @Override
    public void afterCacheInvoker(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        for (CacheManagerInterceptor.NetEaseCacheOperationContext netEaseCacheOperationContext : netEaseCacheOperationContexts) {
        }
    }

    @Override
    public void afterCacheAble(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        cacheStorage.addCache(netEaseCacheOperationContexts);
    }

    @Override
    public void afterCacheEvict(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        cacheStorage.removeCache(netEaseCacheOperationContexts);
    }

    @Override
    public void afterCachePut(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
