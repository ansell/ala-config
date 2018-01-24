package au.org.ala.config;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to look up the entire path and filename on the classpath.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
class AbsoluteClasspathLocationStrategy implements FileLocationStrategy {
	private static final Logger log = LoggerFactory.getLogger(AbsoluteClasspathLocationStrategy.class);
	private final List<ClassLoader> customClassLoaders;

	public AbsoluteClasspathLocationStrategy() {
		this.customClassLoaders = Collections.emptyList();
	}

	public AbsoluteClasspathLocationStrategy(List<ClassLoader> customClassLoaders) {
		this.customClassLoaders = customClassLoaders;
	}

	@Override
	public URL locate(FileSystem fileSystem, FileLocator locator) {
		return locateFromClasspath(locator.getFileName());
	}

	URL locateFromClasspath(String resourceName) {
		for (ClassLoader cl : customClassLoaders) {
			URL nextUrl = cl.getResource(resourceName);
			if (nextUrl != null) {
				return nextUrl;
			}
		}

		URL url = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader != null) {
			url = loader.getResource(resourceName);

			if (url != null && log.isDebugEnabled()) {
				log.debug("Loading configuration from the context classpath (" + resourceName + ")");
			}
		}

		if (url == null) {
			url = ClassLoader.getSystemResource(resourceName);

			if (url != null && log.isDebugEnabled()) {
				log.debug("Loading configuration from the system classpath (" + resourceName + ")");
			}
		}

		if (url == null) {
			url = AbsoluteClasspathLocationStrategy.class.getResource(resourceName);

			if (url != null && log.isDebugEnabled()) {
				log.debug("Loading configuration from the AbsoluteClasspathLocationStrategy classpath (" + resourceName
						+ ")");
			}
		}

		return url;
	}
}
