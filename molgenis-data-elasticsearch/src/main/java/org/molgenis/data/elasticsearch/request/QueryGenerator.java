package org.molgenis.data.elasticsearch.request;

import com.google.common.collect.Iterables;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.MolgenisDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;

/**
 * Creates Elasticsearch query from MOLGENIS query
 */
public class QueryGenerator implements QueryPartGenerator
{
	static final String ATTRIBUTE_SEPARATOR = ".";

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query<Entity> query, EntityType entityType)
	{
		List<QueryRule> queryRules = query.getRules();
		if (queryRules == null || queryRules.isEmpty()) return;

		QueryBuilder q = createQueryBuilder(queryRules, entityType);
		searchRequestBuilder.setQuery(q);
	}

	public QueryBuilder createQueryBuilder(List<QueryRule> queryRules, EntityType entityType)
	{
		QueryBuilder queryBuilder;

		final int nrQueryRules = queryRules.size();
		if (nrQueryRules == 1)
		{
			// simple query consisting of one query clause
			queryBuilder = createQueryClause(queryRules.get(0), entityType);
		}
		else
		{
			// boolean query consisting of combination of query clauses
			Operator occur = null;
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			for (int i = 0; i < nrQueryRules; i += 2)
			{
				QueryRule queryRule = queryRules.get(i);

				// determine whether this query is a 'not' query
				if (queryRule.getOperator() == Operator.NOT)
				{
					occur = Operator.NOT;
					queryRule = queryRules.get(i + 1);
					i += 1;
				}
				else if (i + 1 < nrQueryRules)
				{
					QueryRule occurQueryRule = queryRules.get(i + 1);
					Operator occurOperator = occurQueryRule.getOperator();
					if (occurOperator == null) throw new MolgenisQueryException("Missing expected occur operator");

					switch (occurOperator)
					{
						case AND:
						case OR:
							if (occur != null && occurOperator != occur)
							{
								throw new MolgenisQueryException(
										"Mixing query operators not allowed, use nested queries");
							}
							occur = occurOperator;
							break;
						// $CASES-OMITTED$
						default:
							throw new MolgenisQueryException(
									"Expected query occur operator instead of [" + occurOperator + "]");
					}
				}

				QueryBuilder queryPartBuilder = createQueryClause(queryRule, entityType);
				if (queryPartBuilder == null) continue; // skip SHOULD and DIS_MAX query rules

				// add query part to query
				switch (occur)
				{
					case AND:
						boolQuery.must(queryPartBuilder);
						break;
					case OR:
						boolQuery.should(queryPartBuilder).minimumNumberShouldMatch(1);
						break;
					case NOT:
						boolQuery.mustNot(queryPartBuilder);
						break;
					// $CASES-OMITTED$
					default:
						throw new MolgenisQueryException("Unknown occurence operator [" + occur + "]");
				}
			}
			queryBuilder = boolQuery;
		}
		return queryBuilder;
	}

	/**
	 * Create query clause for query rule
	 *
	 * @param queryRule
	 * @param entityType
	 * @return query class or null for SHOULD and DIS_MAX query rules
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder createQueryClause(QueryRule queryRule, EntityType entityType)
	{
		// create query rule
		String queryField = queryRule.getField();
		Operator queryOperator = queryRule.getOperator();
		Object queryValue = queryRule.getValue();

		QueryBuilder queryBuilder;

		switch (queryOperator)
		{
			case AND:
			case OR:
			case NOT:
				throw new MolgenisQueryException("Unexpected query operator [" + queryOperator + ']');
			case SHOULD:
				BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
				for (QueryRule subQuery : queryRule.getNestedRules())
				{
					boolQueryBuilder.should(createQueryClause(subQuery, entityType));
				}
				queryBuilder = boolQueryBuilder;
				break;
			case DIS_MAX:
				DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
				for (QueryRule subQuery : queryRule.getNestedRules())
				{
					disMaxQueryBuilder.add(createQueryClause(subQuery, entityType));
				}
				disMaxQueryBuilder.tieBreaker((float) 0.0);
				if (queryRule.getValue() != null)
				{
					disMaxQueryBuilder.boost(Float.parseFloat(queryRule.getValue().toString()));
				}
				queryBuilder = disMaxQueryBuilder;
				break;
			case EQUALS:
			{
				// As a general rule, filters should be used instead of queries:
				// - for binary yes/no searches
				// - for queries on exact values

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValue instanceof Date)
				{
					String[] attributePath = parseAttributePath(queryField);

					Attribute attr = getAttribute(entityType, attributePath);
					queryValue = getESDateQueryValue((Date) queryValue, attr);
				}

				FilterBuilder filterBuilder;
				String[] attributePath = parseAttributePath(queryField);
				Attribute attr = getAttribute(entityType, attributePath);

				// construct query part
				if (queryValue != null)
				{
					AttributeType attrType = attr.getDataType();
					switch (attrType)
					{
						case BOOL:
						case DATE:
						case DATE_TIME:
						case DECIMAL:
						case INT:
						case LONG:
						{
							filterBuilder = FilterBuilders.termFilter(queryField, queryValue);
							filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
							break;
						}
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case SCRIPT:
						case STRING:
						case TEXT:
						{
							filterBuilder = FilterBuilders
									.termFilter(queryField + '.' + MappingsBuilder.FIELD_NOT_ANALYZED, queryValue);
							filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
							break;
						}
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case XREF:
						case MREF:
						case FILE:
						case ONE_TO_MANY:
						{
							if (attributePath.length > 1)
								throw new UnsupportedOperationException("Can not filter on references deeper than 1.");

							// support both entity as entity id as value
							Object queryIdValue = queryValue instanceof Entity ? ((Entity) queryValue)
									.getIdValue() : queryValue;

							Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
							String indexFieldName = getXRefEqualsInSearchFieldName(refIdAttr, queryField);

							filterBuilder = FilterBuilders
									.nestedFilter(queryField, FilterBuilders.termFilter(indexFieldName, queryIdValue));
							break;
						}
						case COMPOUND:
							throw new MolgenisQueryException(
									format("Illegal attribute type [%s]", attrType.toString()));
						default:
							throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
					}
				}
				else
				{
					AttributeType dataType = attr.getDataType();
					switch (dataType)
					{
						case BOOL:
						case DATE:
						case DATE_TIME:
						case DECIMAL:
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case INT:
						case LONG:
						case SCRIPT:
						case STRING:
						case TEXT:
							filterBuilder = FilterBuilders.missingFilter(queryField).existence(true).nullValue(true);
							break;
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case FILE:
						case MREF:
						case XREF:
							Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
							String indexFieldName = getXRefEqualsInSearchFieldName(refIdAttr, queryField);

							// see https://github.com/elastic/elasticsearch/issues/3495
							filterBuilder = FilterBuilders.notFilter(FilterBuilders
									.nestedFilter(queryField, FilterBuilders.existsFilter(indexFieldName)));
							break;
						case COMPOUND:
							throw new MolgenisQueryException(
									"Illegal data type [" + dataType + "] for operator [" + queryOperator + "]");
						default:
							throw new RuntimeException("Unknown data type [" + dataType + "]");

					}
				}
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case GREATER:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityType);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValue instanceof Date)
				{
					Attribute attr = getAttribute(entityType, attributePath);
					queryValue = getESDateQueryValue((Date) queryValue, attr);
				}

				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gt(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);

				break;
			}
			case GREATER_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityType);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValue instanceof Date)
				{
					Attribute attr = getAttribute(entityType, attributePath);
					queryValue = getESDateQueryValue((Date) queryValue, attr);
				}
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gte(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);

				break;
			}
			case IN:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				if (!(queryValue instanceof Iterable<?>))
				{
					throw new MolgenisQueryException(
							"Query value must be a Iterable instead of [" + queryValue.getClass().getSimpleName()
									+ "]");
				}
				Iterable<?> iterable = (Iterable<?>) queryValue;

				String[] attributePath = parseAttributePath(queryField);
				Attribute attr = getAttribute(entityType, attributePath);
				AttributeType dataType = attr.getDataType();

				FilterBuilder filterBuilder;
				switch (dataType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case INT:
					case LONG:
					case SCRIPT:
					case STRING:
					case TEXT:
						// note: inFilter expects array, not iterable
						filterBuilder = FilterBuilders
								.inFilter(getFieldName(attr, queryField), Iterables.toArray(iterable, Object.class));
						filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
						break;
					case CATEGORICAL:
					case CATEGORICAL_MREF:
					case MREF:
					case XREF:
					case FILE:
						if (attributePath.length > 1)
							throw new UnsupportedOperationException("Can not filter on references deeper than 1.");

						// support both entity iterable as entity id iterable as value
						Iterable<Object> idValues;
						if (isEntityIterable(iterable))
						{
							idValues = Iterables.transform((Iterable<Entity>) iterable, Entity::getIdValue);
						}
						else
						{
							idValues = (Iterable<Object>) iterable;
						}

						// note: inFilter expects array, not iterable
						filterBuilder = FilterBuilders.nestedFilter(queryField, FilterBuilders.inFilter(
								getXRefEqualsInSearchFieldName(attr.getRefEntity().getIdAttribute(), queryField),
								Iterables.toArray(idValues, Object.class)));
						break;
					case COMPOUND:
						throw new MolgenisQueryException(
								"Illegal data type [" + dataType + "] for operator [" + queryOperator + "]");
					default:
						throw new RuntimeException("Unknown data type [" + dataType + "]");
				}
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityType);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValue instanceof Date)
				{
					Attribute attr = getAttribute(entityType, attributePath);
					queryValue = getESDateQueryValue((Date) queryValue, attr);
				}
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).lt(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityType);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValue instanceof Date)
				{
					Attribute attr = getAttribute(entityType, attributePath);
					queryValue = getESDateQueryValue((Date) queryValue, attr);
				}
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).lte(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case RANGE:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				if (!(queryValue instanceof Iterable<?>))
				{
					throw new MolgenisQueryException(
							"Query value must be a Iterable instead of [" + queryValue.getClass().getSimpleName()
									+ "]");
				}
				Iterable<?> iterable = (Iterable<?>) queryValue;

				validateNumericalQueryField(queryField, entityType);

				Iterator<?> iterator = iterable.iterator();

				String[] attributePath = parseAttributePath(queryField);
				Attribute attr = getAttribute(entityType, attributePath);

				Object queryValueFrom = iterator.next();

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValueFrom instanceof Date)
				{
					queryValueFrom = getESDateQueryValue((Date) queryValueFrom, attr);
				}

				Object queryValueTo = iterator.next();

				// Workaround for Elasticsearch Date to String conversion issue
				if (queryValueTo instanceof Date)
				{
					queryValueTo = getESDateQueryValue((Date) queryValueTo, attr);
				}

				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gte(queryValueFrom)
						.lte(queryValueTo);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case NESTED:
				List<QueryRule> nestedQueryRules = queryRule.getNestedRules();
				if (nestedQueryRules == null || nestedQueryRules.isEmpty())
				{
					throw new MolgenisQueryException("Missing nested rules for nested query");
				}
				queryBuilder = createQueryBuilder(nestedQueryRules, entityType);
				break;
			case LIKE:
			{
				String[] attributePath = parseAttributePath(queryField);
				Attribute attr = getAttribute(entityType, attributePath);

				// construct query part
				AttributeType dataType = attr.getDataType();
				switch (dataType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case COMPOUND:
					case INT:
					case LONG:
						throw new MolgenisQueryException(
								"Illegal data type [" + dataType + "] for operator [" + queryOperator + "]");
					case CATEGORICAL:
					case CATEGORICAL_MREF:
					case MREF:
					case XREF:
					case FILE:
					case SCRIPT: // due to size would result in large amount of ngrams
					case TEXT: // due to size would result in large amount of ngrams
					case HTML: // due to size would result in large amount of ngrams
						throw new UnsupportedOperationException(
								"Query with operator [" + queryOperator + "] and data type [" + dataType
										+ "] not supported");
					case EMAIL:
					case ENUM:
					case HYPERLINK:
					case STRING:
						queryBuilder = QueryBuilders
								.matchQuery(queryField + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, queryValue)
								.analyzer(DEFAULT_ANALYZER);
						queryBuilder = nestedQueryBuilder(attributePath, queryBuilder);
						break;
					default:
						throw new RuntimeException("Unknown data type [" + dataType + "]");
				}
				break;
			}
			case SEARCH:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");

				// 1. attribute: search in attribute
				// 2. no attribute: search in all
				if (queryField == null)
				{
					queryBuilder = QueryBuilders.matchPhraseQuery("_all", queryValue).slop(10);
				}
				else
				{
					String[] attributePath = parseAttributePath(queryField);

					Attribute attr = getAttribute(entityType, attributePath);

					// construct query part
					AttributeType dataType = attr.getDataType();
					switch (dataType)
					{
						case BOOL:
							throw new MolgenisQueryException(
									"Cannot execute search query on [" + dataType + "] attribute");
						case DATE:
						case DATE_TIME:
						case DECIMAL:
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case INT:
						case LONG:
						case SCRIPT:
						case STRING:
						case TEXT:
							queryBuilder = QueryBuilders.matchQuery(queryField, queryValue);
							queryBuilder = nestedQueryBuilder(attributePath, queryBuilder);
							break;
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case MREF:
						case XREF:
						case FILE:
							if (attributePath.length > 1)
								throw new UnsupportedOperationException("Can not filter on references deeper than 1.");

							queryBuilder = QueryBuilders.nestedQuery(queryField,
									QueryBuilders.matchQuery(queryField + '.' + "_all", queryValue));
							break;
						case COMPOUND:
							throw new MolgenisQueryException(
									"Illegal data type [" + dataType + "] for operator [" + queryOperator + "]");
						default:
							throw new RuntimeException("Unknown data type [" + dataType + "]");
					}

				}

				break;
			}
			case FUZZY_MATCH:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");

				if (queryField == null)
				{
					queryBuilder = QueryBuilders.matchQuery("_all", queryValue);
				}
				else
				{
					Attribute attr = entityType.getAttribute(queryField);
					if (attr == null) throw new UnknownAttributeException(queryField);
					// construct query part
					AttributeType dataType = attr.getDataType();
					switch (dataType)
					{
						case DATE:
						case DATE_TIME:
						case DECIMAL:
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case INT:
						case LONG:
						case SCRIPT:
						case STRING:
						case TEXT:
							queryBuilder = QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")");
							break;
						case MREF:
						case XREF:
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case FILE:
							queryField = attr.getName() + "." + attr.getRefEntity().getLabelAttribute().getName();
							queryBuilder = QueryBuilders.nestedQuery(attr.getName(),
									QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")"))
									.scoreMode("max");
							break;
						case BOOL:
						case COMPOUND:
							throw new MolgenisQueryException(
									"Illegal data type [" + dataType + "] for operator [" + queryOperator + "]");
						default:
							throw new RuntimeException("Unknown data type [" + dataType + "]");
					}
				}
				break;
			}
			case FUZZY_MATCH_NGRAM:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");

				if (queryField == null)
				{
					queryBuilder = QueryBuilders.matchQuery("_all", queryValue);
				}
				else
				{
					Attribute attr = entityType.getAttribute(queryField);
					if (attr == null) throw new UnknownAttributeException(queryField);
					// construct query part
					AttributeType dataType = attr.getDataType();
					switch (dataType)
					{
						case DATE:
						case DATE_TIME:
						case DECIMAL:
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case INT:
						case LONG:
						case SCRIPT:
						case STRING:
						case TEXT:
							queryField = queryField + ".ngram";
							queryBuilder = QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")");
							break;
						case MREF:
						case XREF:
							queryField =
									attr.getName() + "." + attr.getRefEntity().getLabelAttribute().getName() + ".ngram";
							queryBuilder = QueryBuilders.nestedQuery(attr.getName(),
									QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")"))
									.scoreMode("max");
							break;
						default:
							throw new RuntimeException("Unknown data type [" + dataType + "]");
					}
				}
				break;
			}
			default:
				throw new MolgenisQueryException("Unknown query operator [" + queryOperator + "]");
		}
		return queryBuilder;
	}

	private String getFieldName(Attribute attr, String queryField)
	{
		AttributeType dataType = attr.getDataType();

		switch (dataType)
		{
			case XREF:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case MREF:
			case FILE:
				return queryField;
			case BOOL:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case INT:
			case LONG:
				return queryField;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return new StringBuilder(queryField).append('.').append(MappingsBuilder.FIELD_NOT_ANALYZED).toString();
			case COMPOUND:
				throw new MolgenisQueryException("Illegal data type [" + dataType + "] not supported");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private String getXRefEqualsInSearchFieldName(Attribute refIdAttr, String queryField)
	{
		String indexFieldName = queryField + '.' + refIdAttr.getName();
		return getFieldName(refIdAttr, indexFieldName);
	}

	private void validateNumericalQueryField(String queryField, EntityType entityType)
	{
		String[] attributePath = parseAttributePath(queryField);

		AttributeType dataType = getAttribute(entityType, attributePath).getDataType();

		switch (dataType)
		{
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case INT:
			case LONG:
				break;
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case EMAIL:
			case ENUM:
			case FILE:
			case HTML:
			case HYPERLINK:
			case MREF:
			case SCRIPT:
			case STRING:
			case TEXT:
			case XREF:
				throw new MolgenisQueryException("Range query not allowed for type [" + dataType + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private boolean isEntityIterable(Iterable<?> iterable)
	{
		Iterator<?> it = iterable.iterator();
		boolean isEntity = it.hasNext() && (it.next() instanceof Entity);
		return isEntity;
	}

	private String[] parseAttributePath(String queryField)
	{
		return queryField.split("\\" + ATTRIBUTE_SEPARATOR);
	}

	/**
	 * Wraps the filter in a nested filter when a query is done on a reference entity. Returns the original filter when
	 * it is applied to the current entity.
	 */
	private FilterBuilder nestedFilterBuilder(String[] attributePath, FilterBuilder filterBuilder)
	{
		if (attributePath.length == 1)
		{
			return filterBuilder;
		}
		else if (attributePath.length == 2)
		{
			return FilterBuilders.nestedFilter(attributePath[0], filterBuilder);
		}
		else
		{
			throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
		}
	}

	/**
	 * Wraps the query in a nested query when a query is done on a reference entity. Returns the original query when it
	 * is applied to the current entity.
	 */
	private QueryBuilder nestedQueryBuilder(String[] attributePath, QueryBuilder queryBuilder)
	{
		if (attributePath.length == 1)
		{
			return queryBuilder;
		}
		else if (attributePath.length == 2)
		{
			return QueryBuilders.nestedQuery(attributePath[0], queryBuilder);
		}
		else
		{
			throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
		}
	}

	/**
	 * Returns the target attribute. Looks in the reference entity when it is a nested query.
	 */
	private Attribute getAttribute(EntityType entityType, String[] attributePath)
	{
		if (attributePath.length > 2)
			throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
		if (attributePath.length == 0) throw new MolgenisQueryException("Attribute path length is 0!");

		if (attributePath.length == 1)
		{
			Attribute attr = entityType.getAttribute(attributePath[0]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0]);
			return attr;
		}
		else
		{
			Attribute attr = entityType.getAttribute(attributePath[0]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0]);

			attr = attr.getRefEntity().getAttribute(attributePath[1]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0] + "." + attributePath[1]);

			return attr;
		}
	}

	private String getESDateQueryValue(Date queryValue, Attribute attr)
	{
		if (attr.getDataType() == AttributeType.DATE_TIME)
		{
			return MolgenisDateFormat.getDateTimeFormat().format(queryValue);
		}

		return MolgenisDateFormat.getDateFormat().format(queryValue);
	}
}
