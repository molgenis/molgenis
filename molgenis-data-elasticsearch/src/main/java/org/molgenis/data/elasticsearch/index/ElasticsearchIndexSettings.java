package org.molgenis.data.elasticsearch.index;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Creates Elasticsearch index settings.
 */
public class ElasticsearchIndexSettings
{
	public static final String DEFAULT_ANALYZER = "default";
	static final String NGRAM_ANALYZER = "ngram_analyzer";
	private static final String NGRAM_TOKENIZER = "ngram_tokenizer";
	private static final String DEFAULT_TOKENIZER = "default_tokenizer";
	private static final String DEFAULT_STEMMER = "default_stemmer";

	private ElasticsearchIndexSettings()
	{
	}

	public static Settings getIndexSettings()
	{
		try
		{
			Settings.Builder settings = Settings.builder().loadFromSource(
					XContentFactory.jsonBuilder().startObject().field("index.mapper.dynamic", false)
							.field("index.number_of_shards", 1).field("index.number_of_replicas", 0)
							.field("index.mapping.total_fields.limit", Long.MAX_VALUE)
							.field("index.mapping.nested_fields.limit", Long.MAX_VALUE).startObject("analysis")
							.startObject("analyzer").startObject(DEFAULT_ANALYZER).field("type", "custom")
							.field("filter", new String[] { "lowercase", DEFAULT_STEMMER })
							.field("tokenizer", DEFAULT_TOKENIZER).field("char_filter", "html_strip").endObject()
							.startObject(NGRAM_ANALYZER).field("type", "custom")
							.field("filter", new String[] { "lowercase" }).field("tokenizer", NGRAM_TOKENIZER)
							.endObject().endObject().startObject("filter").startObject(DEFAULT_STEMMER)
							.field("type", "stemmer").field("name", "english").endObject().endObject()
							.startObject("tokenizer").startObject(DEFAULT_TOKENIZER).field("type", "pattern")
							.field("pattern", "([^a-zA-Z0-9]+)").endObject().startObject(NGRAM_TOKENIZER)
							.field("type", "nGram").field("min_gram", 1).field("max_gram", 10).endObject().endObject()
							.endObject().endObject().string(), XContentType.JSON);
			return settings.build();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
