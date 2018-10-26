package org.channel.cache.manager.storage.h2;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author zhangchanglu
 * @since 2018/10/16 17:54.
 */
@Repository
public interface CacheKeyRepository extends CrudRepository<CacheKeyEntity, Long> {
    CacheKeyEntity findByKeyAndCacheEntity_Id(String key, Long cacheEntityId);

    Iterable<CacheKeyEntity> findByKey(String key);

    Iterable<CacheKeyEntity> findByKeyLike(String key);

    void removeByCacheEntity_IdAndKey(Long cacheEntityId, String key);

    Collection<CacheKeyEntity> findByCacheEntity_Id(Long cacheEntityId);

    int removeByKey(String key);
}
