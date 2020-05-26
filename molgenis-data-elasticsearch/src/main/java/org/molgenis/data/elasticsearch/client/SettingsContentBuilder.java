package org.molgenis.data.elasticsearch.client;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.molgenis.data.elasticsearch.FieldConstants;
import org.molgenis.data.elasticsearch.generator.model.IndexSettings;

/** Creates Elasticsearch transport client content for settings. */
class SettingsContentBuilder {

  public static final String CI_NORMALIZER = "lowercase_asciifold";
  private static final String DEFAULT_STEMMER = "default_stemmer";
  public static final String FILTER = "filter";

  private final XContentType xContentType;

  SettingsContentBuilder() {
    this(XContentType.JSON);
  }

  private SettingsContentBuilder(XContentType xContentType) {
    this.xContentType = requireNonNull(xContentType);
  }

  XContentBuilder createSettings(IndexSettings indexSettings) {
    try (XContentBuilder contentBuilder = XContentFactory.contentBuilder(xContentType)) {
      createSettings(indexSettings, contentBuilder);
      return contentBuilder;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void createSettings(IndexSettings indexSettings, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.startObject();
    createIndexSettings(indexSettings, contentBuilder);
    contentBuilder.endObject();
  }

  private void createIndexSettings(IndexSettings indexSettings, XContentBuilder contentBuilder)
      throws IOException {
    contentBuilder.startObject("index");

    contentBuilder.field("number_of_shards", indexSettings.getNumberOfShards());
    contentBuilder.field("number_of_replicas", indexSettings.getNumberOfReplicas());
    createMapperSettings(contentBuilder);
    createMappingSettings(contentBuilder);
    createAnalysisSettings(contentBuilder);

    contentBuilder.endObject();
  }

  private void createMapperSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject("mapper");
    contentBuilder.field("dynamic", false);
    contentBuilder.endObject();
  }

  private void createMappingSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject("mapping");
    contentBuilder.field("total_fields.limit", Long.MAX_VALUE);
    contentBuilder.field("nested_fields.limit", Long.MAX_VALUE);
    contentBuilder.endObject();
  }

  private void createAnalysisSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject("analysis");
    createFilterSettings(contentBuilder);
    createAnalyzerSettings(contentBuilder);
    createNormalizerSettings(contentBuilder);
    contentBuilder.endObject();
  }

  private void createFilterSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject(FILTER);
    createDefaultStemmerSettings(contentBuilder);
    contentBuilder.endObject();
  }

  private void createDefaultStemmerSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject(DEFAULT_STEMMER);
    contentBuilder.field("type", "stemmer");
    contentBuilder.field("name", "english");
    contentBuilder.endObject();
  }

  private void createAnalyzerSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject("analyzer");
    createDefaultAnalyzerSettings(contentBuilder);
    createNGramAnalyzerSettings(contentBuilder);
    contentBuilder.endObject();
  }

  private void createDefaultAnalyzerSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject(FieldConstants.DEFAULT_ANALYZER);
    contentBuilder.field("type", "custom");
    contentBuilder.array(FILTER, "lowercase", "asciifolding", DEFAULT_STEMMER);
    contentBuilder.field("tokenizer", "standard");
    contentBuilder.field("char_filter", "html_strip");
    contentBuilder.endObject();
  }

  private void createNGramAnalyzerSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder.startObject(FieldConstants.NGRAM_ANALYZER);
    contentBuilder.field("type", "custom");
    contentBuilder.array(FILTER, "lowercase", "asciifolding");
    contentBuilder.field("tokenizer", "ngram");
    contentBuilder.endObject();
  }

  private static void createNormalizerSettings(XContentBuilder contentBuilder) throws IOException {
    contentBuilder
        .startObject("normalizer")
        .startObject(CI_NORMALIZER)
        .field("type", "custom")
        .array("char_filter")
        .array(FILTER, "lowercase", "asciifolding")
        .endObject()
        .endObject();
  }
}
