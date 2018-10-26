package org.channel.cache.manager.core;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

public class CachingManagerConfigurationSelector extends AdviceModeImportSelector<EnableCaching> {

	private static final String PROXY_JCACHE_CONFIGURATION_CLASS =
			"org.springframework.cache.jcache.config.ProxyJCacheConfiguration";

	private static final String CACHE_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.cache.aspectj.AspectJCachingConfiguration";

	private static final String JCACHE_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.cache.aspectj.AspectJJCacheConfiguration";


	private static final boolean jsr107Present = ClassUtils.isPresent(
			"javax.cache.Cache", CachingManagerConfigurationSelector.class.getClassLoader());

	private static final boolean jcacheImplPresent = ClassUtils.isPresent(
			PROXY_JCACHE_CONFIGURATION_CLASS, CachingManagerConfigurationSelector.class.getClassLoader());

	@Override
	public String[] selectImports(AdviceMode adviceMode) {
		switch (adviceMode) {
			case PROXY:
				return getProxyImports();
			case ASPECTJ:
				return getAspectJImports();
			default:
				return null;
		}
	}

	/**
	 * Return the imports to use if the {@link AdviceMode} is set to {@link AdviceMode#PROXY}.
	 * <p>Take care of adding the necessary JSR-107 import if it is available.
	 */
	private String[] getProxyImports() {
		List<String> result = new ArrayList<String>();
		result.add(AutoProxyRegistrar.class.getName());
		result.add(ProxyCachingManagerConfiguration.class.getName());
		if (jsr107Present && jcacheImplPresent) {
			result.add(PROXY_JCACHE_CONFIGURATION_CLASS);
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Return the imports to use if the {@link AdviceMode} is set to {@link AdviceMode#ASPECTJ}.
	 * <p>Take care of adding the necessary JSR-107 import if it is available.
	 */
	private String[] getAspectJImports() {
		List<String> result = new ArrayList<String>();
		result.add(CACHE_ASPECT_CONFIGURATION_CLASS_NAME);
		if (jsr107Present && jcacheImplPresent) {
			result.add(JCACHE_ASPECT_CONFIGURATION_CLASS_NAME);
		}
		return result.toArray(new String[result.size()]);
	}

}
