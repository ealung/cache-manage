package org.channel.cache.manager.storage.h2;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author zhangchanglu
 * @since 2018/10/16 17:50.
 */
@Repository

public interface CacheNameRepository extends CrudRepository<CacheNameEntity, Long> {
    Collection<CacheNameEntity> findByCacheNameLike(String cacheName);

    Collection<CacheNameEntity> findByCacheName(String cacheName);

    Collection<CacheNameEntity> findByCacheEntity_Id(Long cacheEntiy_id);
}
