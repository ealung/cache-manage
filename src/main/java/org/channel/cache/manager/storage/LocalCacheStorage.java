package org.channel.cache.manager.storage;

import org.channel.cache.manager.core.CacheManagerInterceptor;
import org.channel.cache.manager.dto.CacheManagerDto;
import org.channel.cache.manager.dto.CacheMethodDto;
import org.channel.cache.manager.dto.ClassCacheDto;
import javafx.util.Callback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangchanglu
 * @since 2018/09/30 16:22.
 */
@Component
@ConditionalOnMissingBean(CacheStorage.class)
public class LocalCacheStorage implements CacheStorage {
    private final String CACHE_CLASS_CONFIG = "KADA_CACHE_CACHE_CLASS_CONFIG";
    private final String CACHE_CLASS_CACHE = "KADA_CACHE_CACHE";
    private static Map<String, Collection<CacheMethodDto>> cacheInfo = new ConcurrentHashMap<>();

    @Override
    public void allCacheConfig(Collection<ClassCacheDto> cacheConfig) {
        cacheInfo.clear();
        for (ClassCacheDto classCacheDto : cacheConfig) {
            Collection<CacheMethodDto> cacheMethodDtos = classCacheDto.getCacheMethodDtos();
            cacheInfo.put(getClassName(classCacheDto.getaClass()), cacheMethodDtos);
        }
    }

    private String getClassName(Class cla) {
        int index = cla.getName().indexOf("$$");
        return cla.getName().substring(0, index);
    }

    @Override
    public Collection<ClassCacheDto> search(SearchParam searchParam) {
        return null;
    }

    private CacheManagerDto convertToCacheManagerDto(String className, String methodName, CacheOperation cacheOperation) {
        CacheManagerDto cacheManagerDto = new CacheManagerDto();
        cacheManagerDto.setClassName(className);
        cacheManagerDto.setMethodName(methodName);
        cacheManagerDto.setCacheNames(cacheOperation.getCacheNames());
        cacheManagerDto.setCacheOperation(cacheOperation.getClass().getSimpleName());
        cacheManagerDto.setCacheConfigKey(cacheOperation.getKey());
        return cacheManagerDto;
    }

    private Collection<ClassCacheDto> getClassCache(Map<String, Collection<CacheMethodDto>> map) {
        Collection<ClassCacheDto> classCacheDtos = new ArrayList<>();
        map.forEach((s, cacheMethodDtos) -> {
            ClassCacheDto classCacheDto = new ClassCacheDto(cacheMethodDtos);
            classCacheDto.setClassName(s);
            classCacheDtos.add(classCacheDto);
        });
        return classCacheDtos;
    }

    private int getClassMethodSplitIndex(String name) {
        int i = name.lastIndexOf("(");
        name = name.substring(0, i);
        return name.lastIndexOf(".");
    }

    @Override
    public Collection<ClassCacheDto> getAllCache() {
        return getClassCache(cacheInfo);
    }

    @Override
    public boolean removeCacheName(String cacheName, String key) {
        return removeCache(key);
    }

    @Override
    public boolean removeClassName(String cacheName, String key) {
        return removeCache(key);
    }

    @Override
    public boolean removeCache(String key) {
        for (Map.Entry<String, Collection<CacheMethodDto>> stringListEntry : cacheInfo.entrySet()) {
            for (CacheMethodDto cacheMethodDto : stringListEntry.getValue()) {
                for (CacheManagerDto cacheManagerDto : cacheMethodDto.getCacheManagerDtos()) {
                    for (Cache cache : cacheManagerDto.getCaches()) {
                        cache.evict(key);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void addCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        CacheManagerInterceptor.NetEaseCacheOperationContext cacheOperationContext = netEaseCacheOperationContexts.get(0);
        handelForClassMethodCache(cacheOperationContext, managerDto -> {
            if (null == managerDto.getCacheKeys()) {
                managerDto.setCacheKeys(new ArrayList<>());
            }
            managerDto.getCacheKeys().add(cacheOperationContext.getKey().toString());
            return managerDto;
        });
    }

    @Override
    public void removeCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        CacheManagerInterceptor.NetEaseCacheOperationContext cacheOperationContext = netEaseCacheOperationContexts.get(0);
        handelForClassMethodCache(cacheOperationContext, managerDto -> {
            if (null != managerDto.getCacheKeys()) {
                managerDto.getCacheKeys().remove(cacheOperationContext.getKey().toString());
            }
            return null;
        });
    }

    private void handelForClassMethodCache(CacheManagerInterceptor.NetEaseCacheOperationContext cacheOperationContext, Callback<CacheManagerDto, Object> callback) {
        CacheOperation cacheOperation = cacheOperationContext.getOperation();
        String className = cacheOperationContext.getTarget().getClass().getName();
        Collection<? extends Cache> caches = cacheOperationContext.getCaches();
        CacheManagerDto cacheManagerDto = convertToCacheManagerDto(cacheOperationContext.getTarget().getClass().getName(), cacheOperationContext.getMethod().getName(), cacheOperation);
        Collection<CacheMethodDto> map = cacheInfo.get(className);
        for (CacheMethodDto cacheMethodDto : map) {
            for (CacheManagerDto managerDto : cacheMethodDto.getCacheManagerDtos()) {
                if (managerDto.getCacheConfigKey().equals(cacheOperation.getKey())) {
                    managerDto.setCaches(caches);
                    callback.call(managerDto);
                }
            }
        }
        cacheInfo.put(cacheManagerDto.getClassName(), map);
    }
}
