package org.molgenis.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Query;
import org.molgenis.elasticsearch.index.MappingsBuilder;

/**
 * Builds a ElasticSearch search request
 * 
 * @author erwin
 * 
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryPartGenerator> generators = Arrays.asList(new QueryGenerator(),
			new SortGenerator(), new LimitOffsetGenerator(), new DisMaxQueryGenerator());

	/**
	 * Add the 'searchType', 'fields', 'types' and 'query' of the SearchRequestBuilder
	 * 
	 * @param searchRequestBuilder
	 * @param entityNames
	 * @param searchType
	 * @param query
	 * @param fieldsToReturn
	 * @param aggregateField1
	 *            First Field to aggregate on, attributemetadata instead of name because of elasticsearch bug:
	 *            http://elasticsearch
	 *            -users.115913.n3.nabble.com/boolean-multi-field-silently-ignored-in-1-2-1-td4058107.html
	 * @param aggregateField2
	 *            Second Field to aggregate on, attributemetadata instead of name because of elasticsearch bug:
	 *            http://elasticsearch
	 *            -users.115913.n3.nabble.com/boolean-multi-field-silently-ignored-in-1-2-1-td4058107.html
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, List<String> entityNames,
			SearchType searchType, Query query, List<String> fieldsToReturn, AttributeMetaData aggregateField1,
			AttributeMetaData aggregateField2)
	{
		searchRequestBuilder.setSearchType(searchType);

		/*
		 * determine correct aggregateFieldNames (http://elasticsearch
		 * -users.115913.n3.nabble.com/boolean-multi-field-silently-ignored-in-1-2-1-td4058107.html)
		 */
		String aggregateFieldName1 = aggregateField1 != null ? aggregateField1.getName() : null;
		String aggregateFieldName2 = aggregateField2 != null ? aggregateField2.getName() : null;
		String aggregateFieldName_not_analysed1 = aggregateField1 != null ? aggregateField1.getDataType().equals(
				MolgenisFieldTypes.BOOL) ? aggregateFieldName1 : aggregateFieldName1 + '.'
				+ MappingsBuilder.FIELD_NOT_ANALYZED : null;
		String aggregateFieldName_not_analysed2 = aggregateField2 != null ? aggregateField2.getDataType().equals(
				MolgenisFieldTypes.BOOL) ? aggregateFieldName2 : aggregateFieldName2 + '.'
				+ MappingsBuilder.FIELD_NOT_ANALYZED : null;

		// Document type
		if (entityNames != null)
		{
			searchRequestBuilder.setTypes(entityNames.toArray(new String[entityNames.size()]));
		}

		// Fields
		if ((fieldsToReturn != null) && !fieldsToReturn.isEmpty())
		{
			searchRequestBuilder.addFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
		}

		// Generate query
		for (QueryPartGenerator generator : generators)
		{
			generator.generate(searchRequestBuilder, query);
		}

		// Aggregates
		if (StringUtils.isNotBlank(aggregateFieldName1) || StringUtils.isNotBlank(aggregateFieldName2))
		{
			searchRequestBuilder.setSize(0);

			TermsBuilder termsBuilder;
			if (StringUtils.isNotBlank(aggregateFieldName1) && StringUtils.isNotBlank(aggregateFieldName2))
			{
				termsBuilder = new TermsBuilder(aggregateFieldName1).size(Integer.MAX_VALUE).field(
						aggregateFieldName_not_analysed1);
				TermsBuilder subTermsBuilder = new TermsBuilder(aggregateFieldName2).size(Integer.MAX_VALUE).field(
						aggregateFieldName_not_analysed2);
				termsBuilder.subAggregation(subTermsBuilder);
			}
			else if (StringUtils.isNotBlank(aggregateFieldName1))
			{
				termsBuilder = new TermsBuilder(aggregateFieldName1).size(Integer.MAX_VALUE).field(
						aggregateFieldName_not_analysed1);
			}
			else
			{
				termsBuilder = new TermsBuilder(aggregateFieldName2).size(Integer.MAX_VALUE).field(
						aggregateFieldName_not_analysed2);
			}
			searchRequestBuilder.addAggregation(termsBuilder);
		}
	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, List<String> fieldsToReturn, AttributeMetaData aggregateField1,
			AttributeMetaData aggregateField2)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, fieldsToReturn, aggregateField1, aggregateField2);
	}
}
