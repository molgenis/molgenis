package org.molgenis.data.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
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
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(dataService.getEntityMetaData(dataSetIdentifier)).thenReturn(entityMetaData);
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
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(dataService.getEntityMetaData(dataSetIdentifier)).thenReturn(entityMetaData);
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

	@Test
	public void testIteratorPageSizeLargerThanBatchSize()
	{
		int nrEntities = OmxRepositoryIterator.BATCH_SIZE + 10;
		String dataSetIdentifier = "identifier";
		String attrName = "attr1";
		List<String> attributeNames = Collections.singletonList(attrName);

		List<Hit> hits1 = new ArrayList<Hit>();
		for (int i = 0; i < OmxRepositoryIterator.BATCH_SIZE; i++)
			hits1.add(new Hit("id", dataSetIdentifier, Collections.<String, Object> singletonMap(attrName, "val" + i)));
		List<Hit> hits2 = new ArrayList<Hit>();
		for (int i = 0; i < nrEntities - OmxRepositoryIterator.BATCH_SIZE; i++)
			hits2.add(new Hit("id", dataSetIdentifier, Collections.<String, Object> singletonMap(attrName, "val"
					+ (OmxRepositoryIterator.BATCH_SIZE + i))));

		Query q1 = new QueryImpl().pageSize(OmxRepositoryIterator.BATCH_SIZE).offset(0);
		SearchRequest request1 = new SearchRequest(dataSetIdentifier, q1, null);
		SearchResult result1 = new SearchResult(nrEntities, hits1);

		Query q2 = new QueryImpl().pageSize(OmxRepositoryIterator.BATCH_SIZE).offset(OmxRepositoryIterator.BATCH_SIZE);
		SearchRequest request2 = new SearchRequest(dataSetIdentifier, q2, null);
		SearchResult result2 = new SearchResult(nrEntities, hits2);

		SearchService searchService = mock(SearchService.class);
		when(searchService.search(request1)).thenReturn(result1);
		when(searchService.search(request2)).thenReturn(result2);

		DataService dataService = mock(DataService.class);
		Repository repository = mock(Repository.class);
		AttributeMetaData attributeMetaData = mock(AttributeMetaData.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		FieldType fieldType = mock(FieldType.class);
		when(repository.getEntityMetaData()).thenReturn(entityMetaData);
		when(fieldType.getEnumType()).thenReturn(FieldTypeEnum.STRING);
		when(attributeMetaData.getDataType()).thenReturn(fieldType);
		when(entityMetaData.getAttribute(attrName)).thenReturn(attributeMetaData);
		when(dataService.getRepositoryByEntityName(dataSetIdentifier)).thenReturn(repository);
		when(dataService.getEntityMetaData(dataSetIdentifier)).thenReturn(entityMetaData);

		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchService, dataService,
				new QueryImpl(), new HashSet<String>(attributeNames));
		int count = 0;
		for (; it.hasNext(); ++count)
		{
			assertEquals(it.next().get(attrName), "val" + count);
		}
		assertEquals(count, nrEntities);
	}

	@Test
	public void testIteratorPageSizeSmallerThanBatchSizeNoOffset()
	{
		int nrEntities = OmxRepositoryIterator.BATCH_SIZE - 10;
		String dataSetIdentifier = "identifier";
		String attrName = "attr1";
		List<String> attributeNames = Collections.singletonList(attrName);
		int pageSize = 20;
		int offset = 0;

		List<Hit> hits1 = new ArrayList<Hit>();
		for (int i = 0; i < OmxRepositoryIterator.BATCH_SIZE; i++)
			hits1.add(new Hit("id", dataSetIdentifier, Collections.<String, Object> singletonMap(attrName, "val" + i)));

		Query q1 = new QueryImpl().pageSize(pageSize).offset(offset);
		SearchRequest request1 = new SearchRequest(dataSetIdentifier, q1, null);
		SearchResult result1 = new SearchResult(nrEntities, hits1);

		SearchService searchService = mock(SearchService.class);
		when(searchService.search(request1)).thenReturn(result1);

		DataService dataService = mock(DataService.class);
		Repository repository = mock(Repository.class);
		AttributeMetaData attributeMetaData = mock(AttributeMetaData.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(repository.getEntityMetaData()).thenReturn(entityMetaData);
		FieldType fieldType = mock(FieldType.class);
		when(fieldType.getEnumType()).thenReturn(FieldTypeEnum.STRING);
		when(attributeMetaData.getDataType()).thenReturn(fieldType);
		when(repository.getEntityMetaData().getAttribute(attrName)).thenReturn(attributeMetaData);
		when(dataService.getRepositoryByEntityName(dataSetIdentifier)).thenReturn(repository);
		when(dataService.getEntityMetaData(dataSetIdentifier)).thenReturn(entityMetaData);

		Query q = new QueryImpl().pageSize(pageSize).offset(offset);
		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchService, dataService, q,
				new HashSet<String>(attributeNames));

		int count = 0;
		for (; it.hasNext(); ++count)
		{
			assertEquals(it.next().get(attrName), "val" + count);
		}
		assertEquals(count, pageSize);
	}

	@Test
	public void testIteratorPageSizeSmallerThanBatchSizeWithOffset()
	{
		int nrEntities = OmxRepositoryIterator.BATCH_SIZE - 10;
		String dataSetIdentifier = "identifier";
		String attrName = "attr1";
		List<String> attributeNames = Collections.singletonList(attrName);
		int pageSize = 20;
		int offset = nrEntities - 10;

		List<Hit> hits1 = new ArrayList<Hit>();
		for (int i = 0; i < OmxRepositoryIterator.BATCH_SIZE; i++)
			hits1.add(new Hit("id", dataSetIdentifier, Collections.<String, Object> singletonMap(attrName, "val" + i)));

		Query q1 = new QueryImpl().pageSize(pageSize).offset(offset);
		SearchRequest request1 = new SearchRequest(dataSetIdentifier, q1, null);
		SearchResult result1 = new SearchResult(nrEntities, hits1);

		SearchService searchService = mock(SearchService.class);
		when(searchService.search(request1)).thenReturn(result1);

		DataService dataService = mock(DataService.class);
		Repository repository = mock(Repository.class);
		AttributeMetaData attributeMetaData = mock(AttributeMetaData.class);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(repository.getEntityMetaData()).thenReturn(entityMetaData);
		FieldType fieldType = mock(FieldType.class);
		when(fieldType.getEnumType()).thenReturn(FieldTypeEnum.STRING);
		when(attributeMetaData.getDataType()).thenReturn(fieldType);
		when(repository.getEntityMetaData().getAttribute(attrName)).thenReturn(attributeMetaData);
		when(dataService.getRepositoryByEntityName(dataSetIdentifier)).thenReturn(repository);
		when(dataService.getEntityMetaData(dataSetIdentifier)).thenReturn(entityMetaData);

		Query q = new QueryImpl().pageSize(pageSize).offset(offset);
		OmxRepositoryIterator it = new OmxRepositoryIterator(dataSetIdentifier, searchService, dataService, q,
				new HashSet<String>(attributeNames));

		int count = 0;
		for (; it.hasNext(); ++count)
		{
			assertEquals(it.next().get(attrName), "val" + count);
		}
		assertEquals(count, nrEntities - offset);
	}
}
