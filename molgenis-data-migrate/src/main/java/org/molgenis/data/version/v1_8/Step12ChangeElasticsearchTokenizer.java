package org.molgenis.data.version.v1_8;

import static org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory.DEFAULT_INDEX_NAME;

import java.util.Collections;
import java.util.Map;

import org.elasticsearch.client.IndicesAdminClient;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.version.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Step12ChangeElasticsearchTokenizer extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step12ChangeElasticsearchTokenizer.class);

	private final EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	public Step12ChangeElasticsearchTokenizer(EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory)
	{
		super(11, 12);
		this.embeddedElasticSearchServiceFactory = embeddedElasticSearchServiceFactory;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating Elasticsearch settings ...");

		// close index
		IndicesAdminClient indices = embeddedElasticSearchServiceFactory.getClient().admin().indices();
		indices.prepareClose(DEFAULT_INDEX_NAME).execute().actionGet();

		// update tokenizer pattern
		Map<String, Object> updatedTokenizerPattern = Collections.singletonMap(
				"index.analysis.tokenizer.default_tokenizer.pattern", "([^a-zA-Z0-9]+)");
		indices.prepareUpdateSettings(DEFAULT_INDEX_NAME).setSettings(updatedTokenizerPattern).execute().actionGet();

		// open index
		indices.prepareOpen(DEFAULT_INDEX_NAME).execute().actionGet();

		LOG.info("Updated Elasticsearch settings");
	}
}
