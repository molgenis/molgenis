package org.molgenis.data.elasticsearch.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;

/**
 * For entities that (besides indexing) store their data in Elasticsearch select the source attributes to return based
 * on the query fetch.
 */
public class SourceFilteringGenerator implements QueryPartGenerator
{
	@Override
	public void generate(SearchRequestBuilder searchRequestBuilder, Query query, EntityMetaData entityMetaData)
	{
		if (ElasticsearchRepositoryCollection.NAME.equals(entityMetaData.getBackend()))
		{
			Fetch fetch = query.getFetch();
			if (fetch != null)
			{
				String[] fields = toFetchFields(fetch);
				searchRequestBuilder.setFetchSource(fields, null);
			}
		}
	}

	public static String[] toFetchFields(Fetch fetch)
	{
		Set<String> fields = fetch.getFields();
		List<String> sourceIncludes = new ArrayList<>();
		fields.forEach(field -> {
			Fetch subFetch = fetch.getFetch(field);
			if (subFetch != null)
			{
				// filter nested types. Sub fetches of sub fetches are ignored because nested
				// types do not contain nested types (due to the indexing depth)
				Set<String> subFields = subFetch.getFields();
				subFields.forEach(subField -> {
					sourceIncludes.add(field + '.' + subField);
				});
			}
			else
			{
				sourceIncludes.add(field);
			}
		});
		return sourceIncludes.toArray(new String[sourceIncludes.size()]);
	}
}
