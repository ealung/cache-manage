package org.channel.cache.manager.dto;

import com.google.common.base.Strings;

import java.util.Collection;

/**
 * @author zhangchanglu
 * @since 2018/09/30 16:08.
 */
public class ClassCacheDto {
    //对应类
    private Class aClass;
    private String className;
    private Collection<CacheMethodDto> cacheMethodDtos;

    public ClassCacheDto(Class aClass,Collection<CacheMethodDto> cacheMethodDtos) {
        this.aClass = aClass;
        this.cacheMethodDtos = cacheMethodDtos;
    }
    public ClassCacheDto(Collection<CacheMethodDto> cacheMethodDtos) {
        this.cacheMethodDtos = cacheMethodDtos;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    public Collection<CacheMethodDto> getCacheMethodDtos() {
        return cacheMethodDtos;
    }

    public void setCacheMethodDtos(Collection<CacheMethodDto> cacheMethodDtos) {
        this.cacheMethodDtos = cacheMethodDtos;
    }

    public String getClassName() {
        if (Strings.isNullOrEmpty(className)) {
            if (aClass.getName().contains("$$")) {
                className = aClass.getName().substring(0, aClass.getName().indexOf("$$"));
            } else {
                className = aClass.getName();
            }
        }
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
