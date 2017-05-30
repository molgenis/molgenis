package org.molgenis.data.elasticsearch.request;

public class SearchRequestGeneratorTest
{
	// FIXME
	//	private SearchRequestBuilder searchRequestBuilderMock;
	//	private EntityType entityType;
	//	private SearchRequestGenerator searchRequestGenerator;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
	//		entityType = when(mock(EntityType.class).getId()).thenReturn("test").getMock();
	//
	//		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
	//		when(documentIdGenerator.generateId(any(EntityType.class)))
	//				.thenAnswer(invocation -> ((EntityType) invocation.getArguments()[0]).getId());
	//		when(documentIdGenerator.generateId(any(Attribute.class)))
	//				.thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());
	//		searchRequestGenerator = new SearchRequestGenerator(documentIdGenerator);
	//	}
	//
	//	@Test
	//	public void testBuildSearchRequest()
	//	{
	//		when(entityType.getBackend()).thenReturn("notElasticsearch");
	//
	//		String entityTypeId = "test";
	//		SearchType searchType = SearchType.QUERY_THEN_FETCH; // FIXME used to be COUNT
	//
	//		searchRequestGenerator.buildSearchRequest(searchRequestBuilderMock, searchType, entityType,
	//				new QueryImpl<Entity>().search("test").fetch(new Fetch().field("field1").field("field2")), null, null,
	//				null);
	//		verify(searchRequestBuilderMock).setFrom(0);
	//		verify(searchRequestBuilderMock).setSearchType(searchType);
	//		verify(searchRequestBuilderMock).setTypes(entityTypeId);
	//		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder>anyObject());
	//		verifyNoMoreInteractions(searchRequestBuilderMock);
	//	}
	//
	//	@Test
	//	public void testBuildSearchRequestNoFetch()
	//	{
	//		when(entityType.getBackend()).thenReturn("notElasticsearch");
	//		String entityTypeId = "test";
	//		SearchType searchType = SearchType.QUERY_THEN_FETCH; // FIXME used to be COUNT
	//
	//		searchRequestGenerator
	//				.buildSearchRequest(searchRequestBuilderMock, searchType, entityType, new QueryImpl<>().search("test"),
	//						null, null, null);
	//		verify(searchRequestBuilderMock).setFrom(0);
	//		verify(searchRequestBuilderMock).setSearchType(searchType);
	//		verify(searchRequestBuilderMock).setTypes(entityTypeId);
	//		verify(searchRequestBuilderMock).setQuery(Matchers.<QueryBuilder>anyObject());
	//		verifyNoMoreInteractions(searchRequestBuilderMock);
	//	}
}
