package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.mockito.Matchers;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchRequestGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
	}

	@Test
	public void testBuildSearchRequest()
	{
		SearchRequestGenerator gen = new SearchRequestGenerator();
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;
		List<String> fieldsToReturn = Arrays.asList("field1", "field2");

		gen.buildSearchRequest(searchRequestBuilderMock, entityName, searchType, new QueryImpl().search("test"),
				fieldsToReturn, null, null);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).addFields(new String[]
		{ "field1", "field2" });
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder> anyObject());
	}
}
