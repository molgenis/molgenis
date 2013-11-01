package org.molgenis.data;

import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Sort;

/**
 * Definition of a query
 */
public interface Query
{
	/**
	 * Filtering rules
	 */
	List<QueryRule> getRules();

	/**
	 * Enable attribute selection, if null then all attributes will be returned
	 */
	Iterator<String> getAttributeNames();

	/**
	 * Size of a page. Synonym: limit
	 */
	int getPageSize();

	/**
	 * Start
	 */
	int getOffset();

	/**
	 * Returns sort
	 */
	Sort getSort();

	/**
	 * Search all fields
	 */
	Query search(String searchTerms);

	Query or();

	Query like(String field, Object value);

	/**
	 * Equals
	 */
	Query eq(String field, Object value);

	Query in(String field, Iterable<?> objectIterator);

	/**
	 * Greater than
	 */
	Query gt(String field, Object value);

	/**
	 * Greater than or equal to
	 */
	Query ge(String field, Object value);

	/**
	 * Less than
	 */
	Query lt(String field, Object value);

	/**
	 * Less than or equal to
	 */
	Query le(String field, Object value);

	/**
	 * Start nested query
	 */
	Query nest();

	/**
	 * End nested query
	 */
	Query unnest();

	Query unnestAll();

	/**
	 * Range (excluding smaller and bigger)
	 */
	Query rng(String field, Object smaller, Object bigger);

}
