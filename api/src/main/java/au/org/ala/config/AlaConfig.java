/**
 * 
 */
package au.org.ala.config;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.AbsoluteNameLocationStrategy;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Provides access to configuration information used by Biocache Store NG.
 * 
 * Wraps Commons Configuration to allow changes to the underlying format and
 * source easily.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AlaConfig {

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

	private final ImmutableConfiguration immutableConfig;

	private volatile AbstractModule internalConfigModule;

	private volatile Injector internalInjector;

	/**
	 * Use static helper methods instead.
	 */
	private AlaConfig(ImmutableConfiguration immutableConfig) {
		this.immutableConfig = immutableConfig;
	}

	/**
	 * Gets a configuration using the hardcoded defaults and override properties
	 * locations, checking the system property biocache.config to optionally
	 * override {@link #OVERRIDES_PROPERTIES}
	 * 
	 * @return A Config object that will use the override properties were possible,
	 *         but default to the defaults otherwise.
	 * @throws ConfigurationException
	 *             If there is an issue setting up the configuration.
	 */
	public static AlaConfig getConfig() throws ConfigurationException {
		Optional<String> overridesLocation = Optional.ofNullable(System.getProperty("biocache.config"));
		return getConfig(Paths.get(overridesLocation.orElse(OVERRIDES_PROPERTIES)));
	}

	/**
	 * Gets a configuration using the hardcoded defaults and the given override
	 * properties locations.
	 * 
	 * @param pathToOverrides
	 *            The path to the override properties locations
	 * @return A Config object that will use the override properties were possible,
	 *         but default to the defaults otherwise.
	 * @throws ConfigurationException
	 *             If there is an issue setting up the configuration.
	 */
	public static AlaConfig getConfig(Path pathToOverrides) throws ConfigurationException {
		return getConfig(pathToOverrides, Paths.get(DEFAULTS_PROPERTIES));
	}

	public static AlaConfig getConfig(Path pathToOverrides, Path pathToDefaults) throws ConfigurationException {

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

		return new AlaConfig(ConfigurationUtils.unmodifiableConfiguration(combinedConfiguration));
	}

	/**
	 * Get a config property as a boolean.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 * @throws ConversionException
	 *             If the property cannot be represented as a boolean
	 * @throws NoSuchElementException
	 *             If the property cannot be found in the configuration
	 */
	public boolean getBoolean(String propertyName) throws ConversionException, NoSuchElementException {
		return immutableConfig.getBoolean(propertyName);
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
	 * @throws ConversionException
	 *             If the property cannot be represented as a boolean
	 */
	public boolean getBooleanOrDefault(String propertyName, boolean defaultValue)
			throws ConversionException, NoSuchElementException {
		return immutableConfig.getBoolean(propertyName, defaultValue);
	}

	/**
	 * Get a config property as a string.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 */
	public String get(String propertyName) throws ConversionException {
		return immutableConfig.getString(propertyName);
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
	 */
	public String getOrDefault(String propertyName, String defaultValue) throws ConversionException {
		return immutableConfig.getString(propertyName, defaultValue);
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
	 */
	public Set<String> getSetOrDefault(String propertyName, String defaultValue, String splitChar)
			throws ConversionException {
		Set<String> splitList = Arrays.asList(immutableConfig.getString(propertyName, defaultValue).split(splitChar))
				.stream().map(String::trim).collect(Collectors.toSet());
		if (splitList.isEmpty() || (splitList.size() == 1 && splitList.iterator().next().isEmpty())) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(splitList);
		}
	}

	/**
	 * Get a config property as an int.
	 * 
	 * @param propertyName
	 *            The configuration property to find
	 * @return The value for the configuration property
	 * @throws ConversionException
	 *             If the property cannot be represented as an int
	 * @throws NoSuchElementException
	 *             If the property cannot be found in the configuration
	 */
	public int getInt(String propertyName) throws ConversionException, NoSuchElementException {
		return immutableConfig.getInt(propertyName);
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
	 * @throws ConversionException
	 *             If the property cannot be represented as an int
	 */
	public int getIntOrDefault(String propertyName, int defaultValue)
			throws ConversionException, NoSuchElementException {
		return immutableConfig.getInt(propertyName, defaultValue);
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
	protected AbstractModule getNewModule() {
		return new AbstractModule() {

			@Override
			protected void configure() {
				Names.bindProperties(binder(), ConfigurationConverter.getProperties(immutableConfig));
			}
		};
	}

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

	public String remoteMediaStoreUrl() {
		return getOrDefault("media.store.url", "");
	}

	public boolean hashImageFileNames() {
		return getBooleanOrDefault("hash.image.filenames", false);
	}

	public int solrUpdateThreads() {
		return getIntOrDefault("solr.update.threads", 4);
	}

	public int cassandraUpdateThreads() {
		return getIntOrDefault("cassandra.update.threads", 8);
	}

	public String volunteerHubUid() {
		return getOrDefault("volunteer.hub.uid", "");
	}

	public String collectoryApiKey() {
		return getOrDefault("registry.api.key", "xxxxxxxxxxxxxxxxx");
	}

	public String loadFileStore() {
		return getOrDefault("load.dir", "/data/biocache-load/");
	}

	public String vocabDirectory() {
		return getOrDefault("vocab.dir", "/data/biocache/vocab/");
	}

	public String layersDirectory() {
		return getOrDefault("layers.dir", "/data/biocache/layers/");
	}

	public String deletedFileStore() {
		return getOrDefault("deleted.file.store", "/data/biocache-delete/");
	}

	public String mediaFileStore() {
		return getOrDefault("media.dir", "/data/biocache-media/");
	}

	public String mediaBaseUrl() {
		return getOrDefault("media.url", "http://biocache.ala.org.au/biocache-media");
	}

	public String excludeSensitiveValuesFor() {
		return getOrDefault("exclude.sensitive.values", "");
	}

	public String allowCollectoryUpdates() {
		return getOrDefault("allow.registry.updates", "false");
	}

	public String extraMiscFields() {
		return getOrDefault("extra.misc.fields", "");
	}

	public String technicalContact() {
		return getOrDefault("technical.contact", "support@ala.org.au");
	}

	public String irmngDwcArchiveUrl() {
		return getOrDefault("irmng.archive.url", "http://www.cmar.csiro.au/datacentre/downloads/IRMNG_DWC.zip");
	}

	public boolean obeySDSIsLoadable() {
		return getBooleanOrDefault("obey.sds.is.loadable", true);
	}

	public String nationalChecklistIdentifierPattern() {
		return getOrDefault("national.checklist.guid.pattern", "biodiversity.org.au");
	}

	public List<String> blacklistedMediaUrls() {
		return null;
	}

	public String speciesSubgroupsUrl() {
		return getOrDefault("species.subgroups.url", "http://bie.ala.org.au/subgroups.json");
	}

	public String listToolUrl() {
		return getOrDefault("list.tool.url", "http://lists.ala.org.au");
	}

	public String volunteerUrl() {
		return getOrDefault("volunteer.url", "http://volunteer.ala.org.au");
	}

	public String tmpWorkDir() {
		return getOrDefault("tmp.work.dir", "/tmp");
	}

	public String registryUrl() {
		return getOrDefault("registry.url", "http://collections.ala.org.au/ws");
	}

	public String persistPointsFile() {
		return getOrDefault("persist.points.file", "");
	}

	public String flickrUsersUrl() {
		return getOrDefault("flickr.users.url", "http://auth.ala.org.au/userdetails/external/flickr");
	}

	public String reindexUrl() {
		return get("reindex.url");
	}

	public String reindexData() {
		return get("reindex.data");
	}

	public String reindexViewDataResourceUrl() {
		return get("reindex.data.resource.url");
	}

	public String layersServiceUrl() {
		return get("layers.service.url");
	}

	public boolean layersServiceSampling() {
		return getBooleanOrDefault("layers.service.sampling", true);
	}

	public int layerServiceRetries() {
		return getIntOrDefault("layers.service.retries", 10);
	}

	public String biocacheServiceUrl() {
		return getOrDefault("webservices.root", "http://biocache.ala.org.au/ws");
	}

	public int solrBatchSize() {
		return getIntOrDefault("solr.batch.size", 1000);
	}

	public int solrHardCommitSize() {
		return getIntOrDefault("solr.hardcommit.size", 10000);
	}

	public Set<String> stateProvincePrefixFields() {
		return getSetOrDefault("species.list.prefix", "stateProvince", ",");
	}

	public Set<String> speciesListIndexValues() {
		return getSetOrDefault("species.list.index.keys", "category,status,sourceStatus", ",");
	}

	public boolean loadSpeciesLists() {
		return getBooleanOrDefault("include.species.lists", false);
	}

	public boolean taxonProfilesEnabled() {
		return getBooleanOrDefault("taxon.profiles.enabled", true);
	}

	public String localNodeIp() {
		return getOrDefault("local.node.ip", "127.0.0.1");
	}

	public String zookeeperAddress() {
		return getOrDefault("zookeeper.address", "127.0.0.1:2181");
	}

	public boolean zookeeperUpdatesEnabled() {
		return getBooleanOrDefault("zookeeper.updates.enabled", false);
	}

	public int nodeNumber() {
		return getIntOrDefault("node.number", 0);
	}

	public int cassandraTokenSplit() {
		return getIntOrDefault("cassandra.token.split", 1);
	}

	public int clusterSize() {
		return getIntOrDefault("cluster.size", 1);
	}

	/**
	 * 
	 * @param propertyName
	 * @return
	 * @deprecated Use {@link #get(String)} instead.
	 */
	@Deprecated
	public String getProperty(String propertyName) {
		return get(propertyName);
	}

	public void outputConfig(Writer outputWriter) throws IOException {
		ConfigurationConverter.getProperties(immutableConfig).store(outputWriter, "biocache-store-ng configuration");
	}

	public String stateProvinceLayerID() {
		return getOrDefault("layer.state.province", "cl927");
	}

	public String terrestrialLayerID() {
		return getOrDefault("layer.terrestrial", "cl1048");
	}

	public String marineLayerID() {
		return getOrDefault("layer.marine", "cl21");
	}

	public String countriesLayerID() {
		return getOrDefault("layer.countries", "cl932");
	}

	public String localGovLayerID() {
		return getOrDefault("layer.localgov", "cl959");
	}

	public boolean gridRefIndexingEnabled() {
		return getBooleanOrDefault("gridref.indexing.enabled", false);
	}

	public String defaultCountry() {
		return getOrDefault("default.country", "Australia");
	}

	public java.util.Properties versionProperties() {
		return null;
	}

	public Set<String> additionalFieldsToIndex() {
		return getSetOrDefault("additional.fields.to.index", "", ",");
	}

	public String sdsUrl() {
		return getOrDefault("sds.url", "http://sds.ala.org.au");
	}

	public boolean sdsEnabled() {
		return getBooleanOrDefault("sds.enabled", true);
	}

	public Set<String> sensitiveFields() {
		return getSetOrDefault("sensitive.field",
				"originalSensitiveValues,originalDecimalLatitude,originalDecimalLongitude,originalLocationRemarks,originalVerbatimLatitude,originalVerbatimLongitude",
				",");
	}

	public String exportIndexAsCsvPath() {
		return getOrDefault("export.index.as.csv.path", "");
	}

	public String exportIndexAsCsvPathSensitive() {
		return getOrDefault("export.index.as.csv.path.sensitive", "");
	}

}
