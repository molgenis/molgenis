package org.molgenis.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.NESTED;
import static org.molgenis.data.QueryRule.Operator.OR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;

public class QueryGeneratorHelper
{
	private final EntityMetaData entityMetaData;
	private final LinkedHashMap<BaseQueryBuilder, Operator> baseQueryCollection;
	private final List<QueryRule> queryRules;
	private final List<QueryRule> mrefRulesOfSameField;

	public QueryGeneratorHelper(List<QueryRule> rules, EntityMetaData metaData)
	{
		if (rules.size() == 0) throw new RuntimeException("The queryRules cannot be empty : " + rules);
		baseQueryCollection = new LinkedHashMap<BaseQueryBuilder, Operator>(rules.size());
		mrefRulesOfSameField = new ArrayList<QueryRule>();
		queryRules = rules;
		entityMetaData = metaData;
	}

	public BaseQueryBuilder generateQuery()
	{
		StringBuilder mrefField = new StringBuilder();

		for (QueryRule queryRule : queryRules)
		{
			// Jump to next round if the queryRule is just logic operators
			if (queryRule.getOperator().equals(AND) || queryRule.getOperator().equals(OR)) continue;

			// Take special care of NESTED operator
			if (queryRule.getOperator().equals(NESTED))
			{
				QueryGeneratorHelper helper = new QueryGeneratorHelper(queryRule.getNestedRules(), entityMetaData);
				baseQueryCollection.put(helper.generateQuery(), getLogicOperator(queryRule));
			}
			else if (isMref(queryRule))
			{
				// Initialize the mrefField the first time
				if (mrefField.length() == 0) mrefField.append(queryRule.getField());

				// Encounter a new mref queryRule add Operator between them to
				// the rules
				if (mrefField.toString().equals(queryRule.getField()))
				{
					// Only add logic operator to mrefRules when there are mref
					// queryRules already stored
					if (mrefRulesOfSameField.size() > 0)
					{
						mrefRulesOfSameField.add(new QueryRule(getLogicOperator(queryRule)));
					}
				}
				else
				{
					// Deal with the case where the next mref does not have the
					// same
					// field name as current mref queryRule
					addNestedQueryToCollection();
				}
				mrefField.delete(0, mrefField.length()).append(queryRule.getField());
				mrefRulesOfSameField.add(queryRule);
			}
			else
			{
				// Generate nesteQuery for MREF if there are any
				// queryRules left
				// in mrefRules list.
				addNestedQueryToCollection();
				mrefField.delete(0, mrefField.length());
				baseQueryCollection.put(
						QueryBuilders.queryString(LuceneQueryStringBuilder.buildQueryString(Arrays.asList(queryRule))),
						getLogicOperator(queryRule));
			}
		}
		// Generate nesteQuery for MREF if there are any
		// queryRules left
		// in mrefRules list.
		addNestedQueryToCollection();

		return combineQueryBuilders();
	}

	/**
	 * A helper function to generate the ElasticSearch NestetQueryBuilder object
	 * for the collected MREFs and add it to the queryBuilder collection
	 */
	private void addNestedQueryToCollection()
	{
		if (mrefRulesOfSameField.size() > 0)
		{
			String path = mrefRulesOfSameField.get(0).getField();
			StringBuilder queryStringBuilder = new StringBuilder();
			queryStringBuilder.append('(');
			for (QueryRule rule : mrefRulesOfSameField)
			{
				if (rule.getField() == null)
				{

					queryStringBuilder.append(' ').append(rule.getOperator().toString()).append(' ');
				}
				else
				{
					EntityMetaData refEntity = entityMetaData.getAttribute(rule.getField()).getRefEntity();
					String nestedField = rule.getField() + "." + refEntity.getLabelAttribute().getName();
					queryStringBuilder.append(nestedField).append(':').append("\"").append(rule.getValue())
							.append("\"");
				}
			}
			queryStringBuilder.append(')');

			baseQueryCollection.put(
					QueryBuilders.nestedQuery(path, QueryBuilders.queryString(queryStringBuilder.toString())),
					getLogicOperator(mrefRulesOfSameField.get(0)));

			mrefRulesOfSameField.clear();
		}
	}

	/**
	 * A helper function to get the connected Operator (AND/OR)
	 * 
	 * @param queryRule
	 * @param queryBuilder
	 */
	private Operator getLogicOperator(QueryRule queryRule)
	{
		// Initialize the Operator with 'AND' always
		QueryRule operator = new QueryRule(AND);
		int index = queryRules.indexOf(queryRule);
		// if the index is 1, check the next possible operator
		if (index == 0)
		{
			if (queryRules.size() > 1)
			{
				operator = queryRules.get(1);
			}
		}
		else
		{
			operator = queryRules.get(index - 1);
		}

		if (operator.getOperator() != null && (operator.getOperator().equals(AND) || operator.getOperator().equals(OR)))
		{
			return operator.getOperator();
		}
		else
		{
			throw new RuntimeException("The query operator is not valid : " + operator.toString());
		}
	}

	/**
	 * A helper function that checks if the attribute field is a MREF field
	 * 
	 * @param queryRule
	 * @return
	 */
	private boolean isMref(QueryRule queryRule)
	{
		if (entityMetaData == null || queryRule.getField() == null) return false;

		return entityMetaData.getAttribute(queryRule.getField()).getDataType().getEnumType().toString()
				.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString());
	}

	/**
	 * A helper function to generate a ElasticSearch BaseQueryBuilder Object
	 * based on previously collected QueryBuilders. Create a BoolQueryBuilder
	 * only if there are more than one queryBuilder in the collection, otherwise
	 * only return the first QueryBuilder.
	 * 
	 * @return
	 */
	private BaseQueryBuilder combineQueryBuilders()
	{
		if (baseQueryCollection.size() == 1)
		{
			Iterator<BaseQueryBuilder> iterator = baseQueryCollection.keySet().iterator();
			return iterator.next();
		}
		else
		{
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (Entry<BaseQueryBuilder, Operator> entry : baseQueryCollection.entrySet())
			{
				if (entry.getValue().equals(AND))
				{
					boolQueryBuilder.must(entry.getKey());
				}
				else if (entry.getValue().equals(OR))
				{
					boolQueryBuilder.should(entry.getKey());
				}
			}
			return boolQueryBuilder;
		}
	}
}