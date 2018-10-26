package org.channel.cache.manager.storage.h2;

import javax.persistence.*;

/**
 * @author zhangchanglu
 * @since 2018/10/16 17:21.
 */
@Entity
public class CacheKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "cache_id")
    @ManyToOne
    private CacheEntity cacheEntity;
    //缓存key
    private String key;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public CacheEntity getCacheEntity() {
        return cacheEntity;
    }

    public void setCacheEntity(CacheEntity cacheEntity) {
        this.cacheEntity = cacheEntity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
