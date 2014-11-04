package org.molgenis.data;

import java.util.List;

import org.springframework.data.domain.Sort;

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

	<E extends Entity> Iterable<E> findAll(Class<E> klazz);

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

	Query or();

	Query and();

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
	 * Range (excluding smaller and bigger)
	 */
	Query rng(String field, Object smaller, Object bigger);

	Query pageSize(int pageSize);

	Query offset(int offset);

	Query sort(Sort.Direction direction, String... fields);

	Query sort(Sort sort);
}
