/**
 * 
 */
package au.org.ala.config.biocache;

import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.ImmutableConfiguration;

import com.google.inject.AbstractModule;

import au.org.ala.config.AlaConfig;

/**
 * The class for instances of {@link AlaConfig} that are used by Biocache
 * applications.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class BiocacheConfig extends AlaConfig {

	/**
	 * Create a new instance of BiocacheConfig using the default locations to
	 * discover the configuration properties.
	 */
	public BiocacheConfig() {
		this(getConfig());
	}

	/**
	 * Create a new instance of BiocacheConfig using the given configuration
	 * properties.
	 * 
	 * @param config
	 *            The configuration properties to use for this instance of
	 *            BiocacheConfig
	 */
	public BiocacheConfig(ImmutableConfiguration config) {
		super(config);
	}

	@Override
	protected AbstractModule getNewModule() {
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
		throw new UnsupportedOperationException("TODO: Implement BiocacheConfig.getNewModule");
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
