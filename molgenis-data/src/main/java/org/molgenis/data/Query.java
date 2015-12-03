package org.molgenis.data;

import java.util.List;

/**
 * Definition of a query
 */
public interface Query extends Iterable<Entity>
{
	/**
	 * Count entities matching query
	 *
	 * @return count or Exception of not bound to repository
	 */
	Long count();

	Iterable<Entity> findAll();

	Entity findOne();

	/**
	 * Filtering rules, seperated by QueryRule.AND and QueryRule.OR clauses
	 */
	List<QueryRule> getRules();

	/**
	 * Size of a page. Synonym: maxResults
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

	/**
	 * Search field
	 */
	Query search(String field, String searchTerms);

	/**
	 * Occur operator 'or'. Example usage: query.eq("field0", "val0").or().eq("field1", "val1")
	 */
	Query or();

	/**
	 * Occur operator 'and'. Example usage: query.eq("field0", "val0").and().eq("field1", "val1")
	 */
	Query and();

	/**
	 * Occur operator 'not'. Example usage: query.not().eq("field0", "val0")
	 */
	Query not();

	/**
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	Query like(String field, String value);

	/**
	 * 
	 * @param field
	 * @param value
	 *            categorical/xref: entity or entity id; mref: entity iterable or id iterable; else: value
	 * @return
	 */
	Query eq(String field, Object value);

	/**
	 * 
	 * @param field
	 * @param values
	 *            ids or entities
	 * @return
	 */
	Query in(String field, Iterable<?> values);

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
	 * Range (including smaller and bigger)
	 */
	Query rng(String field, Object smaller, Object bigger);

	Query pageSize(int pageSize);

	Query offset(int offset);

	Sort sort();

	Query sort(Sort sort);

	/**
	 * Return the query {@link Fetch} that defines which entity attributes to retrieve.
	 * 
	 * @return the query {@link Fetch} or null
	 */
	Fetch getFetch();

	/**
	 * Sets the query {@link Fetch} that defines which entity attributes to retrieve.
	 * 
	 * @param fetch
	 *            the query {@link Fetch}
	 */
	void setFetch(Fetch fetch);

	/**
	 * Create a new empty {@link Fetch} that defines which entity attributes to retrieve.
	 * 
	 * @return new empty {@link Fetch}
	 */
	Fetch fetch();

	/**
	 * Sets the query {@link Fetch} that defines which entity attributes to retrieve.
	 * 
	 * @return this query
	 */
	Query fetch(Fetch fetch);
}
