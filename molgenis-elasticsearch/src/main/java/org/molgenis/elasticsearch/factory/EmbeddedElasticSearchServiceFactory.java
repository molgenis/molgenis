package org.molgenis.elasticsearch.factory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.search.SearchServiceFactory;

/**
 * Factory for creating an embedded ElasticSearch server service. An elastic
 * search config file named 'elasticsearch.yml' must be on the classpath
 * 
 * @author erwin
 * 
 */
public class EmbeddedElasticSearchServiceFactory implements SearchServiceFactory
{
	private static final Logger LOG = Logger.getLogger(EmbeddedElasticSearchServiceFactory.class);
	private static final String CONFIG_FILE_NAME = "elasticsearch.yml";
	private static final String DEFAULT_INDEX_NAME = "molgenis";
	private Node node;
	private final String indexName;

	public EmbeddedElasticSearchServiceFactory()
	{
		this(DEFAULT_INDEX_NAME);

		Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath(CONFIG_FILE_NAME).build();
		System.out.println("DATAPATH:" + settings.get("path.data"));
		node = nodeBuilder().settings(settings).local(true).node();
		LOG.info("Embedded elasticsearch server started");
	}

	public EmbeddedElasticSearchServiceFactory(String indexName)
	{
		this.indexName = indexName;
	}

	@Override
	public ElasticSearchService create()
	{
		return new ElasticSearchService(node, indexName);
	}

	@Override
	public void close() throws IOException
	{
		node.close();
		LOG.info("Elastic searc server stopped");
	}

}
