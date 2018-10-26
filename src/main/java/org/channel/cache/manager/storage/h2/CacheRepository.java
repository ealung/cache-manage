package org.channel.cache.manager.storage.h2;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author zhangchanglu
 * @since 2018/10/16 17:18.
 */
@Repository
public interface CacheRepository extends CrudRepository<CacheEntity, Long> {
    Iterable<CacheEntity> findAllByClassName(String className);

    Iterable<CacheEntity> findAllByClassNameLike(String className);

    Iterable<CacheEntity> findAllByClassNameAndMethodName(String className, String methodName);

    CacheEntity findAllByClassNameAndMethodNameAndCacheConfigKey(String className, String methodName, String cacheConfigKey);

}
