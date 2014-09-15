package org.molgenis.data.elasticsearch.request;

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.UnknownAttributeException;

/**
 * Creates Elasticsearch query from MOLGENIS query
 */
public class QueryGenerator implements QueryPartGenerator
{
	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		List<QueryRule> queryRules = query.getRules();
		if (queryRules == null || queryRules.isEmpty()) return;
		searchRequestBuilder.setQuery(createQueryBuilder(queryRules, entityMetaData));
	}

	private QueryBuilder createQueryBuilder(List<QueryRule> queryRules, EntityMetaData entityMetaData)
	{
		QueryBuilder queryBuilder;

		final int nrQueryRules = queryRules.size();
		if (nrQueryRules == 1)
		{
			queryBuilder = createQueryClause(queryRules.get(0), entityMetaData);
		}
		else
		{

			Operator occur = null;
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			for (int i = 0; i < nrQueryRules; i += 2)
			{
				QueryRule queryRule = queryRules.get(i);

				// read ahead to retrieve and validate occur operator
				if (nrQueryRules == 1)
				{
					occur = Operator.AND;
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
						case NOT:
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
	@SuppressWarnings("deprecation")
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
				throw new UnsupportedOperationException("Unexpected query operator [" + queryOperator + ']');
			case SHOULD:
			case DIS_MAX:
				// SHOULD and DIS_MAX are handled in DisMaxQueryGenerator
				// TODO merge DisMaxQueryGenerator with this class
				return null;
			case EQUALS:
			{
				// As a general rule, filters should be used instead of queries:
				// - for binary yes/no searches
				// - for queries on exact values

				AttributeMetaData attr = entityMetaData.getAttribute(queryField);
				if (attr == null) throw new UnknownAttributeException(queryField);

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
							filterBuilder = FilterBuilders.termFilter(queryField, queryValue);
							break;
						case EMAIL:
						case ENUM:
						case HTML:
						case HYPERLINK:
						case SCRIPT:
						case STRING:
						case TEXT:
							filterBuilder = FilterBuilders.termFilter(queryField + '.'
									+ MappingsBuilder.FIELD_NOT_ANALYZED, queryValue);
							break;
						case CATEGORICAL:
						case MREF:
						case XREF:
							AttributeMetaData refIdAttr = attr.getRefEntity().getIdAttribute();
							filterBuilder = FilterBuilders.nestedFilter(queryField,
									FilterBuilders.termFilter(queryField + '.' + refIdAttr.getName(), queryValue));
							break;
						case COMPOUND:
						case FILE:
						case IMAGE:
							throw new UnsupportedOperationException();
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
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gt(queryValue);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case GREATER_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).gte(queryValue);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case IN:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				FilterBuilder filterBuilder = FilterBuilders.inFilter(queryField, queryValue);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).lt(queryValue);
				queryBuilder = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
				break;
			}
			case LESS_EQUAL:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");
				FilterBuilder filterBuilder = FilterBuilders.rangeFilter(queryField).lte(queryValue);
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
			case SEARCH:
			{
				if (queryValue == null) throw new MolgenisQueryException("Query value cannot be null");

				// 1. attribute: search in attribute
				// 2. no attribute: search in all
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
							queryBuilder = QueryBuilders.matchQuery(queryField, queryValue);
							break;
						case CATEGORICAL:
						case MREF:
						case XREF:
							queryBuilder = QueryBuilders.nestedQuery(queryField,
									QueryBuilders.matchQuery(queryField + '.' + "_all", queryValue));
							break;
						case COMPOUND:
						case FILE:
						case IMAGE:
							throw new UnsupportedOperationException();
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
}
