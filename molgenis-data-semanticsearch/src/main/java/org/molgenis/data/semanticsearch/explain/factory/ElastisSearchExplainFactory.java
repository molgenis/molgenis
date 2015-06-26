package org.molgenis.data.semanticsearch.explain.factory;

import java.io.Closeable;
import java.io.IOException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainServiceImpl;
import org.molgenis.data.semanticsearch.explain.service.ExplainServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElastisSearchExplainFactory implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(ElastisSearchExplainFactory.class);

	private static final String CONFIG_FILE_NAME = "elasticsearch.yml";
	private static final String DEFAULT_INDEX_NAME = "molgenis";
	private final Client client;

	public ElastisSearchExplainFactory()
	{
		Builder builder = ImmutableSettings.settingsBuilder().loadFromClasspath(CONFIG_FILE_NAME);
		Settings settings = builder.build();
		client = new TransportClient(settings);
	}

	public ElasticSearchExplainService create(ExplainServiceHelper explainServiceHelper)
	{
		return new ElasticSearchExplainServiceImpl(client, DEFAULT_INDEX_NAME, explainServiceHelper);
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			client.close();
		}
		catch (Exception e)
		{
			LOG.error("Error closing client", e);
		}

		LOG.info("ElasticSearch Explain service stopped");
	}
}
