/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.autoconfigure.web;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.convert.DefaultDurationUnit;
import org.springframework.http.CacheControl;

/**
 * Properties used to configure resource handling.
 *
 * @author Phillip Webb
 * @author Brian Clozel
 * @author Dave Syer
 * @author Venil Noronha
 * @author Kristine Jetzke
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "spring.resources", ignoreUnknownFields = false)
public class ResourceProperties {

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/resources/", "classpath:/resources/",
			"classpath:/static/", "classpath:/public/" };

	/**
	 * Locations of static resources. Defaults to classpath:[/META-INF/resources/,
	 * /resources/, /static/, /public/].
	 */
	private String[] staticLocations = CLASSPATH_RESOURCE_LOCATIONS;

	/**
	 * Cache period for the resources served by the resource handler. If a duration suffix
	 * is not specified, seconds will be used. Can be overridden by the 'cache-control'
	 * property.
	 */
	@DefaultDurationUnit(ChronoUnit.SECONDS)
	private Duration cachePeriod;

	/**
	 * Cache control HTTP headers, only allows valid directive combinations. Overrides the
	 * 'cache-period' property.
	 */
	private CacheControlProperties cacheControl = new CacheControlProperties();

	/**
	 * Enable default resource handling.
	 */
	private boolean addMappings = true;

	private final Chain chain = new Chain();

	public String[] getStaticLocations() {
		return this.staticLocations;
	}

	public void setStaticLocations(String[] staticLocations) {
		this.staticLocations = appendSlashIfNecessary(staticLocations);
	}

	private String[] appendSlashIfNecessary(String[] staticLocations) {
		String[] normalized = new String[staticLocations.length];
		for (int i = 0; i < staticLocations.length; i++) {
			String location = staticLocations[i];
			normalized[i] = (location.endsWith("/") ? location : location + "/");
		}
		return normalized;
	}

	public Duration getCachePeriod() {
		return this.cachePeriod;
	}

	public void setCachePeriod(Duration cachePeriod) {
		this.cachePeriod = cachePeriod;
	}

	public CacheControlProperties getCacheControl() {
		return this.cacheControl;
	}

	public void setCacheControl(CacheControlProperties cacheControl) {
		this.cacheControl = cacheControl;
	}

	public boolean isAddMappings() {
		return this.addMappings;
	}

	public void setAddMappings(boolean addMappings) {
		this.addMappings = addMappings;
	}

	public Chain getChain() {
		return this.chain;
	}

	/**
	 * Configuration for the Spring Resource Handling chain.
	 */
	public static class Chain {

		/**
		 * Enable the Spring Resource Handling chain. Disabled by default unless at least
		 * one strategy has been enabled.
		 */
		private Boolean enabled;

		/**
		 * Enable caching in the Resource chain.
		 */
		private boolean cache = true;

		/**
		 * Enable HTML5 application cache manifest rewriting.
		 */
		private boolean htmlApplicationCache = false;

		/**
		 * Enable resolution of already gzipped resources. Checks for a resource name
		 * variant with the "*.gz" extension.
		 */
		private boolean gzipped = false;

		private final Strategy strategy = new Strategy();

		/**
		 * Return whether the resource chain is enabled. Return {@code null} if no
		 * specific settings are present.
		 * @return whether the resource chain is enabled or {@code null} if no specified
		 * settings are present.
		 */
		public Boolean getEnabled() {
			return getEnabled(getStrategy().getFixed().isEnabled(),
					getStrategy().getContent().isEnabled(), this.enabled);
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isCache() {
			return this.cache;
		}

		public void setCache(boolean cache) {
			this.cache = cache;
		}

		public Strategy getStrategy() {
			return this.strategy;
		}

		public boolean isHtmlApplicationCache() {
			return this.htmlApplicationCache;
		}

		public void setHtmlApplicationCache(boolean htmlApplicationCache) {
			this.htmlApplicationCache = htmlApplicationCache;
		}

		public boolean isGzipped() {
			return this.gzipped;
		}

		public void setGzipped(boolean gzipped) {
			this.gzipped = gzipped;
		}

		static Boolean getEnabled(boolean fixedEnabled, boolean contentEnabled,
				Boolean chainEnabled) {
			return (fixedEnabled || contentEnabled ? Boolean.TRUE : chainEnabled);
		}

	}

	/**
	 * Strategies for extracting and embedding a resource version in its URL path.
	 */
	public static class Strategy {

		private final Fixed fixed = new Fixed();

		private final Content content = new Content();

		public Fixed getFixed() {
			return this.fixed;
		}

		public Content getContent() {
			return this.content;
		}

	}

	/**
	 * Version Strategy based on content hashing.
	 */
	public static class Content {

		/**
		 * Enable the content Version Strategy.
		 */
		private boolean enabled;

		/**
		 * Comma-separated list of patterns to apply to the Version Strategy.
		 */
		private String[] paths = new String[] { "/**" };

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String[] getPaths() {
			return this.paths;
		}

		public void setPaths(String[] paths) {
			this.paths = paths;
		}

	}

	/**
	 * Version Strategy based on a fixed version string.
	 */
	public static class Fixed {

		/**
		 * Enable the fixed Version Strategy.
		 */
		private boolean enabled;

		/**
		 * Comma-separated list of patterns to apply to the Version Strategy.
		 */
		private String[] paths = new String[] { "/**" };

		/**
		 * Version string to use for the Version Strategy.
		 */
		private String version;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String[] getPaths() {
			return this.paths;
		}

		public void setPaths(String[] paths) {
			this.paths = paths;
		}

		public String getVersion() {
			return this.version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

	}

	/**
	 * Configuration for the Cache Control HTTP header.
	 */
	public static class CacheControlProperties {

		/**
		 * Maximum time the response should be cached, in seconds if no duration suffix is
		 * not specified.
		 */
		@DefaultDurationUnit(ChronoUnit.SECONDS)
		private Duration maxAge;

		/**
		 * Indicate that the cached response can be reused only if re-validated with the
		 * server.
		 */
		private Boolean noCache;

		/**
		 * Indicate to not cache the response in any case.
		 */
		private Boolean noStore;

		/**
		 * Indicate that once it has become stale, a cache must not use the response
		 * without re-validating it with the server.
		 */
		private Boolean mustRevalidate;

		/**
		 * Indicate intermediaries (caches and others) that they should not transform the
		 * response content.
		 */
		private Boolean noTransform;

		/**
		 * Indicate that any cache may store the response.
		 */
		private Boolean cachePublic;

		/**
		 * Indicate that the response message is intended for a single user and must not
		 * be stored by a shared cache.
		 */
		private Boolean cachePrivate;

		/**
		 * Same meaning as the "must-revalidate" directive, except that it does not apply
		 * to private caches.
		 */
		private Boolean proxyRevalidate;

		/**
		 * Maximum time the response can be served after it becomes stale, in seconds if
		 * no duration suffix is not specified.
		 */
		@DefaultDurationUnit(ChronoUnit.SECONDS)
		private Duration staleWhileRevalidate;

		/**
		 * Maximum time the response may be used when errors are encountered, in seconds
		 * if no duration suffix is not specified.
		 */
		@DefaultDurationUnit(ChronoUnit.SECONDS)
		private Duration staleIfError;

		/**
		 * Maximum time the response should be cached by shared caches, in seconds if no
		 * duration suffix is not specified.
		 */
		@DefaultDurationUnit(ChronoUnit.SECONDS)
		private Duration sMaxAge;

		public Duration getMaxAge() {
			return this.maxAge;
		}

		public void setMaxAge(Duration maxAge) {
			this.maxAge = maxAge;
		}

		public Boolean getNoCache() {
			return this.noCache;
		}

		public void setNoCache(Boolean noCache) {
			this.noCache = noCache;
		}

		public Boolean getNoStore() {
			return this.noStore;
		}

		public void setNoStore(Boolean noStore) {
			this.noStore = noStore;
		}

		public Boolean getMustRevalidate() {
			return this.mustRevalidate;
		}

		public void setMustRevalidate(Boolean mustRevalidate) {
			this.mustRevalidate = mustRevalidate;
		}

		public Boolean getNoTransform() {
			return this.noTransform;
		}

		public void setNoTransform(Boolean noTransform) {
			this.noTransform = noTransform;
		}

		public Boolean getCachePublic() {
			return this.cachePublic;
		}

		public void setCachePublic(Boolean cachePublic) {
			this.cachePublic = cachePublic;
		}

		public Boolean getCachePrivate() {
			return this.cachePrivate;
		}

		public void setCachePrivate(Boolean cachePrivate) {
			this.cachePrivate = cachePrivate;
		}

		public Boolean getProxyRevalidate() {
			return this.proxyRevalidate;
		}

		public void setProxyRevalidate(Boolean proxyRevalidate) {
			this.proxyRevalidate = proxyRevalidate;
		}

		public Duration getStaleWhileRevalidate() {
			return this.staleWhileRevalidate;
		}

		public void setStaleWhileRevalidate(Duration staleWhileRevalidate) {
			this.staleWhileRevalidate = staleWhileRevalidate;
		}

		public Duration getStaleIfError() {
			return this.staleIfError;
		}

		public void setStaleIfError(Duration staleIfError) {
			this.staleIfError = staleIfError;
		}

		public Duration getsMaxAge() {
			return this.sMaxAge;
		}

		public void setsMaxAge(Duration sMaxAge) {
			this.sMaxAge = sMaxAge;
		}

		public CacheControl toHttpCacheControl() {
			CacheControl cacheControl = createCacheControl();
			callIfTrue(this.mustRevalidate, cacheControl, CacheControl::mustRevalidate);
			callIfTrue(this.noTransform, cacheControl, CacheControl::noTransform);
			callIfTrue(this.cachePublic, cacheControl, CacheControl::cachePublic);
			callIfTrue(this.cachePrivate, cacheControl, CacheControl::cachePrivate);
			callIfTrue(this.proxyRevalidate, cacheControl, CacheControl::proxyRevalidate);
			if (this.staleWhileRevalidate != null) {
				cacheControl.staleWhileRevalidate(this.staleWhileRevalidate.getSeconds(),
						TimeUnit.SECONDS);
			}
			if (this.staleIfError != null) {
				cacheControl.staleIfError(this.staleIfError.getSeconds(),
						TimeUnit.SECONDS);
			}
			if (this.sMaxAge != null) {
				cacheControl.sMaxAge(this.sMaxAge.getSeconds(), TimeUnit.SECONDS);
			}
			return cacheControl;
		}

		private CacheControl createCacheControl() {
			if (Boolean.TRUE.equals(this.noStore)) {
				return CacheControl.noStore();
			}
			if (Boolean.TRUE.equals(this.noCache)) {
				return CacheControl.noCache();
			}
			if (this.maxAge != null) {
				return CacheControl.maxAge(this.maxAge.getSeconds(), TimeUnit.SECONDS);
			}
			return CacheControl.empty();
		}

		private <T> void callIfTrue(Boolean property, T instance, Consumer<T> call) {
			if (Boolean.TRUE.equals(property)) {
				call.accept(instance);
			}
		}

	}

}
