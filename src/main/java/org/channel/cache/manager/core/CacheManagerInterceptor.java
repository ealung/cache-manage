package org.channel.cache.manager.core;

import org.channel.cache.manager.dto.CacheMethodDto;
import org.channel.cache.manager.dto.ClassCacheDto;
import org.channel.cache.manager.storage.CacheStorage;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.interceptor.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.util.*;

/**
 * @author zhangchanglu
 * @since 2018/09/26 14:52.
 */
@Slf4j
public class CacheManagerInterceptor extends CacheInterceptor implements ApplicationListener<ContextRefreshedEvent> {
    private MultiValueMap<CacheOperation, NetEaseCacheOperationContext> contexts = null;
    private List<CacheManagerHandler> cacheManagerHandlers = new ArrayList<>();
    private boolean initialized=false;
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Object invoke = super.invoke(invocation);
        if(initialized) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(invocation.getThis());
            CacheOperationSource cacheOperationSource = getCacheOperationSource();
            if (null == cacheOperationSource) {
                return invoke;
            }
            Collection<CacheOperation> operations = cacheOperationSource.getCacheOperations(invocation.getMethod(), targetClass);
            if (!CollectionUtils.isEmpty(operations)) {
                contexts = new LinkedMultiValueMap<>(operations.size());
                for (CacheOperation op : operations) {
                    CacheOperationMetadata metadata = getCacheOperationMetadata(op, invocation.getMethod(), targetClass);
                    this.contexts.add(op, new NetEaseCacheOperationContext(metadata, invocation.getArguments(), invocation.getThis()));
                }
                executeHandlers();
            }
        }
        return invoke;
    }

    protected void executeHandlers() {
        for (CacheManagerHandler cacheManagerHandler : cacheManagerHandlers) {
            for (Map.Entry<CacheOperation, List<NetEaseCacheOperationContext>> classListEntry : contexts.entrySet()) {
                if (classListEntry.getKey().getClass().isAssignableFrom(CacheableOperation.class)) {
                    cacheManagerHandler.afterCacheAble(classListEntry.getValue());
                } else if (classListEntry.getKey().getClass().isAssignableFrom(CachePutOperation.class)) {
                    cacheManagerHandler.afterCachePut(classListEntry.getValue());
                } else if (classListEntry.getKey().getClass().isAssignableFrom(CacheEvictOperation.class)) {
                    cacheManagerHandler.afterCacheEvict(classListEntry.getValue());
                }
                cacheManagerHandler.afterCacheInvoker(classListEntry.getValue());
            }
        }
    }

    public class NetEaseCacheOperationContext extends CacheOperationContext {
        @Override
        public Collection<? extends Cache> getCaches() {
            return super.getCaches();
        }

        public Object getKey() {
            return generateKey(new Object());
        }

        public NetEaseCacheOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
            super(metadata, args, target);
        }
    }

    public void addHandler(CacheManagerHandler cacheManagerHandler) {
        cacheManagerHandlers.add(cacheManagerHandler);
    }

    /**
     * 部分情况下尚未加载完成，不能进行后续处理，这里重写用来暴露父类的initialized 标记
     */
    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();
        this.initialized = true;
    }
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        List<ClassCacheDto> cacheConfig = new ArrayList<>();
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, CacheStorage> cacheStorageMap = applicationContext.getBeansOfType(CacheStorage.class);
        if (cacheStorageMap.isEmpty()) {
            log.warn("can't load cacheStorage");
            return;
        }
        CacheStorage cacheStorage = cacheStorageMap.entrySet().iterator().next().getValue();
        Map<String, CacheManagerHandler> beansOfType = applicationContext.getBeansOfType(CacheManagerHandler.class);
        beansOfType.forEach((s, cacheManagerHandler) -> {
            cacheManagerHandler.setStorage(cacheStorage);
            addHandler(cacheManagerHandler);
        });
        AnnotationCacheOperationSource annotationCacheOperationSource = new AnnotationCacheOperationSource();
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String s : beanDefinitionNames) {
            Collection<CacheMethodDto> cacheMethodDtos=new ArrayList<>();
            final Object bean;
            try {
                bean = applicationContext.getBean(s);
            } catch (BeansException e) {
                log.warn("load bean - " + s);
                continue;
            }
            ReflectionUtils.doWithMethods(bean.getClass(), method -> {
                if (!method.toString().contains("$$")) {
                    Collection<CacheOperation> cacheOperations = annotationCacheOperationSource.getCacheOperations(method, bean.getClass());
                    if (!CollectionUtils.isEmpty(cacheOperations)) {
                        for (CacheOperation cacheOperation : cacheOperations) {
                            CacheMethodDto cacheMethodDto=new CacheMethodDto();
                            cacheMethodDto.setMethodName(method.getName());
                            cacheMethodDto.setCacheOperation(cacheOperation);
                            cacheMethodDtos.add(cacheMethodDto);
                        }
                    }
                }
            });
            if (!cacheMethodDtos.isEmpty()) {
                cacheConfig.add(new ClassCacheDto(bean.getClass(), cacheMethodDtos));
            }
        }
        cacheStorage.allCacheConfig(cacheConfig);
    }
}
