package org.molgenis.data.elasticsearch.request;

import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;

import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Creates Elasticsearch query from MOLGENIS query
 */
public class QueryGenerator implements QueryPartGenerator
{
	public static final String ATTRIBUTE_SEPARATOR = ".";

	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		List<QueryRule> queryRules = query.getRules();
		if (queryRules == null || queryRules.isEmpty()) return;

		QueryBuilder q = createQueryBuilder(queryRules, entityMetaData);
		searchRequestBuilder.setQuery(q);
	}

	public QueryBuilder createQueryBuilder(List<QueryRule> queryRules, EntityMetaData entityMetaData)
	{
		QueryBuilder queryBuilder;

		final int nrQueryRules = queryRules.size();
		if (nrQueryRules == 1)
		{
			// simple query consisting of one query clause
			queryBuilder = createQueryClause(queryRules.get(0), entityMetaData);
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
							throw new MolgenisQueryException("Expected query occur operator instead of ["
									+ occurOperator + "]");
					}
				}

				QueryBuilder queryPartBuilder = createQueryClause(queryRule, entityMetaData);
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
	 * @param entityMetaData
	 * @return query class or null for SHOULD and DIS_MAX query rules
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder createQueryClause(QueryRule queryRule, EntityMetaData entityMetaData)
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
					boolQueryBuilder.should(createQueryClause(subQuery, entityMetaData));
				}
				queryBuilder = boolQueryBuilder;
				break;
			case DIS_MAX:
				DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
				for (QueryRule subQuery : queryRule.getNestedRules())
				{
					disMaxQueryBuilder.add(createQueryClause(subQuery, entityMetaData));
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

				String[] attributePath = parseAttributePath(queryField);
				AttributeMetaData attr = getAttribute(entityMetaData, attributePath);

				// construct query part
				FilterBuilder filterBuilder;
				if (queryValue != null)
				{
					FieldTypeEnum dataType = attr.getDataType().getEnumType();
					switch (dataType)
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
							filterBuilder = FilterBuilders.termFilter(queryField + '.'
									+ MappingsBuilder.FIELD_NOT_ANALYZED, queryValue);
							filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
							break;
						}
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case XREF:
						case MREF:
						case FILE:
						{
							if (attributePath.length > 1) throw new UnsupportedOperationException(
									"Can not filter on references deeper than 1.");

							// support both entity as entity id as value
							Object queryIdValue = queryValue instanceof Entity ? ((Entity) queryValue).getIdValue() : queryValue;

							AttributeMetaData refIdAttr = attr.getRefEntity().getIdAttribute();
							String indexFieldName = getXRefEqualsInSearchFieldName(refIdAttr, queryField);

							filterBuilder = FilterBuilders.nestedFilter(queryField,
									FilterBuilders.termFilter(indexFieldName, queryIdValue));
							break;
						}
						case COMPOUND:
							throw new MolgenisQueryException("Illegal data type [" + dataType + "] for operator ["
									+ queryOperator + "]");
						case IMAGE:
							throw new UnsupportedOperationException("Query with data type [" + dataType
									+ "] not supported");
						default:
							throw new RuntimeException("Unknown data type [" + dataType + "]");
					}
				}
				else
				{
					filterBuilder = FilterBuilders.missingFilter("").existence(true).nullValue(true);
				}
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case GREATER:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityMetaData);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				queryValue = queryValue instanceof java.util.Date ? queryValue.toString() : queryValue;
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gt(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);

				break;
			}
			case GREATER_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityMetaData);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				queryValue = queryValue instanceof java.util.Date ? queryValue.toString() : queryValue;
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
					throw new MolgenisQueryException("Query value must be a Iterable instead of ["
							+ queryValue.getClass().getSimpleName() + "]");
				}
				Iterable<?> iterable = (Iterable<?>) queryValue;

				String[] attributePath = parseAttributePath(queryField);
				AttributeMetaData attr = getAttribute(entityMetaData, attributePath);
				FieldTypeEnum dataType = attr.getDataType().getEnumType();

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
						filterBuilder = FilterBuilders.inFilter(getFieldName(attr, queryField),
								Iterables.toArray(iterable, Object.class));
						filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
						break;
					case CATEGORICAL:
					case CATEGORICAL_MREF:
					case MREF:
					case XREF:
					case FILE:
						if (attributePath.length > 1) throw new UnsupportedOperationException(
								"Can not filter on references deeper than 1.");

						// support both entity iterable as entity id iterable as value
						Iterable<Object> idValues;
						if (isEntityIterable(iterable))
						{
							idValues = Iterables.transform((Iterable<Entity>) iterable, new Function<Entity, Object>()
							{
								@Override
								public Object apply(Entity entity)
								{
									return entity.getIdValue();
								}
							});
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
						throw new MolgenisQueryException("Illegal data type [" + dataType + "] for operator ["
								+ queryOperator + "]");
					case IMAGE:
						throw new UnsupportedOperationException("Query with data type [" + dataType + "] not supported");
					default:
						throw new RuntimeException("Unknown data type [" + dataType + "]");
				}
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityMetaData);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				queryValue = queryValue instanceof java.util.Date ? queryValue.toString() : queryValue;
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).lt(queryValue);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				validateNumericalQueryField(queryField, entityMetaData);

				String[] attributePath = parseAttributePath(queryField);

				// Workaround for Elasticsearch Date to String conversion issue
				queryValue = queryValue instanceof java.util.Date ? queryValue.toString() : queryValue;
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
					throw new MolgenisQueryException("Query value must be a Iterable instead of ["
							+ queryValue.getClass().getSimpleName() + "]");
				}
				Iterable<?> iterable = (Iterable<?>) queryValue;

				validateNumericalQueryField(queryField, entityMetaData);

				Iterator<?> iterator = iterable.iterator();

				Object queryValueFrom = iterator.next();
				// Workaround for Elasticsearch Date to String conversion issue
				queryValueFrom = queryValueFrom instanceof java.util.Date ? queryValueFrom.toString() : queryValueFrom;

				Object queryValueTo = iterator.next();
				// Workaround for Elasticsearch Date to String conversion issue
				queryValueTo = queryValueTo instanceof java.util.Date ? queryValueTo.toString() : queryValueTo;

				String[] attributePath = parseAttributePath(queryField);
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
				queryBuilder = createQueryBuilder(nestedQueryRules, entityMetaData);
				break;
			case LIKE:
			{
				String[] attributePath = parseAttributePath(queryField);
				AttributeMetaData attr = getAttribute(entityMetaData, attributePath);

				// construct query part
				FieldTypeEnum dataType = attr.getDataType().getEnumType();
				switch (dataType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case COMPOUND:
					case INT:
					case LONG:
						throw new MolgenisQueryException("Illegal data type [" + dataType + "] for operator ["
								+ queryOperator + "]");
					case CATEGORICAL:
					case CATEGORICAL_MREF:
					case MREF:
					case XREF:
					case FILE:
					case SCRIPT: // due to size would result in large amount of ngrams
					case TEXT: // due to size would result in large amount of ngrams
					case HTML: // due to size would result in large amount of ngrams
						throw new UnsupportedOperationException("Query with operator [" + queryOperator
								+ "] and data type [" + dataType + "] not supported");
					case EMAIL:
					case ENUM:
					case HYPERLINK:
					case STRING:
						queryBuilder = QueryBuilders.matchQuery(
								queryField + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, queryValue).analyzer(
								DEFAULT_ANALYZER);
						queryBuilder = nestedQueryBuilder(attributePath, queryBuilder);
						break;
					case IMAGE:
						throw new UnsupportedOperationException("Query with data type [" + dataType + "] not supported");
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

					AttributeMetaData attr = getAttribute(entityMetaData, attributePath);

					// construct query part
					FieldTypeEnum dataType = attr.getDataType().getEnumType();
					switch (dataType)
					{
						case BOOL:
							throw new MolgenisQueryException("Cannot execute search query on [" + dataType
									+ "] attribute");
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
							if (attributePath.length > 1) throw new UnsupportedOperationException(
									"Can not filter on references deeper than 1.");

							queryBuilder = QueryBuilders.nestedQuery(queryField,
									QueryBuilders.matchQuery(queryField + '.' + "_all", queryValue));
							break;
						case COMPOUND:
							throw new MolgenisQueryException("Illegal data type [" + dataType + "] for operator ["
									+ queryOperator + "]");

						case IMAGE:
							throw new UnsupportedOperationException("Query with data type [" + dataType
									+ "] not supported");
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
					AttributeMetaData attr = entityMetaData.getAttribute(queryField);
					if (attr == null) throw new UnknownAttributeException(queryField);
					// construct query part
					FieldTypeEnum dataType = attr.getDataType().getEnumType();
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
							queryBuilder = QueryBuilders.queryString(queryField + ":(" + queryValue + ")");
							break;
						case MREF:
						case XREF:
						case CATEGORICAL:
						case CATEGORICAL_MREF:
						case FILE:
							queryField = attr.getName() + "." + attr.getRefEntity().getLabelAttribute().getName();
							queryBuilder = QueryBuilders.nestedQuery(attr.getName(),
									QueryBuilders.queryString(queryField + ":(" + queryValue + ")")).scoreMode("max");
							break;
						case BOOL:
						case COMPOUND:
							throw new MolgenisQueryException("Illegal data type [" + dataType + "] for operator ["
									+ queryOperator + "]");

						case IMAGE:
							throw new UnsupportedOperationException("Query with data type [" + dataType
									+ "] not supported");
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

	private String getFieldName(AttributeMetaData attr, String queryField)
	{
		FieldTypeEnum dataType = attr.getDataType().getEnumType();

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
			case IMAGE:
				throw new UnsupportedOperationException("Query with data type [" + dataType + "] not supported");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private String getXRefEqualsInSearchFieldName(AttributeMetaData refIdAttr, String queryField)
	{
		String indexFieldName = queryField + '.' + refIdAttr.getName();
		return getFieldName(refIdAttr, indexFieldName);
	}

	private void validateNumericalQueryField(String queryField, EntityMetaData entityMetaData)
	{
		String[] attributePath = parseAttributePath(queryField);

		FieldTypeEnum dataType = getAttribute(entityMetaData, attributePath).getDataType().getEnumType();

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
			case IMAGE:
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

	/** Returns the target attribute. Looks in the reference entity when it is a nested query. */
	private AttributeMetaData getAttribute(EntityMetaData entityMetaData, String[] attributePath)
	{
		if (attributePath.length > 2) throw new UnsupportedOperationException(
				"Can not filter on references deeper than 1.");
		if (attributePath.length == 0) throw new MolgenisQueryException("Attribute path length is 0!");

		if (attributePath.length == 1)
		{
			AttributeMetaData attr = entityMetaData.getAttribute(attributePath[0]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0]);
			return attr;
		}
		else
		{
			AttributeMetaData attr = entityMetaData.getAttribute(attributePath[0]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0]);

			attr = attr.getRefEntity().getAttribute(attributePath[1]);
			if (attr == null) throw new UnknownAttributeException(attributePath[0] + "." + attributePath[1]);

			return attr;
		}
	}
}
