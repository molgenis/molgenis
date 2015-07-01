package org.molgenis.data.elasticsearch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticSearchService.BulkProcessorFactory;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class ElasticSearchServiceTest
{
	private Client client;
	private ElasticSearchService searchService;
	private String indexName;
	private EntityToSourceConverter entityToSourceConverter;
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod() throws InterruptedException
	{
		indexName = "molgenis";
		client = mock(Client.class);

		entityToSourceConverter = mock(EntityToSourceConverter.class);
		dataService = spy(new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory()));
		searchService = spy(new ElasticSearchService(client, indexName, dataService, entityToSourceConverter, false));
		BulkProcessorFactory bulkProcessorFactory = mock(BulkProcessorFactory.class);
		BulkProcessor bulkProcessor = mock(BulkProcessor.class);
		when(bulkProcessor.awaitClose(any(Long.class), any(TimeUnit.class))).thenReturn(true);
		when(bulkProcessorFactory.create(client)).thenReturn(bulkProcessor);
		ElasticSearchService.setBulkProcessorFactory(bulkProcessorFactory);
		doNothing().when(searchService).refresh(any(String.class));
	}

	@BeforeClass
	public void beforeClass()
	{
	}

	@AfterClass
	public void afterClass()
	{
	}

	@Test
	public void indexEntityAdd()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id0");

		searchService.index(entity, entityMetaData, IndexingMode.ADD);
		verify(searchService, times(1)).index(indexName, Arrays.asList(entity), entityMetaData,
				ElasticSearchService.CrudType.ADD, true);
	}

	@Test
	public void indexEntityUpdateNoRefs()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id0");
		when(dataService.getEntityNames()).thenReturn(Lists.newArrayList());

		searchService.index(entity, entityMetaData, IndexingMode.UPDATE);
		verify(searchService, times(1)).index(indexName, Arrays.asList(entity), entityMetaData,
				ElasticSearchService.CrudType.UPDATE, true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void search()
	{
		int batchSize = 1000;
		int totalSize = batchSize + 1;
		SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
		String idAttrName = "id";

		ListenableActionFuture<SearchResponse> value1 = mock(ListenableActionFuture.class);
		SearchResponse searchResponse1 = mock(SearchResponse.class);
		SearchHits searchHits1 = mock(SearchHits.class);
		SearchHit[] hits1 = new SearchHit[batchSize];
		for (int i = 0; i < batchSize; ++i)
		{
			SearchHit searchHit = mock(SearchHit.class);
			when(searchHit.getSource()).thenReturn(Collections.<String, Object> singletonMap(idAttrName, i + 1));
			hits1[i] = searchHit;
		}
		when(searchHits1.getHits()).thenReturn(hits1);
		when(searchHits1.getTotalHits()).thenReturn(Long.valueOf(totalSize));
		when(searchResponse1.getHits()).thenReturn(searchHits1);
		when(value1.actionGet()).thenReturn(searchResponse1);

		ListenableActionFuture<SearchResponse> value2 = mock(ListenableActionFuture.class);
		SearchResponse searchResponse2 = mock(SearchResponse.class);
		SearchHits searchHits2 = mock(SearchHits.class);

		SearchHit[] hits2 = new SearchHit[totalSize - batchSize];
		SearchHit searchHit = mock(SearchHit.class);
		when(searchHit.getSource()).thenReturn(Collections.<String, Object> singletonMap(idAttrName, batchSize + 1));
		hits2[0] = searchHit;
		when(searchHits2.getHits()).thenReturn(hits2);
		when(searchHits2.getTotalHits()).thenReturn(Long.valueOf(totalSize));
		when(searchResponse2.getHits()).thenReturn(searchHits2);
		when(value2.actionGet()).thenReturn(searchResponse2);

		when(searchRequestBuilder.execute()).thenReturn(value1, value2);
		when(client.prepareSearch(indexName)).thenReturn(searchRequestBuilder);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(idAttrName).setDataType(MolgenisFieldTypes.INT).setIdAttribute(true);
		Query q = new QueryImpl();
		Iterable<Entity> searchResults = searchService.search(q, entityMetaData);
		Iterator<Entity> it = searchResults.iterator();
		for (int i = 1; i <= totalSize; ++i)
		{
			assertEquals(it.next().getIdValue(), i);
		}
		assertFalse(it.hasNext());
	}

	private MapEntity createEntityAndRegisterSource(final EntityMetaData metaData, final String id)
	{
		final String idAttributeName = metaData.getIdAttribute().getName();
		final String entityName = metaData.getName();
		MapEntity entity = new MapEntity(idAttributeName);
		entity.set(idAttributeName, id);
		Map<String, Object> source = getSource(metaData, entity);
		when(entityToSourceConverter.convert(entity, metaData)).thenReturn(source);
		whenIndexEntity(client, id, entityName, source);
		return entity;
	}

	private Map<String, Object> getSource(EntityMetaData metaData, Entity entity)
	{
		final String idAttributeName = metaData.getIdAttribute().getName();
		final String entityName = metaData.getName();
		Map<String, Object> source = new HashMap<String, Object>();
		source.put(idAttributeName, entity.get(idAttributeName));
		source.put("type", entityName);
		return source;
	}

	private DefaultEntityMetaData createEntityMeta(String refEntityName)
	{
		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData(refEntityName);
		refEntityMetaData.addAttribute("id").setIdAttribute(true).setUnique(true);
		return refEntityMetaData;
	}

	private void whenIndexEntity(Client client, String id, String entityName, Map<String, Object> source)
	{
		IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
		when(indexRequestBuilder.setSource(eq(source))).thenReturn(indexRequestBuilder);
		@SuppressWarnings("unchecked")
		ListenableActionFuture<IndexResponse> indexResponse = mock(ListenableActionFuture.class);
		when(indexRequestBuilder.execute()).thenReturn(indexResponse);
		when(client.prepareIndex(indexName, entityName, id)).thenReturn(indexRequestBuilder);
	}
}
