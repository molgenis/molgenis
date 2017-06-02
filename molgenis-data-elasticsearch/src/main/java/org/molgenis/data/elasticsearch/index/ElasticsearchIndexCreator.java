package org.molgenis.data.elasticsearch.index;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Creates ElasticSearch indices.
 */
public class ElasticsearchIndexCreator
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchIndexCreator.class);

	public static final String DEFAULT_ANALYZER = "default";
	public static final String NGRAM_ANALYZER = "ngram_analyzer";
	private static final String NGRAM_TOKENIZER = "ngram_tokenizer";
	private static final String DEFAULT_TOKENIZER = "default_tokenizer";
	private static final String DEFAULT_STEMMER = "default_stemmer";

	private final Client client;
	private ElasticsearchUtils elasticsearchUtils;

	public ElasticsearchIndexCreator(Client client)
	{
		this.client = client;
		elasticsearchUtils = new ElasticsearchUtils(client);
	}

	public void createIndexIfNotExists(String indexName)
	{
		try
		{
			// Wait until elasticsearch is ready
			elasticsearchUtils.waitForYellowStatus();

			if (!elasticsearchUtils.indexExists(indexName))
			{
				createIndexInternal(indexName);
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void createIndexInternal(String indexName) throws IOException
	{
		if (LOG.isTraceEnabled()) LOG.trace("Creating Elasticsearch index [" + indexName + "] ...");
		Settings.Builder settings = Settings.builder().loadFromSource(
				XContentFactory.jsonBuilder().startObject().field("index.mapper.dynamic", false)
						.field("index.number_of_shards", 1).field("index.number_of_replicas", 0)
						.field("index.mapping.total_fields.limit", Long.MAX_VALUE).startObject("analysis")
						.startObject("analyzer").startObject(DEFAULT_ANALYZER).field("type", "custom")
						.field("filter", new String[] { "lowercase", DEFAULT_STEMMER })
						.field("tokenizer", DEFAULT_TOKENIZER).field("char_filter", "html_strip").endObject()
						.startObject(NGRAM_ANALYZER).field("type", "custom")
						.field("filter", new String[] { "lowercase" }).field("tokenizer", NGRAM_TOKENIZER).endObject()
						.endObject().startObject("filter").startObject(DEFAULT_STEMMER).field("type", "stemmer")
						.field("name", "english").endObject().endObject().startObject("tokenizer")
						.startObject(DEFAULT_TOKENIZER).field("type", "pattern").field("pattern", "([^a-zA-Z0-9]+)")
						.endObject().startObject(NGRAM_TOKENIZER).field("type", "nGram").field("min_gram", 1)
						.field("max_gram", 10).endObject().endObject().endObject().endObject().string(),
				XContentType.JSON);

		CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).setSettings(settings).execute()
				.actionGet();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException("Creation of index [" + indexName + "] failed. Response=" + response);
		}

		elasticsearchUtils.waitForYellowStatus();

		LOG.debug("Created Elasticsearch index [" + indexName + "]");
	}
}
