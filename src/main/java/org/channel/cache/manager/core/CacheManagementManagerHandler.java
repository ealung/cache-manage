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
    public void afterCacheInvoker(List<CacheManagerInterceptor.CacheManagerOperationContext> cacheManagerOperationContexts) {
    }

    @Override
    public void afterCacheAble(List<CacheManagerInterceptor.CacheManagerOperationContext> cacheManagerOperationContexts) {
        cacheStorage.addCache(cacheManagerOperationContexts);
    }

    @Override
    public void afterCacheEvict(List<CacheManagerInterceptor.CacheManagerOperationContext> cacheManagerOperationContexts) {
        cacheStorage.removeCache(cacheManagerOperationContexts);
    }

    @Override
    public void afterCachePut(List<CacheManagerInterceptor.CacheManagerOperationContext> cacheManagerOperationContexts) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
