package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.molgenis.data.elasticsearch.FieldConstants;
import org.molgenis.data.elasticsearch.generator.model.IndexSettings;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

/**
 * Creates Elasticsearch transport client content for settings.
 */
class SettingsContentBuilder
{
	private static final String NGRAM_TOKENIZER = "ngram_tokenizer";
	private static final String DEFAULT_TOKENIZER = "default_tokenizer";
	private static final String DEFAULT_STEMMER = "default_stemmer";

	private final XContentType xContentType;

	SettingsContentBuilder()
	{
		this(XContentType.JSON);
	}

	private SettingsContentBuilder(XContentType xContentType)
	{
		this.xContentType = requireNonNull(xContentType);
	}

	XContentBuilder createSettings(IndexSettings indexSettings)
	{
		try (XContentBuilder contentBuilder = XContentFactory.contentBuilder(xContentType))
		{
			createSettings(indexSettings, contentBuilder);
			return contentBuilder;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void createSettings(IndexSettings indexSettings, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject();
		createIndexSettings(indexSettings, contentBuilder);
		contentBuilder.endObject();
	}

	private void createIndexSettings(IndexSettings indexSettings, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("index");

		contentBuilder.field("number_of_shards", indexSettings.getNumberOfShards());
		contentBuilder.field("number_of_replicas", indexSettings.getNumberOfReplicas());
		createMapperSettings(contentBuilder);
		createMappingSettings(contentBuilder);
		createAnalysisSettings(contentBuilder);

		contentBuilder.endObject();
	}

	private void createMapperSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("mapper");
		contentBuilder.field("dynamic", false);
		contentBuilder.endObject();
	}

	private void createMappingSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("mapping");
		contentBuilder.field("total_fields.limit", Long.MAX_VALUE);
		contentBuilder.field("nested_fields.limit", Long.MAX_VALUE);
		contentBuilder.endObject();
	}

	private void createAnalysisSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("analysis");
		createFilterSettings(contentBuilder);
		createAnalyzerSettings(contentBuilder);
		createTokenizerSettings(contentBuilder);
		contentBuilder.endObject();
	}

	private void createFilterSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("filter");
		createDefaultStemmerSettings(contentBuilder);
		contentBuilder.endObject();
	}

	private void createDefaultStemmerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(DEFAULT_STEMMER);
		contentBuilder.field("type", "stemmer");
		contentBuilder.field("name", "english");
		contentBuilder.endObject();
	}

	private void createAnalyzerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("analyzer");
		createDefaultAnalyzerSettings(contentBuilder);
		createNGramAnalyzerSettings(contentBuilder);
		contentBuilder.endObject();
	}

	private void createDefaultAnalyzerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(FieldConstants.DEFAULT_ANALYZER);
		contentBuilder.field("type", "custom");
		contentBuilder.array("filter", "lowercase", DEFAULT_STEMMER);
		contentBuilder.field("tokenizer", DEFAULT_TOKENIZER);
		contentBuilder.field("char_filter", "html_strip");
		contentBuilder.endObject();
	}

	private void createNGramAnalyzerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(FieldConstants.NGRAM_ANALYZER);
		contentBuilder.field("type", "custom");
		contentBuilder.array("filter", "lowercase");
		contentBuilder.field("tokenizer", NGRAM_TOKENIZER);
		contentBuilder.endObject();
	}

	private void createTokenizerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject("tokenizer");
		createDefaultTokenizerSettings(contentBuilder);
		createNGramTokenizerSettings(contentBuilder);
		contentBuilder.endObject();
	}

	private void createDefaultTokenizerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(DEFAULT_TOKENIZER);
		contentBuilder.field("type", "pattern");
		contentBuilder.field("pattern", "([^a-zA-Z0-9]+)");
		contentBuilder.endObject();
	}

	private void createNGramTokenizerSettings(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(NGRAM_TOKENIZER);
		contentBuilder.field("type", "nGram");
		contentBuilder.field("min_gram", 1);
		contentBuilder.field("max_gram", 10);
		contentBuilder.endObject();
	}
}
