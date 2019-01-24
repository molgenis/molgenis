package org.molgenis.data.elasticsearch;

@SuppressWarnings("unused")
public final class FieldConstants {

  private FieldConstants() {}

  public static final String FIELD_NOT_ANALYZED = "raw";
  public static final String DEFAULT_ANALYZER = "default";
  public static final String NGRAM_ANALYZER = "ngram_analyzer";
  public static final String AGGREGATION_MISSING_POSTFIX = "_missing";
  public static final String AGGREGATION_DISTINCT_POSTFIX = "_distinct";
  public static final String AGGREGATION_TERMS_POSTFIX = "_terms";
}
