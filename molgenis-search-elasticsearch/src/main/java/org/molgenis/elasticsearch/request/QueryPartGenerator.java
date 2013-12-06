package org.molgenis.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.Query;

/**
 * Generates a part of the elasticsearch query of the SearchRequestBuilder. There are generators for the lucene
 * queryString part, the offset/limit part and the sort part.
 * 
 * The supportsOperator method determines wich QueryRule is supported by this generator
 * 
 * @author erwin
 * 
 */
public interface QueryPartGenerator
{
	/**
	 * Add the query part to the SearchRequestBuilder
	 * 
	 * @param searchRequestBuilder
	 */
	void generate(SearchRequestBuilder searchRequestBuilder, Query query);
}
