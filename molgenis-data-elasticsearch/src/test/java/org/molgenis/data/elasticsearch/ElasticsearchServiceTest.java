package org.molgenis.data.elasticsearch;

public class ElasticsearchServiceTest
{
	//	private Client client;
	//	private ElasticsearchService searchService;
	//	private String indexName;
	//	private DataServiceImpl dataService;
	//	private EntityManager entityManager;
	//	private ElasticsearchEntityFactory elasticsearchEntityFactory;
	//
	//	@BeforeMethod
	//	public void beforeMethod() throws InterruptedException
	//	{
	//		indexName = "molgenis";
	//		client = mock(Client.class);
	//
	//		dataService = spy(new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory()));
	//		entityManager = new EntityManagerImpl(dataService);
	//		SourceToEntityConverter sourceToEntityManager = new SourceToEntityConverter(dataService, entityManager);
	//		EntityToSourceConverter entityToSourceManager = mock(EntityToSourceConverter.class);
	//		elasticsearchEntityFactory = new ElasticsearchEntityFactory(entityManager, sourceToEntityManager,
	//				entityToSourceManager);
	//		BulkProcessorFactory bulkProcessorFactory = mock(BulkProcessorFactory.class);
	//		BulkProcessor bulkProcessor = mock(BulkProcessor.class);
	//		when(bulkProcessor.awaitClose(any(Long.class), any(TimeUnit.class))).thenReturn(true);
	//		when(bulkProcessorFactory.create(client)).thenReturn(bulkProcessor);
	//
	//		ElasticsearchUtils facade = new ElasticsearchUtils(client, bulkProcessorFactory);
	//		searchService = spy(new ElasticsearchService(facade, indexName, dataService, elasticsearchEntityFactory));
	//
	//		doNothing().when(searchService).refresh();
	//	}
	//
	//	@BeforeClass
	//	public void beforeClass()
	//	{
	//	}
	//
	//	@AfterClass
	//	public void afterClass()
	//	{
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Test
	//	public void search()
	//	{
	//		int batchSize = 1000;
	//		int totalSize = batchSize + 1;
	//		SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
	//		String idAttrName = "id";
	//
	//		ListenableActionFuture<SearchResponse> value1 = mock(ListenableActionFuture.class);
	//		SearchResponse searchResponse1 = mock(SearchResponse.class);
	//
	//		SearchHit[] hits1 = new SearchHit[batchSize];
	//		for (int i = 0; i < batchSize; ++i)
	//		{
	//			SearchHit searchHit = mock(SearchHit.class);
	//			when(searchHit.getSource()).thenReturn(Collections.<String, Object> singletonMap(idAttrName, i + 1));
	//			when(searchHit.getId()).thenReturn(String.valueOf(i + 1));
	//			hits1[i] = searchHit;
	//		}
	//		SearchHits searchHits1 = createSearchHits(hits1, totalSize);
	//		when(searchResponse1.getHits()).thenReturn(searchHits1);
	//		when(value1.actionGet()).thenReturn(searchResponse1);
	//
	//		ListenableActionFuture<SearchResponse> value2 = mock(ListenableActionFuture.class);
	//		SearchResponse searchResponse2 = mock(SearchResponse.class);
	//
	//		SearchHit[] hits2 = new SearchHit[totalSize - batchSize];
	//		SearchHit searchHit = mock(SearchHit.class);
	//		when(searchHit.getSource()).thenReturn(Collections.<String, Object> singletonMap(idAttrName, batchSize + 1));
	//		when(searchHit.getId()).thenReturn(String.valueOf(batchSize + 1));
	//		hits2[0] = searchHit;
	//		SearchHits searchHits2 = createSearchHits(hits2, totalSize);
	//		when(searchResponse2.getHits()).thenReturn(searchHits2);
	//		when(value2.actionGet()).thenReturn(searchResponse2);
	//
	//		when(searchRequestBuilder.execute()).thenReturn(value1, value2);
	//		when(client.prepareSearch(indexName)).thenReturn(searchRequestBuilder);
	//
	//		Repository<Entity> repo = when(mock(Repository.class).getName()).thenReturn("entity").getMock();
	//		List<Object> idsBatch0 = new ArrayList<>();
	//		for (int i = 0; i < batchSize; ++i)
	//		{
	//			idsBatch0.add(i + 1);
	//		}
	//		List<Object> idsBatch1 = new ArrayList<>();
	//		for (int i = batchSize; i < totalSize; ++i)
	//		{
	//			idsBatch1.add(i + 1);
	//		}
	//		List<Entity> entitiesBatch0 = new ArrayList<>();
	//		for (int i = 0; i < batchSize; ++i)
	//		{
	//			entitiesBatch0.add(when(mock(Entity.class).getIdValue()).thenReturn(i + 1).getMock());
	//		}
	//		List<Entity> entitiesBatch1 = new ArrayList<>();
	//		for (int i = batchSize; i < totalSize; ++i)
	//		{
	//			entitiesBatch1.add(when(mock(Entity.class).getIdValue()).thenReturn(i + 1).getMock());
	//		}
	//		when(repo.findAll(idsBatch0.stream())).thenAnswer(new Answer<Stream<Entity>>()
	//		{
	//			@Override
	//			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
	//			{
	//				return entitiesBatch0.stream();
	//			}
	//		});
	//		when(repo.findAll(idsBatch1.stream())).thenAnswer(new Answer<Stream<Entity>>()
	//		{
	//			@Override
	//			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
	//			{
	//				return entitiesBatch1.stream();
	//			}
	//		});
	//		dataService.addRepository(repo);
	//		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
	//		entityMetaData.setBackend(ElasticsearchRepositoryCollection.NAME);
	//		entityMetaData.addAttribute(idAttrName, ROLE_ID).setDataType(MolgenisFieldTypes.INT);
	//		Query<Entity> q = new QueryImpl<>();
	//		Iterable<Entity> searchResults = searchService.search(q, entityMetaData);
	//		Iterator<Entity> it = searchResults.iterator();
	//		for (int i = 1; i <= totalSize; ++i)
	//		{
	//			assertEquals(it.next().getIdValue(), i);
	//		}
	//		assertFalse(it.hasNext());
	//	}
	//
	//	private SearchHits createSearchHits(final SearchHit[] searchHits, final int totalHits)
	//	{
	//		return new SearchHits()
	//		{
	//			@Override
	//			public Iterator<SearchHit> iterator()
	//			{
	//				return Arrays.asList(searchHits).iterator();
	//			}
	//
	//			@Override
	//			public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException
	//			{
	//				throw new UnsupportedOperationException();
	//			}
	//
	//			@Override
	//			public void writeTo(StreamOutput out) throws IOException
	//			{
	//				throw new UnsupportedOperationException();
	//			}
	//
	//			@Override
	//			public void readFrom(StreamInput in) throws IOException
	//			{
	//				throw new UnsupportedOperationException();
	//			}
	//
	//			@Override
	//			public long totalHits()
	//			{
	//				return getTotalHits();
	//			}
	//
	//			@Override
	//			public float maxScore()
	//			{
	//				return getMaxScore();
	//			}
	//
	//			@Override
	//			public SearchHit[] hits()
	//			{
	//				return getHits();
	//			}
	//
	//			@Override
	//			public long getTotalHits()
	//			{
	//				return totalHits;
	//			}
	//
	//			@Override
	//			public float getMaxScore()
	//			{
	//				throw new UnsupportedOperationException();
	//			}
	//
	//			@Override
	//			public SearchHit[] getHits()
	//			{
	//				return searchHits;
	//			}
	//
	//			@Override
	//			public SearchHit getAt(int position)
	//			{
	//				throw new UnsupportedOperationException();
	//			}
	//		};
	//	}
}
