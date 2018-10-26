package org.channel.cache.manager.storage;

import org.channel.cache.manager.dto.ClassCacheDto;

import java.util.Collection;

/**
 * @author zhangchanglu
 * @since 2018/10/23 17:41.
 */
public interface CacheManagerOperation {
    Collection<ClassCacheDto> search(SearchParam searchParam);
}
