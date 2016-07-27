package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.mockito.Matchers;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class SearchRequestGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;
	private EntityMetaData entityMeta;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
		entityMeta = mock(EntityMetaData.class);
	}

	@Test
	public void testBuildSearchRequest()
	{
		when(entityMeta.getBackend()).thenReturn("notElasticsearch");
		SearchRequestGenerator gen = new SearchRequestGenerator();
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;
		gen.buildSearchRequest(searchRequestBuilderMock, entityName, searchType,
				new QueryImpl<Entity>().search("test").fetch(new Fetch().field("field1").field("field2")), null, null, null,
				entityMeta);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder> anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}

	@Test
	public void testBuildSearchRequestNoFetch()
	{
		when(entityMeta.getBackend()).thenReturn("notElasticsearch");
		SearchRequestGenerator gen = new SearchRequestGenerator();
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;

		gen.buildSearchRequest(searchRequestBuilderMock, entityName, searchType, new QueryImpl<>().search("test"), null,
				null, null, entityMeta);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder> anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}
}
