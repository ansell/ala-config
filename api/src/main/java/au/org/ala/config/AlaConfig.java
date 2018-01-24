/**
 * 
 */
package au.org.ala.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.AbsoluteNameLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Provides access to configuration information used by Biocache Store NG.
 * 
 * Wraps Commons Configuration to allow changes to the underlying format and
 * source easily.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class AlaConfig {

	/**
	 * This is the system property that is looked for to find the configuration
	 * location, if no other is specified.
	 */
	public static final String DEFAULT_SYSTEM_PROPERTY = "biocache.config";

	protected static final Logger logger = LoggerFactory.getLogger(AlaConfig.class);

	/**
	 * This is the location on the classpath that default properties are sourced
	 * from. Any properties that are set in the other files override these values.
	 */
	public static final String DEFAULTS_PROPERTIES = "/au/org/ala/biocache/store/api/biocache-defaults.properties";

	/**
	 * This is the location on either the classpath or file-system where the
	 * override properties can be sourced.
	 */
	public static final String OVERRIDES_PROPERTIES = "/data/biocache/config/biocache-config.properties";

	protected final ImmutableConfiguration immutableConfig;

	private volatile Injector internalInjector;

	/**
	 * Use static helper methods instead.
	 * 
	 * @param immutableConfig
	 *            The ImmutableConfiguration to use to find properties.
	 */
	protected AlaConfig(ImmutableConfiguration immutableConfig) {
		this.immutableConfig = immutableConfig;
	}

	/**
	 * Gets a configuration using the hardcoded defaults and override properties
	 * locations, checking the system property biocache.config to optionally
	 * override {@link #OVERRIDES_PROPERTIES}
	 * 
	 * @return An ImmutableConfiguration object that will use the override
	 *         properties were possible, but default to the defaults otherwise.
	 * @throws AlaConfigException
	 *             If there is an issue setting up the configuration.
	 */
	public static ImmutableConfiguration getConfig() throws AlaConfigException {
		Optional<String> overridesLocation = Optional.ofNullable(System.getProperty(DEFAULT_SYSTEM_PROPERTY));
		return getConfig(Paths.get(overridesLocation.orElse(OVERRIDES_PROPERTIES)));
	}

	/**
	 * Gets a configuration using the hardcoded defaults and the given override
	 * properties locations.
	 * 
	 * @param pathToOverrides
	 *            The path to the override properties locations
	 * @return An ImmutableConfiguration object that will use the override
	 *         properties were possible, but default to the defaults otherwise.
	 * @throws AlaConfigException
	 *             If there is an issue setting up the configuration.
	 */
	public static ImmutableConfiguration getConfig(Path pathToOverrides) throws AlaConfigException {
		return getConfig(pathToOverrides, Paths.get(DEFAULTS_PROPERTIES));
	}

	/**
	 * Gets a configuration using the given defaults and the given override
	 * properties locations.
	 * 
	 * @param pathToOverrides
	 *            The path to the override properties locations
	 * @param pathToDefaults
	 *            The path to the default properties locations
	 * @return An ImmutableConfiguration object that will use the override
	 *         properties were possible, but default to the defaults otherwise.
	 * @throws AlaConfigException
	 *             If there is an issue setting up the configuration.
	 */
	public static ImmutableConfiguration getConfig(Path pathToOverrides, Path pathToDefaults)
			throws AlaConfigException {
		try {
			List<ClassLoader> customClassLoaders = Arrays.asList(AlaConfig.class.getClassLoader());

			List<FileLocationStrategy> locationsOverrides = Arrays.asList(new AbsoluteNameLocationStrategy(),
					new FileSystemLocationStrategy(), new AbsoluteClasspathLocationStrategy(customClassLoaders));
			FileLocationStrategy locationStrategiesOverrides = new CombinedLocationStrategy(locationsOverrides);
			Parameters paramsOverride = new Parameters();
			FileBasedConfigurationBuilder<FileBasedConfiguration> builderOverride = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
					PropertiesConfiguration.class)
							.configure(paramsOverride.properties().setFileName(pathToOverrides.toString())

									.setThrowExceptionOnMissing(false).setEncoding(StandardCharsets.UTF_8.name())
									.setLocationStrategy(locationStrategiesOverrides));

			List<FileLocationStrategy> locationsDefaults = Arrays
					.asList(new AbsoluteClasspathLocationStrategy(customClassLoaders));
			FileLocationStrategy locationStrategiesDefaults = new CombinedLocationStrategy(locationsDefaults);
			Parameters paramsDefaults = new Parameters();
			FileBasedConfigurationBuilder<FileBasedConfiguration> builderDefaults = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
					PropertiesConfiguration.class)
							.configure(paramsDefaults.properties().setFileName(pathToDefaults.toString())

									.setThrowExceptionOnMissing(true).setEncoding(StandardCharsets.UTF_8.name())
									.setLocationStrategy(locationStrategiesDefaults));

			CompositeConfiguration combinedConfiguration = new CompositeConfiguration();
			// Important: Overrides must be added first
			combinedConfiguration.addConfiguration(builderOverride.getConfiguration());
			combinedConfiguration.addConfiguration(builderDefaults.getConfiguration());

			// return new
			// AlaConfig(ConfigurationUtils.unmodifiableConfiguration(combinedConfiguration));
			return ConfigurationUtils.unmodifiableConfiguration(combinedConfiguration);
		} catch (ConfigurationException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as a boolean.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be found in the configuration or
	 *             represented as a boolean
	 */
	public boolean getBoolean(String propertyName) throws AlaConfigException {
		try {
			return immutableConfig.getBoolean(propertyName);
		} catch (ConversionException | NoSuchElementException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as a boolean, or use a default value if it isn't
	 * present.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @param defaultValue
	 *            The default value to use if the property doesn't have a value
	 *            assigned to it
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be represented as a boolean
	 */
	public boolean getBooleanOrDefault(String propertyName, boolean defaultValue)
			throws ConversionException, NoSuchElementException {
		try {
			return immutableConfig.getBoolean(propertyName, defaultValue);
		} catch (ConversionException | NoSuchElementException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as a string.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be found in the configuration
	 */
	public String get(String propertyName) throws AlaConfigException {
		try {
			return immutableConfig.getString(propertyName);
		} catch (ConversionException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as a string, or use a default value if it isn't
	 * present.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @param defaultValue
	 *            The default to use if the property isn't present
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property found in the configuration was not able to be
	 *             represented as a String.
	 */
	public String getOrDefault(String propertyName, String defaultValue) throws AlaConfigException {
		try {
			return immutableConfig.getString(propertyName, defaultValue);
		} catch (ConversionException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as a string, or use a default value if it isn't
	 * present.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @param defaultValue
	 *            The default to use if the property isn't present
	 * @param splitChar
	 *            The character to split the property String on to convert it to a
	 *            set of values.
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be found in the configuration
	 */
	public Set<String> getSetOrDefault(String propertyName, String defaultValue, String splitChar)
			throws AlaConfigException {
		try {
			Set<String> splitList = Arrays
					.asList(immutableConfig.getString(propertyName, defaultValue).split(splitChar)).stream()
					.map(String::trim).collect(Collectors.toSet());
			if (splitList.isEmpty() || (splitList.size() == 1 && splitList.iterator().next().isEmpty())) {
				return Collections.emptySet();
			} else {
				return Collections.unmodifiableSet(splitList);
			}
		} catch (ConversionException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as an int.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be found in the configuration or
	 *             represented as an int
	 */
	public int getInt(String propertyName) throws AlaConfigException {
		try {
			return immutableConfig.getInt(propertyName);
		} catch (ConversionException | NoSuchElementException e) {
			throw new AlaConfigException(e);
		}
	}

	/**
	 * Get a config property as an int, or use a default value if it isn't present.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @param defaultValue
	 *            The default value to use if the property doesn't have a value
	 *            assigned to it
	 * @return The value for the configuration property
	 * @throws AlaConfigException
	 *             If the property cannot be found in the configuration or
	 *             represented as an int
	 */
	public int getIntOrDefault(String propertyName, int defaultValue) throws AlaConfigException {
		try {
			return immutableConfig.getInt(propertyName, defaultValue);
		} catch (ConversionException | NoSuchElementException e) {
			throw new AlaConfigException(e);
		}
	}

	protected org.slf4j.Logger logger() {
		return logger;
	}

	/**
	 * Do not call this method yourself. Called exactly once when creating the
	 * instance of Injector in {@link #injector()}. <br>
	 * Call {@link #getInstance(Class)} to retrieve instances from the Injector as
	 * necessary.
	 * 
	 * @return Returns a new instance of {@link AbstractModule} setup for this
	 *         configuration instance.
	 */
	protected abstract AbstractModule getNewModule();

	// {
	// return new AbstractModule() {
	//
	// @Override
	// protected void configure() {
	// Names.bindProperties(binder(),
	// ConfigurationConverter.getProperties(immutableConfig));
	// }
	// };
	// }

	private Injector injector() {
		Injector result = internalInjector;
		if (result == null) {
			synchronized (this) {
				result = internalInjector;
				if (result == null) {
					result = internalInjector = Guice.createInjector(getNewModule());
				}
			}
		}
		return result;
	}

	public <T> T getInstance(java.lang.Class<T> nextClass) {
		return injector().getInstance(nextClass);
	}

	/**
	 * Get a config property as a string.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 */
	public String getProperty(String propertyName) {
		return get(propertyName);
	}

	public void outputConfig(Writer outputWriter) throws IOException {
		ConfigurationConverter.getProperties(immutableConfig).store(outputWriter,
				"ala-config configuration dump at: " + DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()));
	}

	public java.util.Properties versionProperties() {
		return null;
	}

	public String tmpWorkDir() {
		return getOrDefault("tmp.work.dir", "/tmp");
	}

}
