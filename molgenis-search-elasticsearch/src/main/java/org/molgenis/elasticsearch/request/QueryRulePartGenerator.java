package org.molgenis.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Generates a part of the elasticsearch query of the SearchRequestBuilder.
 * There are generators for the lucene queryString part, the offset/limit part
 * and the sort part.
 * 
 * The supportsOperator method determines wich QueryRule is supported by this
 * generator
 * 
 * @author erwin
 * 
 */
public interface QueryRulePartGenerator
{
	/**
	 * Returns if this generator supports a certain QueryRule Operator
	 * 
	 * @param operator
	 * @return
	 */
	boolean supportsOperator(Operator operator);

	/**
	 * Adds a QueryRule to the query
	 * 
	 * @param queryRule
	 */
	void addQueryRule(QueryRule queryRule);

	/**
	 * Add the query part to the SearchRequestBuilder
	 * 
	 * @param searchRequestBuilder
	 */
	void generate(SearchRequestBuilder searchRequestBuilder);
}
