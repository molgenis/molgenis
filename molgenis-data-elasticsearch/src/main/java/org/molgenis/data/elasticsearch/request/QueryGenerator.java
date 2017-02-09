package org.molgenis.data.elasticsearch.request;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.MolgenisDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator.DEFAULT_ANALYZER;
import static org.molgenis.data.elasticsearch.index.MappingsBuilder.FIELD_NOT_ANALYZED;

/**
 * Creates Elasticsearch query from MOLGENIS query
 */
public class QueryGenerator implements QueryPartGenerator
{
	static final String ATTRIBUTE_SEPARATOR = ".";

	private final DocumentIdGenerator documentIdGenerator;

	public QueryGenerator(DocumentIdGenerator documentIdGenerator)
	{

		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

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

					//noinspection EnumSwitchStatementWhichMissesCases
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
						default:
							throw new MolgenisQueryException(
									"Expected query occur operator instead of [" + occurOperator + "]");
					}
				}

				QueryBuilder queryPartBuilder = createQueryClause(queryRule, entityType);
				if (queryPartBuilder == null) continue; // skip SHOULD and DIS_MAX query rules

				// add query part to query
				//noinspection EnumSwitchStatementWhichMissesCases
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
					default:
						throw new MolgenisQueryException("Unknown occurence operator [" + occur + "]");
				}
			}
			queryBuilder = boolQuery;
		}
		return queryBuilder;
	}

	private QueryBuilder createQueryClause(QueryRule queryRule, EntityType entityType)
	{
		Operator queryOperator = queryRule.getOperator();
		switch (queryOperator)
		{
			case DIS_MAX:
				return createQueryClauseDisMax(queryRule, entityType);
			case EQUALS:
				return createQueryClauseEquals(queryRule, entityType);
			case FUZZY_MATCH:
				return createQueryClauseFuzzyMatch(queryRule, entityType);
			case FUZZY_MATCH_NGRAM:
				return createQueryClauseFuzzyMatchNgram(queryRule, entityType);
			case GREATER:
			case GREATER_EQUAL:
			case LESS:
			case LESS_EQUAL:
				return createQueryClauseRangeOpen(queryRule, entityType);
			case IN:
				return createQueryClauseIn(queryRule, entityType);
			case LIKE:
				return createQueryClauseLike(queryRule, entityType);
			case NESTED:
				return createQueryClauseNested(queryRule, entityType);
			case RANGE:
				return createQueryClauseRangeClosed(queryRule, entityType);
			case SEARCH:
				return createQueryClauseSearch(queryRule, entityType);
			case SHOULD:
				return createQueryClauseShould(queryRule, entityType);
			case AND:
			case OR:
			case NOT:
				throw new MolgenisQueryException(format("Unexpected query operator [%s]", queryOperator.toString()));
			default:
				throw new MolgenisQueryException(format("Unknown query operator [%s]", queryOperator.toString()));
		}
	}

	private QueryBuilder createQueryClauseDisMax(QueryRule queryRule, EntityType entityType)
	{
		DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
		for (QueryRule nestedQueryRule : queryRule.getNestedRules())
		{
			disMaxQueryBuilder.add(createQueryClause(nestedQueryRule, entityType));
		}
		disMaxQueryBuilder.tieBreaker((float) 0.0);
		if (queryRule.getValue() != null)
		{
			disMaxQueryBuilder.boost(Float.parseFloat(queryRule.getValue().toString()));
		}
		return disMaxQueryBuilder;
	}

	private QueryBuilder createQueryClauseEquals(QueryRule queryRule, EntityType entityType)
	{
		FilterBuilder filterBuilder;
		if (queryRule.getValue() != null)
		{
			filterBuilder = createQueryClauseEqualsValue(queryRule, entityType);
		}
		else
		{
			filterBuilder = createQueryClauseEqualsNoValue(queryRule, entityType);
		}
		return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
	}

	private FilterBuilder createQueryClauseEqualsValue(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);
		Object queryValue = getQueryValue(attr, queryRule.getValue());

		String fieldName = getQueryFieldName(attributePath);
		AttributeType attrType = attr.getDataType();
		switch (attrType)
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
				if (useNotAnalyzedField(attr))
				{
					fieldName = fieldName + '.' + FIELD_NOT_ANALYZED;
				}
				return nestedFilterBuilder(attributePath, FilterBuilders.termFilter(fieldName, queryValue));
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case XREF:
			case MREF:
			case FILE:
			case ONE_TO_MANY:
				if (attributePath.size() > 1)
				{
					throw new MolgenisQueryException("Can not filter on references deeper than 1.");
				}

				Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
				List<Attribute> refAttributePath = concat(attributePath.stream(), of(refIdAttr)).collect(toList());
				String indexFieldName = getQueryFieldName(refAttributePath);
				if (useNotAnalyzedField(refIdAttr))
				{
					indexFieldName = indexFieldName + '.' + FIELD_NOT_ANALYZED;
				}
				return FilterBuilders.nestedFilter(fieldName, FilterBuilders.termFilter(indexFieldName, queryValue));
			case COMPOUND:
				throw new MolgenisQueryException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	private FilterBuilder createQueryClauseEqualsNoValue(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);

		String fieldName = getQueryFieldName(attributePath);
		Attribute attr = attributePath.get(0);
		AttributeType attrType = attr.getDataType();
		switch (attrType)
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
				return FilterBuilders.missingFilter(fieldName).existence(true).nullValue(true);
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				if (attributePath.size() > 1)
				{
					throw new MolgenisQueryException("Can not filter on references deeper than 1.");
				}

				Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
				List<Attribute> refAttributePath = concat(attributePath.stream(), of(refIdAttr)).collect(toList());
				String indexFieldName = getQueryFieldName(refAttributePath);

				// see https://github.com/elastic/elasticsearch/issues/3495
				return FilterBuilders
						.notFilter(FilterBuilders.nestedFilter(fieldName, FilterBuilders.existsFilter(indexFieldName)));
			case COMPOUND:
				throw new MolgenisQueryException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	private QueryBuilder createQueryClauseFuzzyMatch(QueryRule queryRule, EntityType entityType)
	{
		String queryField = queryRule.getField();
		Object queryValue = queryRule.getValue();

		QueryBuilder queryBuilder;
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
					queryBuilder = QueryBuilders.queryStringQuery(getQueryFieldName(attr) + ":(" + queryValue + ")");
					break;
				case MREF:
				case XREF:
				case CATEGORICAL:
				case CATEGORICAL_MREF:
				case ONE_TO_MANY:
				case FILE:
					queryField =
							getQueryFieldName(attr) + "." + getQueryFieldName(attr.getRefEntity().getLabelAttribute());
					queryBuilder = QueryBuilders.nestedQuery(getQueryFieldName(attr),
							QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")")).scoreMode("max");
					break;
				case BOOL:
				case COMPOUND:
					throw new MolgenisQueryException(
							"Illegal data type [" + dataType + "] for operator [" + Operator.FUZZY_MATCH + "]");
				default:
					throw new RuntimeException("Unknown data type [" + dataType + "]");
			}
		}
		return queryBuilder;
	}

	private QueryBuilder createQueryClauseFuzzyMatchNgram(QueryRule queryRule, EntityType entityType)
	{
		String queryField = queryRule.getField();
		Object queryValue = queryRule.getValue();

		QueryBuilder queryBuilder;
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
					queryField = getQueryFieldName(attr) + ".ngram";
					queryBuilder = QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")");
					break;
				case MREF:
				case XREF:
					queryField =
							getQueryFieldName(attr) + "." + getQueryFieldName(attr.getRefEntity().getLabelAttribute())
									+ ".ngram";
					queryBuilder = QueryBuilders.nestedQuery(getQueryFieldName(attr),
							QueryBuilders.queryStringQuery(queryField + ":(" + queryValue + ")")).scoreMode("max");
					break;
				default:
					throw new RuntimeException("Unknown data type [" + dataType + "]");
			}
		}
		return queryBuilder;
	}

	private QueryBuilder createQueryClauseIn(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);

		Object queryRuleValue = queryRule.getValue();
		if (queryRuleValue == null)
		{
			throw new MolgenisQueryException("Query value cannot be null");
		}
		if (!(queryRuleValue instanceof Iterable<?>))
		{
			throw new MolgenisQueryException(
					"Query value must be a Iterable instead of [" + queryRuleValue.getClass().getSimpleName() + "]");
		}
		Object[] queryValues = StreamSupport.stream(((Iterable<?>) queryRuleValue).spliterator(), false)
				.map(aQueryRuleValue -> getQueryValue(attr, aQueryRuleValue)).toArray();

		FilterBuilder filterBuilder;
		String fieldName = getQueryFieldName(attr);
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
				if (useNotAnalyzedField(attr))
				{
					fieldName = fieldName + '.' + FIELD_NOT_ANALYZED;
				}
				// note: inFilter expects array, not iterable
				filterBuilder = FilterBuilders.inFilter(fieldName, queryValues);
				filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
				break;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case MREF:
			case XREF:
			case FILE:
			case ONE_TO_MANY:
				if (attributePath.size() > 1)
				{
					throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
				}

				Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
				List<Attribute> refAttributePath = concat(attributePath.stream(), of(refIdAttr)).collect(toList());
				String indexFieldName = getQueryFieldName(refAttributePath);
				if (useNotAnalyzedField(refIdAttr))
				{
					indexFieldName = indexFieldName + '.' + FIELD_NOT_ANALYZED;
				}
				filterBuilder = FilterBuilders.inFilter(indexFieldName, queryValues);
				filterBuilder = FilterBuilders.nestedFilter(fieldName, filterBuilder);
				break;
			case COMPOUND:
				throw new MolgenisQueryException(
						"Illegal data type [" + dataType + "] for operator [" + Operator.IN + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
		return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
	}

	private QueryBuilder createQueryClauseLike(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);
		Object queryValue = getQueryValue(attr, queryRule.getValue());

		String fieldName = getQueryFieldName(attributePath);
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case EMAIL:
			case ENUM:
			case HYPERLINK:
			case STRING:
				return nestedQueryBuilder(attributePath,
						QueryBuilders.matchQuery(fieldName + '.' + MappingsBuilder.FIELD_NGRAM_ANALYZED, queryValue)
								.analyzer(DEFAULT_ANALYZER));
			case BOOL:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case INT:
			case LONG:
				throw new MolgenisQueryException(format("Illegal data type [%s] for operator [%s]", attrType, LIKE));
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case HTML: // due to size would result in large amount of ngrams
			case MREF:
			case ONE_TO_MANY:
			case SCRIPT: // due to size would result in large amount of ngrams
			case TEXT: // due to size would result in large amount of ngrams
			case XREF:
				throw new UnsupportedOperationException(
						format("Unsupported data type [%s] for operator [%s]", attrType, LIKE));
			default:
				throw new RuntimeException("Unknown data type [" + attrType + "]");
		}
	}

	private QueryBuilder createQueryClauseNested(QueryRule queryRule, EntityType entityType)
	{
		List<QueryRule> nestedQueryRules = queryRule.getNestedRules();
		if (nestedQueryRules == null || nestedQueryRules.isEmpty())
		{
			throw new MolgenisQueryException("Missing nested rules for nested query");
		}
		return createQueryBuilder(nestedQueryRules, entityType);
	}

	private QueryBuilder createQueryClauseRangeClosed(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);
		validateNumericalQueryField(attr);
		String fieldName = getQueryFieldName(attributePath);

		Object queryValue = getQueryValue(attr, queryRule.getValue());
		if (queryValue == null)
		{
			throw new MolgenisQueryException("Query value cannot be null");
		}
		if (!(queryValue instanceof Iterable<?>))
		{
			throw new MolgenisQueryException(
					format("Query value must be a Iterable instead of [%s]", queryValue.getClass().getSimpleName()));
		}
		Iterator<?> queryValuesIterator = ((Iterable<?>) queryValue).iterator();
		Object queryValueFrom = getQueryValue(attr, queryValuesIterator.next());
		Object queryValueTo = getQueryValue(attr, queryValuesIterator.next());

		FilterBuilder filterBuilder = FilterBuilders.rangeFilter(fieldName).gte(queryValueFrom).lte(queryValueTo);
		filterBuilder = nestedFilterBuilder(attributePath, filterBuilder);
		return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filterBuilder);
	}

	private QueryBuilder createQueryClauseRangeOpen(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);
		validateNumericalQueryField(attr);
		String fieldName = getQueryFieldName(attributePath);

		Object queryValue = getQueryValue(attr, queryRule.getValue());
		if (queryValue == null)
		{
			throw new MolgenisQueryException("Query value cannot be null");
		}

		RangeFilterBuilder filterBuilder = FilterBuilders.rangeFilter(fieldName);
		Operator operator = queryRule.getOperator();
		switch (operator)
		{
			case GREATER:
				filterBuilder = filterBuilder.gt(queryValue);
				break;
			case GREATER_EQUAL:
				filterBuilder = filterBuilder.gte(queryValue);
				break;
			case LESS:
				filterBuilder = filterBuilder.lt(queryValue);
				break;
			case LESS_EQUAL:
				filterBuilder = filterBuilder.lte(queryValue);
				break;
			case AND:
			case DIS_MAX:
			case EQUALS:
			case FUZZY_MATCH:
			case FUZZY_MATCH_NGRAM:
			case IN:
			case LIKE:
			case NESTED:
			case NOT:
			case OR:
			case RANGE:
			case SEARCH:
			case SHOULD:
				throw new MolgenisQueryException(format("Illegal query rule operator [%s]", operator.toString()));
			default:
				throw new RuntimeException(format("Unknown query operator [%s]", operator.toString()));
		}

		return QueryBuilders
				.filteredQuery(QueryBuilders.matchAllQuery(), nestedFilterBuilder(attributePath, filterBuilder));
	}

	private QueryBuilder createQueryClauseSearch(QueryRule queryRule, EntityType entityType)
	{
		if (queryRule.getValue() == null)
		{
			throw new MolgenisQueryException("Query value cannot be null");
		}

		if (queryRule.getField() == null)
		{
			return createQueryClauseSearchAll(queryRule);
		}
		else
		{
			return createQueryClauseSearchAttribute(queryRule, entityType);
		}
	}

	private QueryBuilder createQueryClauseSearchAll(QueryRule queryRule)
	{
		return QueryBuilders.matchPhraseQuery("_all", queryRule.getValue()).slop(10);
	}

	private QueryBuilder createQueryClauseSearchAttribute(QueryRule queryRule, EntityType entityType)
	{
		List<Attribute> attributePath = getAttributePath(queryRule.getField(), entityType);
		Attribute attr = attributePath.get(attributePath.size() - 1);
		Object queryValue = getQueryValue(attr, queryRule.getValue());

		String fieldName = getQueryFieldName(attributePath);
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
				return nestedQueryBuilder(attributePath, QueryBuilders.matchQuery(fieldName, queryValue));
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
			case FILE:
				if (attributePath.size() > 1)
				{
					throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
				}
				return QueryBuilders
						.nestedQuery(fieldName, QueryBuilders.matchQuery(fieldName + '.' + "_all", queryValue));
			case BOOL:
				throw new MolgenisQueryException("Cannot execute search query on [" + dataType + "] attribute");
			case COMPOUND:
				throw new MolgenisQueryException(
						"Illegal data type [" + dataType + "] for operator [" + Operator.SEARCH + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private QueryBuilder createQueryClauseShould(QueryRule queryRule, EntityType entityType)
	{
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		for (QueryRule subQuery : queryRule.getNestedRules())
		{
			boolQueryBuilder.should(createQueryClause(subQuery, entityType));
		}
		return boolQueryBuilder;
	}

	private boolean useNotAnalyzedField(Attribute attr)
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
				return false;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return true;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case XREF:
			case MREF:
			case FILE:
			case ONE_TO_MANY:
				return useNotAnalyzedField(attr.getRefEntity().getIdAttribute());
			case COMPOUND:
				throw new MolgenisQueryException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	private void validateNumericalQueryField(Attribute attr)
	{
		AttributeType dataType = attr.getDataType();

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
			case ONE_TO_MANY:
			case SCRIPT:
			case STRING:
			case TEXT:
			case XREF:
				throw new MolgenisQueryException("Range query not allowed for type [" + dataType + "]");
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private List<Attribute> getAttributePath(String queryRuleField, EntityType entityType)
	{
		String[] queryRuleFieldTokens = queryRuleField.split("\\" + ATTRIBUTE_SEPARATOR);
		List<Attribute> attributePath = new ArrayList<>(queryRuleFieldTokens.length);
		EntityType entityTypeAtCurrentDepth = entityType;
		for (int depth = 0; depth < queryRuleFieldTokens.length; ++depth)
		{
			Attribute attribute = entityTypeAtCurrentDepth.getAttribute(queryRuleFieldTokens[depth]);
			if (attribute == null)
			{
				throw new UnknownAttributeException(format("Unknown attribute [%s]", queryRuleFieldTokens[depth]));
			}
			attributePath.add(attribute);

			if (depth + 1 < queryRuleFieldTokens.length)
			{
				entityTypeAtCurrentDepth = attribute.getRefEntity();
				if (entityTypeAtCurrentDepth == null)
				{
					throw new MolgenisQueryException(
							format("Invalid query field [%s]: attribute [%s] does not refer to another entity",
									queryRuleField, attribute.getName()));
				}
			}
		}
		return attributePath;
	}

	/**
	 * Wraps the filter in a nested filter when a query is done on a reference entity. Returns the original filter when
	 * it is applied to the current entity.
	 */
	private FilterBuilder nestedFilterBuilder(List<Attribute> attributePath, FilterBuilder filterBuilder)
	{
		if (attributePath.size() == 1)
		{
			return filterBuilder;
		}
		else if (attributePath.size() == 2)
		{
			return FilterBuilders.nestedFilter(getQueryFieldName(attributePath.get(0)), filterBuilder);
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
	private QueryBuilder nestedQueryBuilder(List<Attribute> attributePath, QueryBuilder queryBuilder)
	{
		if (attributePath.size() == 1)
		{
			return queryBuilder;
		}
		else if (attributePath.size() == 2)
		{
			return QueryBuilders.nestedQuery(getQueryFieldName(attributePath.get(0)), queryBuilder);
		}
		else
		{
			throw new UnsupportedOperationException("Can not filter on references deeper than 1.");
		}
	}

	private String getFieldName(QueryRule queryRule, EntityType entityType)
	{
		String queryRuleFieldName = queryRule.getField();
		if (queryRuleFieldName == null)
		{
			return null;
		}

		StringBuilder fieldNameBuilder = new StringBuilder();
		String[] queryRuleFieldNameTokens = StringUtils.split(queryRuleFieldName, '.');
		EntityType entityTypeAtDepth = entityType;
		for (int depth = 0; depth < queryRuleFieldNameTokens.length; ++depth)
		{
			String attrName = queryRuleFieldNameTokens[depth];
			Attribute attr = entityTypeAtDepth.getAttribute(attrName);
			if (attr == null)
			{
				throw new UnknownAttributeException(format("Unknown attribute [%s] of entity type [%s]", attrName,
						entityTypeAtDepth.getFullyQualifiedName()));
			}
			if (depth > 0)
			{
				fieldNameBuilder.append(ATTRIBUTE_SEPARATOR);
			}
			fieldNameBuilder.append(getQueryFieldName(attr));

			if (depth < queryRuleFieldNameTokens.length - 1)
			{
				entityTypeAtDepth = attr.getRefEntity();
			}
		}
		return fieldNameBuilder.toString();
	}

	private String getQueryFieldName(List<Attribute> attributePath)
	{
		return attributePath.stream().map(this::getQueryFieldName).collect(joining(ATTRIBUTE_SEPARATOR));
	}

	private String getQueryFieldName(Attribute attribute)
	{
		return documentIdGenerator.generateId(attribute);
	}

	private Object getQueryValue(Attribute attribute, Object queryRuleValue)
	{
		AttributeType attrType = attribute.getDataType();
		switch (attrType)
		{
			case BOOL:
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
				return queryRuleValue;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				return queryRuleValue instanceof Entity ? ((Entity) queryRuleValue).getIdValue() : queryRuleValue;
			case DATE:
			case DATE_TIME:
				if (queryRuleValue instanceof Date)
				{
					return getESDateQueryValue((Date) queryRuleValue, attribute);
				}
				else if (queryRuleValue instanceof String)
				{
					return queryRuleValue;
				}
				else
				{
					throw new MolgenisQueryException(format("Query value must be of type Date instead of [%s]",
							queryRuleValue.getClass().getSimpleName()));
				}
			case COMPOUND:
				throw new MolgenisQueryException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
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
