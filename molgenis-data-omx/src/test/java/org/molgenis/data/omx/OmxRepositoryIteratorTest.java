package org.molgenis.data.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class OmxRepositoryIteratorTest
{
	@Test
	public void testIteratorPageSizeIsNotZero()
	{
		SearchService searchServiceMock = mock(SearchService.class);
		String dataSetIdentifier = "identifier";
		DataService dataService = mock(DataService.class);
		Query q = new QueryImpl().pageSize(1);
		Set<String> attributeNames = new HashSet<String>(Arrays.asList("attr1", "bogus"));

		Map<String, Object> columnValueMap = new HashMap<String, Object>();
		columnValueMap.put("attr1", 2);
		columnValueMap.put("bogus", "bogus");

		Hit hit = new Hit("id", dataSetIdentifier, columnValueMap);
		SearchResult result = new SearchResult(1, Arrays.asList(hit));
		when(searchServiceMock.search(new SearchRequest(dataSetIdentifier, q, null))).thenReturn(result);
		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchServiceMock, dataService, q,
				attributeNames);

		assertEquals(Iterators.size(it), 1);

		it = new OmxRepositoryIterator(dataSetIdentifier, searchServiceMock, dataService, q, attributeNames);

		Entity entity = it.next();
		assertNotNull(entity);
	}

	@Test
	public void testIteratorPageSizeIsZero()
	{
		SearchService searchServiceMock = mock(SearchService.class);
		String dataSetIdentifier = "identifier";
		DataService dataService = mock(DataService.class);
		Query q = new QueryImpl().pageSize(0);
		Query qBatch = new QueryImpl(q).pageSize(OmxRepositoryIterator.BATCH_SIZE);
		Set<String> attributeNames = new HashSet<String>(Arrays.asList("attr1"));
		Map<String, Object> columnValueMap = new HashMap<String, Object>();
		columnValueMap.put("attr1", 1);

		List<Hit> hits = new ArrayList<Hit>();
		for (int i = 0; i < 1050; i++)
		{
			hits.add(new Hit("id", dataSetIdentifier, columnValueMap));
		}

		SearchResult result = new SearchResult(hits.size(), hits);
		when(searchServiceMock.search(new SearchRequest(dataSetIdentifier, qBatch, null))).thenReturn(result);

		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchServiceMock, dataService, q,
				attributeNames);
		assertEquals(Iterators.size(it), hits.size());
	}
}
