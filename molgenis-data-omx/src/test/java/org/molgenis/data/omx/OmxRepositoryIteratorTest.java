package org.molgenis.data.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	public void testIterator()
	{
		SearchService searchServiceMock = mock(SearchService.class);
		String dataSetIdentifier = "identifier";
		Query q = new QueryImpl().pageSize(1);
		Set<String> attributeNames = new HashSet<String>(Arrays.asList("attr1"));

		Map<String, Object> columnValueMap = new HashMap<String, Object>();
		columnValueMap.put("attr1", 2);
		columnValueMap.put("bogus", "bogus");

		Hit hit = new Hit("id", dataSetIdentifier, null, columnValueMap);
		SearchResult result = new SearchResult(100, Arrays.asList(hit));
		when(searchServiceMock.search(new SearchRequest(dataSetIdentifier, q, null))).thenReturn(result);

		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchServiceMock, q, attributeNames);
		assertEquals(Iterators.size(it), 1);

		it = new OmxRepositoryIterator(dataSetIdentifier, searchServiceMock, q, attributeNames);
		Entity entity = it.next();
		assertNotNull(entity);
		assertEquals(entity.get("attr1"), 2);
		assertNull(entity.get("bogus"));
	}
}
