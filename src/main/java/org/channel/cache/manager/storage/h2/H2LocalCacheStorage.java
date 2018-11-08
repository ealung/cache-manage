package org.channel.cache.manager.storage.h2;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.channel.cache.manager.core.CacheManagerInterceptor;
import org.channel.cache.manager.dto.CacheManagerDto;
import org.channel.cache.manager.dto.CacheMethodDto;
import org.channel.cache.manager.dto.ClassCacheDto;
import org.channel.cache.manager.storage.CacheManagerOperation;
import org.channel.cache.manager.storage.CacheStorage;
import org.channel.cache.manager.storage.SearchParam;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangchanglu
 * @since 2018/10/16 17:39.
 */
@Component
public class H2LocalCacheStorage implements CacheStorage, CacheManagerOperation {
    @Resource
    private CacheRepository cacheRepository;
    @Resource
    private CacheNameRepository cacheNameRepository;
    @Resource
    private CacheKeyRepository cacheKeyRepository;
    @Resource
    private CacheManager cacheManager;

    @Override
    public Collection<ClassCacheDto> search(SearchParam searchParam) {
        //cacheName视图模式
        if (!Objects.isNull(searchParam.getModel()) && searchParam.getModel().equals(2)) {
            return searchForCacheName(searchParam);
        }
        //类视图模式
        Multimap<String, CacheMethodDto> classCache = ArrayListMultimap.create();
        if (!Strings.isNullOrEmpty(searchParam.getClassName())) {
            Iterable<CacheEntity> allByClassName = cacheRepository.findAllByClassNameLike(getLike(searchParam.getClassName()));
            allByClassName.forEach(cacheEntity -> {
                CacheMethodDto cacheMethodDto = getCacheMethod(cacheEntity);
                classCache.put(cacheEntity.getClassName(), cacheMethodDto);
            });
        } else if (!Strings.isNullOrEmpty(searchParam.getCacheName())) {
            Collection<CacheNameEntity> byCacheNameLike = cacheNameRepository.findByCacheNameLike(getLike(searchParam.getCacheName()));
            byCacheNameLike.forEach(cacheNameEntity -> {
                CacheEntity cacheEntity = cacheNameEntity.getCacheEntity();
                CacheMethodDto cacheMethodDto = getCacheMethod(cacheEntity);
                classCache.put(cacheEntity.getClassName(), cacheMethodDto);
            });
        } else if (!Strings.isNullOrEmpty(searchParam.getCacheKey())) {
            Iterable<CacheKeyEntity> byKeyLike = cacheKeyRepository.findByKeyLike(getLike(searchParam.getCacheKey()));
            byKeyLike.forEach(cacheKeyEntity -> {
                CacheEntity cacheEntity = cacheKeyEntity.getCacheEntity();
                CacheMethodDto cacheMethodDto = getCacheMethod(cacheEntity);
                classCache.put(cacheEntity.getClassName(), cacheMethodDto);
            });
        } else {
            return getAllCache();
        }
        return getClassCache(classCache.asMap());
    }

    /**
     * cacheName视图，className存放cacheName
     *
     * @param searchParam 查询条件
     * @return 查询结果
     */
    private Collection<ClassCacheDto> searchForCacheName(SearchParam searchParam) {
        Multimap<String, CacheMethodDto> classCache = ArrayListMultimap.create();
        Collection<CacheNameEntity> all = new ArrayList<>();
        if (!Strings.isNullOrEmpty(searchParam.getClassName())) {
            Iterable<CacheEntity> allByClassName = cacheRepository.findAllByClassNameLike(getLike(searchParam.getClassName()));
            for (CacheEntity cacheEntity : allByClassName) {
                all.addAll(cacheNameRepository.findByCacheEntity_Id(cacheEntity.getId()));
            }
        } else if (!Strings.isNullOrEmpty(searchParam.getCacheName())) {
            all = cacheNameRepository.findByCacheNameLike(getLike(searchParam.getCacheName()));
        } else if (!Strings.isNullOrEmpty(searchParam.getCacheKey())) {
            Iterable<CacheKeyEntity> byKeyLike = cacheKeyRepository.findByKeyLike(getLike(searchParam.getCacheKey()));
            for (CacheKeyEntity cacheKeyEntity : byKeyLike) {
                CacheEntity cacheEntity = cacheKeyEntity.getCacheEntity();
                all.addAll(cacheNameRepository.findByCacheEntity_Id(cacheEntity.getId()));
            }
        } else {
            for (CacheNameEntity cacheNameEntity : cacheNameRepository.findAll()) {
                all.add(cacheNameEntity);
            }
        }
        for (CacheNameEntity cacheNameEntity : all) {
            CacheEntity cacheEntity = cacheNameEntity.getCacheEntity();
            CacheMethodDto cacheMethodDto = getCacheMethod(cacheEntity);
            classCache.put(cacheNameEntity.getCacheName(), cacheMethodDto);
        }
        return getClassCache(classCache.asMap());

    }

    private String getLike(String param) {
        return "%" + param + "%";
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

    private CacheMethodDto getCacheMethod(CacheEntity cacheEntity) {
        CacheMethodDto cacheMethodDto = new CacheMethodDto();
        cacheMethodDto.setMethodName(cacheEntity.getMethodName());
        cacheMethodDto.setCacheManagerDtos(cacheManagerDtos(cacheEntity));
        return cacheMethodDto;
    }
    private void clean() {
        cacheRepository.deleteAll();
        cacheNameRepository.deleteAll();
        cacheKeyRepository.deleteAll();
    }
    @Override
    public void allCacheConfig(Collection<ClassCacheDto> cacheConfig) {
        clean();
        for (ClassCacheDto classCacheDto : cacheConfig) {
            for (CacheMethodDto cacheMethodDto : classCacheDto.getCacheMethodDtos()) {
                CacheOperation cacheOperation = cacheMethodDto.getCacheOperation();
                CacheEntity cacheEntity = new CacheEntity();
                cacheEntity.setClassName(classCacheDto.getClassName());
                cacheEntity.setMethodName(cacheMethodDto.getMethodName());
                cacheEntity.setCacheConfigKey(cacheOperation.getKey());
                cacheEntity.setCacheOperation(cacheOperation.getClass().getSimpleName());
                cacheRepository.save(cacheEntity);
                for (String s : cacheOperation.getCacheNames()) {
                    CacheNameEntity cacheNameEntity = new CacheNameEntity();
                    cacheNameEntity.setCacheName(s);
                    cacheNameEntity.setCacheEntity(cacheEntity);
                    cacheNameRepository.save(cacheNameEntity);
                }
            }
        }
 /*       for (ClassCacheDto classCacheDto : cacheConfig) {
            Set<Map.Entry<Method, Collection<CacheOperation>>> entries = classCacheDto.getCacheOperation().entrySet();
            for (Map.Entry<Method, Collection<CacheOperation>> methodCollectionEntry : entries) {
                for (CacheOperation cacheOperation : methodCollectionEntry.getValue()) {
                    CacheEntity cacheEntity = new CacheEntity();
                    cacheEntity.setClassName(classCacheDto.getClassName());
                    cacheEntity.setMethodName(methodCollectionEntry.getCacheKey().getName());
                    cacheEntity.setCacheConfigKey(cacheOperation.getCacheKey());
                    cacheEntity.setCacheOperation(cacheOperation.getClass().getSimpleName());
                    cacheRepository.save(cacheEntity);
                    for (String s : cacheOperation.getCacheNames()) {
                        CacheNameEntity cacheNameEntity = new CacheNameEntity();
                        cacheNameEntity.setCacheName(s);
                        cacheNameEntity.setCacheEntity(cacheEntity);
                        cacheNameRepository.save(cacheNameEntity);
                    }
                }
            }
        }*/
    }

    @Override
    public Collection<ClassCacheDto> getAllCache() {
        Multimap<String, CacheMethodDto> classCache = ArrayListMultimap.create();
        for (CacheEntity cacheEntity : cacheRepository.findAll()) {
            CacheMethodDto cacheMethodDto = new CacheMethodDto();
            cacheMethodDto.setMethodName(cacheEntity.getMethodName());
            cacheMethodDto.setCacheManagerDtos(cacheManagerDtos(cacheEntity));
            classCache.put(cacheEntity.getClassName(), cacheMethodDto);
        }
        return getClassCache(classCache.asMap());
    }

    private Collection<CacheManagerDto> cacheManagerDtos(CacheEntity cacheEntity) {
        Collection<CacheManagerDto> cacheManagerDtos = new ArrayList<>();
        CacheManagerDto cacheManagerDto = new CacheManagerDto();
        cacheManagerDto.setCacheConfigKey(cacheEntity.getCacheConfigKey());
        cacheManagerDto.setCacheOperation(cacheEntity.getCacheOperation());
        cacheManagerDto.setClassName(cacheEntity.getClassName());
        cacheManagerDto.setMethodName(cacheEntity.getMethodName());
        Collection<CacheKeyEntity> byCacheEntity_id = cacheKeyRepository.findByCacheEntity_Id(cacheEntity.getId());
        if (!CollectionUtils.isEmpty(byCacheEntity_id)) {
            List<String> collect = byCacheEntity_id.stream().map(CacheKeyEntity::getCacheKey).collect(Collectors.toList());
            cacheManagerDto.setCacheKeys(collect);
        }
        cacheManagerDtos.add(cacheManagerDto);
        return cacheManagerDtos;
    }

    @Override
    public boolean removeCacheName(String cacheName, String key) {
        cacheNameRepository.findByCacheName(cacheName).forEach(cacheNameEntity -> {
            if (Strings.isNullOrEmpty(key)) {
                for (CacheKeyEntity cacheKeyEntity : cacheKeyRepository.findByCacheEntity_Id(cacheNameEntity.getCacheEntity().getId())) {
                    cacheKeyRepository.delete(cacheKeyEntity);
                }
            } else {
                cacheKeyRepository.removeByCacheEntity_IdAndKey(cacheNameEntity.getCacheEntity().getId(), key);
            }
        });
        Cache cache = cacheManager.getCache(cacheName);
        if (!Objects.isNull(cache)) {
            if (!Strings.isNullOrEmpty(key)) {
                cache.evict(key);
            } else {
                cache.clear();
            }
        }
        return true;
    }

    @Override
    public boolean removeClassName(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        cacheRepository.findAllByClassName(cacheName).forEach( cacheEntity -> {
            if(Strings.isNullOrEmpty(key)){
                for (CacheKeyEntity cacheKeyEntity : cacheKeyRepository.findByCacheEntity_Id(cacheEntity.getId())) {
                    cacheKeyRepository.delete(cacheKeyEntity);
                    if (!Objects.isNull(cache)) {
                        cache.evict(cacheKeyEntity.getCacheKey());
                    }
                }
            }else{
                cacheKeyRepository.removeByCacheEntity_IdAndKey(cacheEntity.getId(), key);
                if (!Objects.isNull(cache)) {
                    cache.evict(key);
                }
            }
        });
        return false;
    }

    @Override
    @Transactional
    public synchronized boolean removeCache(String key) {
        for (CacheKeyEntity cacheKeyEntity : cacheKeyRepository.findByKey(key)) {
            for (CacheNameEntity cacheNameEntity : cacheNameRepository.findByCacheEntity_Id(cacheKeyEntity.getCacheEntity().getId())) {
                Cache cache = cacheManager.getCache(cacheNameEntity.getCacheName());
                if (!Objects.isNull(cache)) {
                    cache.evict(key);
                }
            }
        }
        return cacheKeyRepository.removeByKey(key) > 0;
    }

    @Override
    public void addCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        CacheManagerInterceptor.NetEaseCacheOperationContext cacheOperationContext = netEaseCacheOperationContexts.get(0);
        CacheKeyEntity cacheKeyEntity = new CacheKeyEntity();
        String className = cacheOperationContext.getTarget().getClass().getName();
        String methodName = cacheOperationContext.getMethod().getName();
        CacheOperation cacheOperation = cacheOperationContext.getOperation();
        CacheEntity cacheEntity = cacheRepository.findAllByClassNameAndMethodNameAndCacheConfigKey(className, methodName, cacheOperation.getKey());
        if (null != cacheEntity) {
            if (null == cacheKeyRepository.findByKeyAndCacheEntity_Id(cacheOperationContext.getKey().toString(), cacheEntity.getId())) {
                cacheKeyEntity.setCacheKey(cacheOperationContext.getKey().toString());
                cacheKeyEntity.setCacheEntity(cacheEntity);
                cacheKeyRepository.save(cacheKeyEntity);
            }
        }
    }

    @Override
    public void removeCache(List<CacheManagerInterceptor.NetEaseCacheOperationContext> netEaseCacheOperationContexts) {
        CacheManagerInterceptor.NetEaseCacheOperationContext cacheOperationContext = netEaseCacheOperationContexts.get(0);
        String className = cacheOperationContext.getTarget().getClass().getName();
        String methodName = cacheOperationContext.getMethod().getName();
        CacheOperation cacheOperation = cacheOperationContext.getOperation();
        CacheEntity cacheEntity = cacheRepository.findAllByClassNameAndMethodNameAndCacheConfigKey(className, methodName, cacheOperation.getKey());
        if (!Objects.isNull(cacheEntity)) {
            CacheKeyEntity cacheKeyEntity = new CacheKeyEntity();
            cacheKeyEntity.setCacheKey(cacheOperationContext.getKey().toString());
            cacheKeyEntity.setCacheEntity(cacheEntity);
            cacheKeyRepository.delete(cacheKeyEntity);
        }
    }
}
