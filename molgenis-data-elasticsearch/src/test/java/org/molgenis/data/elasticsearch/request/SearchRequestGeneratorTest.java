package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.mockito.Matchers;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class SearchRequestGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;
	private EntityType entityType;
	private SearchRequestGenerator searchRequestGenerator;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
		entityType = mock(EntityType.class);

		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
		when(documentIdGenerator.generateId(any(Attribute.class)))
				.thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());
		searchRequestGenerator = new SearchRequestGenerator(documentIdGenerator);
	}

	@Test
	public void testBuildSearchRequest()
	{
		when(entityType.getBackend()).thenReturn("notElasticsearch");

		String entityName = "test";
		SearchType searchType = SearchType.COUNT;
		searchRequestGenerator.buildSearchRequest(searchRequestBuilderMock, entityName, searchType,
				new QueryImpl<Entity>().search("test").fetch(new Fetch().field("field1").field("field2")), null, null,
				null, entityType);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder>anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}

	@Test
	public void testBuildSearchRequestNoFetch()
	{
		when(entityType.getBackend()).thenReturn("notElasticsearch");
		String entityName = "test";
		SearchType searchType = SearchType.COUNT;

		searchRequestGenerator
				.buildSearchRequest(searchRequestBuilderMock, entityName, searchType, new QueryImpl<>().search("test"),
						null,
				null, null, entityType);
		verify(searchRequestBuilderMock).setFrom(0);
		verify(searchRequestBuilderMock).setSearchType(searchType);
		verify(searchRequestBuilderMock).setTypes(entityName);
		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder>anyObject());
		verifyNoMoreInteractions(searchRequestBuilderMock);
	}
}
