package org.molgenis.data.elasticsearch;

import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.client.ClientFacade;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.client.model.SearchHits;
import org.molgenis.data.elasticsearch.generator.ContentGenerators;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.elasticsearch.ElasticsearchService.MAX_BATCH_SIZE;

public class ElasticsearchServiceTest extends AbstractMockitoTest
{
	private ElasticsearchService elasticsearchService;

	@Mock
	private ClientFacade clientFacade;

	@Mock
	private ContentGenerators contentGenerators;

	@Mock
	private DataService dataService;

	@Mock
	private EntityType entityType;

	public ElasticsearchServiceTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		elasticsearchService = new ElasticsearchService(clientFacade, contentGenerators, dataService);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBatchingSearchPageSizeZero()
	{
		QueryImpl<Entity> query = mock(QueryImpl.class);
		when(query.getPageSize()).thenReturn(0);
		when(query.getOffset()).thenReturn(0);

		SearchHits searchHitsBatch = mock(SearchHits.class);
		when(searchHitsBatch.getHits()).thenReturn(asList(new SearchHit[10000]));

		SearchHits finalSearchHitsBatch = mock(SearchHits.class);
		when(finalSearchHitsBatch.getHits()).thenReturn(asList(new SearchHit[5000]));

		when(clientFacade.search(any(), eq(0), eq(10000), any(), any())).thenReturn(searchHitsBatch);
		when(clientFacade.search(any(), eq(10000), anyInt(), any(), any())).thenReturn(searchHitsBatch);
		when(clientFacade.search(any(), eq(20000), anyInt(), any(), any())).thenReturn(finalSearchHitsBatch);

		elasticsearchService.search(entityType, query);

		verify(clientFacade, times(1)).search(any(), eq(0), eq(MAX_BATCH_SIZE), any(), any());
		verify(clientFacade, times(1)).search(any(), eq(10000), eq(MAX_BATCH_SIZE), any(), any());
		verify(clientFacade, times(1)).search(any(), eq(20000), eq(MAX_BATCH_SIZE), any(), any());
		verifyNoMoreInteractions(clientFacade);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSingleBatchSearch()
	{
		QueryImpl<Entity> query = mock(QueryImpl.class);
		when(query.getPageSize()).thenReturn(50);
		when(query.getOffset()).thenReturn(20);

		SearchHits searchHitsBatch = mock(SearchHits.class);
		when(searchHitsBatch.getHits()).thenReturn(asList(new SearchHit[50]));

		when(clientFacade.search(any(), eq(20), eq(50), any(), any())).thenReturn(searchHitsBatch);

		elasticsearchService.search(entityType, query);

		verify(clientFacade, times(1)).search(any(), eq(20), eq(50), any(), any());
		verifyNoMoreInteractions(clientFacade);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBatchingSearchPageSizeLargerThanMax()
	{
		QueryImpl<Entity> query = mock(QueryImpl.class);
		when(query.getPageSize()).thenReturn(10001);
		when(query.getOffset()).thenReturn(5000);

		SearchHits searchHitsBatch = mock(SearchHits.class);
		when(searchHitsBatch.getHits()).thenReturn(asList(new SearchHit[10000]));

		SearchHits finalSearchHitsBatch = mock(SearchHits.class);
		when(finalSearchHitsBatch.getHits()).thenReturn(asList(new SearchHit[1]));

		when(clientFacade.search(any(), eq(5000), eq(MAX_BATCH_SIZE), any(), any())).thenReturn(searchHitsBatch);
		when(clientFacade.search(any(), eq(15000), eq(1), any(), any())).thenReturn(finalSearchHitsBatch);

		elasticsearchService.search(entityType, query);

		verify(clientFacade, times(1)).search(any(), eq(5000), eq(MAX_BATCH_SIZE), any(), any());
		verify(clientFacade, times(1)).search(any(), eq(15000), eq(1), any(), any());
		verifyNoMoreInteractions(clientFacade);
	}
}