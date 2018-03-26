package org.molgenis.data.elasticsearch;

public interface FieldConstants
{
	String FIELD_NOT_ANALYZED = "raw";
	String DEFAULT_ANALYZER = "default";
	String NGRAM_ANALYZER = "ngram_analyzer";
	String AGGREGATION_MISSING_POSTFIX = "_missing";
	String AGGREGATION_DISTINCT_POSTFIX = "_distinct";
	String AGGREGATION_TERMS_POSTFIX = "_terms";
}
