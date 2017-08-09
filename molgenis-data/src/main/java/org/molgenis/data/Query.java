package org.molgenis.data;

import java.util.List;
import java.util.stream.Stream;

/**
 * Definition of a query
 */
public interface Query<E extends Entity> extends Iterable<E>
{
	Repository<E> getRepository();

	/**
	 * Count entities matching query
	 *
	 * @return count or Exception of not bound to repository
	 */
	Long count();

	Stream<E> findAll();

	E findOne();

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
	Query<E> search(String searchTerms);

	/**
	 * Search field
	 */
	Query<E> search(String field, String searchTerms);

	/**
	 * Occur operator 'or'. Example usage: query.eq("field0", "val0").or().eq("field1", "val1")
	 */
	Query<E> or();

	/**
	 * Occur operator 'and'. Example usage: query.eq("field0", "val0").and().eq("field1", "val1")
	 */
	Query<E> and();

	/**
	 * Occur operator 'not'. Example usage: query.not().eq("field0", "val0")
	 */
	Query<E> not();

	/**
	 * @param field
	 * @param value
	 * @return Query<E>
	 */
	Query<E> like(String field, String value);

	/**
	 * @param field
	 * @param value categorical/xref: entity or entity id; mref: entity iterable or id iterable; else: value
	 * @return Query<E>
	 */
	Query<E> eq(String field, Object value);

	/**
	 * @param field
	 * @param values ids
	 * @return Query<E>
	 */
	Query<E> in(String field, Iterable<?> values);

	/**
	 * Greater than
	 */
	Query<E> gt(String field, Object value);

	/**
	 * Greater than or equal to
	 */
	Query<E> ge(String field, Object value);

	/**
	 * Less than
	 */
	Query<E> lt(String field, Object value);

	/**
	 * Less than or equal to
	 */
	Query<E> le(String field, Object value);

	/**
	 * Start nested query
	 */
	Query<E> nest();

	/**
	 * End nested query
	 */
	Query<E> unnest();

	Query<E> unnestAll();

	/**
	 * Range (including from and to)
	 */
	Query<E> rng(String field, Object from, Object to);

	Query<E> pageSize(int pageSize);

	Query<E> offset(int offset);

	Sort sort();

	Query<E> sort(Sort sort);

	/**
	 * Return the query {@link Fetch} that defines which entity attributes to retrieve.
	 *
	 * @return the query {@link Fetch} or null
	 */
	Fetch getFetch();

	/**
	 * Sets the query {@link Fetch} that defines which entity attributes to retrieve.
	 *
	 * @param fetch the query {@link Fetch}
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
	Query<E> fetch(Fetch fetch);
}
