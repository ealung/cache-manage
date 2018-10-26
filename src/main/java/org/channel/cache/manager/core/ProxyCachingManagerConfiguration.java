/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.channel.cache.manager.core;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans necessary
 * to enable proxy-based annotation-driven cache management.
 *
 * @author Chris Beams
 * @see CachingManagerConfigurationSelector
 * @since 3.1
 */
@Configuration
public class ProxyCachingManagerConfiguration extends AbstractCachingConfiguration {

    @Bean(name = CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryCacheOperationSourceAdvisor cacheAdvisor() {
        BeanFactoryCacheOperationSourceAdvisor advisor =
                new BeanFactoryCacheOperationSourceAdvisor();
        advisor.setCacheOperationSource(cacheOperationSource());
        advisor.setAdvice(cacheInterceptor());
        advisor.setOrder(this.enableCaching.<Integer>getNumber("order"));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
        return new AnnotationCacheOperationSource();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheInterceptor cacheInterceptor() {
        CacheManagerInterceptor interceptor = new CacheManagerInterceptor();
        interceptor.setCacheOperationSources(cacheOperationSource());
        if (this.cacheResolver != null) {
            interceptor.setCacheResolver(this.cacheResolver);
        } else if (this.cacheManager != null) {
            interceptor.setCacheManager(this.cacheManager);
        }
        if (this.keyGenerator != null) {
            interceptor.setKeyGenerator(this.keyGenerator);
        }
        if (this.errorHandler != null) {
            interceptor.setErrorHandler(this.errorHandler);
        }
        return interceptor;
    }
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableCaching = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCachingManager.class.getName(), false));
        if (this.enableCaching == null) {
            throw new IllegalArgumentException(
                    "@EnableCachingManager is not present on importing class " + importMetadata.getClassName());
        }
    }
}
