package org.molgenis.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticSearchServiceTest
{
	private Client client;
	private ElasticSearchService searchService;
	private Repository repoMock;

	@BeforeMethod
	public void beforeMethod()
	{
		searchService = new ElasticSearchService(client, "molgenis");
		repoMock = mock(Repository.class);
	}

	@BeforeClass
	public void beforeClass()
	{
		Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath("elasticsearchtest.yml").build();
		client = nodeBuilder().settings(settings).local(true).node().client();

		// We wait now for the yellow (or green) status
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	@AfterClass
	public void afterClass()
	{
		client.close();
	}

	// @Test
	public void testDocumentTypeExists()
	{
		assertFalse(searchService.documentTypeExists("xxx"));

		when(repoMock.iterator()).thenReturn(Arrays.<Entity> asList(new MapEntity()).iterator());
		when(repoMock.getName()).thenReturn("beer");
		when(repoMock.getAttributes()).thenReturn(Collections.<AttributeMetaData> emptyList());

		searchService.indexRepository(repoMock);
		waitForIndexUpdate();

		assertTrue(searchService.documentTypeExists("beer"));
	}

	@Test
	public void testCount() throws Exception
	{
		final List<Entity> entities = new ArrayList<Entity>();
		Entity e1 = new MapEntity("id");
		e1.set("id", 1);
		e1.set("name", "Piet");
		entities.add(e1);

		Entity e2 = new MapEntity("id");
		e2.set("id", 2);
		e2.set("name", "Piet");
		entities.add(e2);

		Entity e3 = new MapEntity("id");
		e3.set("id", 3);
		e3.set("name", "Klaas");
		entities.add(e3);

		when(repoMock.iterator()).thenAnswer(new Answer<Iterator<Entity>>()
		{
			@Override
			public Iterator<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return entities.iterator();
			}
		});
		when(repoMock.getName()).thenReturn("person");
		when(repoMock.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData("id", FieldTypeEnum.INT),
						new DefaultAttributeMetaData("name", FieldTypeEnum.STRING)));

		searchService.indexRepository(repoMock);
		waitForIndexUpdate();

		long count = searchService.count("person", new QueryImpl().eq("name", "Piet"));
		assertEquals(count, 2);
	}

	@Test
	public void testSearch() throws Exception
	{
		final List<Entity> fruits = new ArrayList<Entity>();

		Entity apple = new MapEntity("id");
		apple.set("id", 1);
		apple.set("name", "apple");
		apple.set("color", "green");
		fruits.add(apple);

		Entity banana = new MapEntity("id");
		banana.set("id", 2);
		banana.set("name", "banana");
		banana.set("color", "yellow");
		fruits.add(banana);

		Entity orange = new MapEntity("id");
		orange.set("id", 3);
		orange.set("name", "orange");
		orange.set("color", "orange");
		fruits.add(orange);

		Entity clemantine = new MapEntity("id");
		clemantine.set("id", 4);
		clemantine.set("name", "clemantine");
		clemantine.set("color", "orange");
		fruits.add(clemantine);

		Entity appleWithDot = new MapEntity("id");
		appleWithDot.set("id", 5);
		appleWithDot.set("name", "brown.banana");
		appleWithDot.set("color", "brown");
		fruits.add(appleWithDot);

		when(repoMock.iterator()).thenAnswer(new Answer<Iterator<Entity>>()
		{
			@Override
			public Iterator<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return fruits.iterator();
			}
		});
		when(repoMock.getName()).thenReturn("fruit");
		when(repoMock.getAttributes()).thenReturn(
				Arrays.<AttributeMetaData> asList(new DefaultAttributeMetaData("id", FieldTypeEnum.INT),
						new DefaultAttributeMetaData("name", FieldTypeEnum.STRING), new DefaultAttributeMetaData(
								"color", FieldTypeEnum.STRING)));

		searchService.indexRepository(repoMock);
		waitForIndexUpdate();

		// Search1
		SearchRequest request = new SearchRequest("fruit", new QueryImpl().search("apple"), Arrays.asList("name",
				"color"));

		SearchResult searchResult = searchService.search(request);
		assertNotNull(searchResult);
		assertEquals(searchResult.getTotalHitCount(), 1);
		assertNull(searchResult.getErrorMessage());

		List<Hit> hits = searchResult.getSearchHits();
		assertNotNull(hits);
		assertEquals(hits.size(), 1);
		assertEquals(hits.get(0).getDocumentType(), "fruit");

		Map<String, Object> objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("name", "apple");
		objectValueMapExpected.put("color", "green");
		assertEquals(hits.get(0).getColumnValueMap(), objectValueMapExpected);

		// Search2
		request = new SearchRequest("fruit", new QueryImpl().eq("color", "orange"), Arrays.asList("id"));

		searchResult = searchService.search(request);
		assertNotNull(searchResult);
		assertEquals(searchResult.getTotalHitCount(), 2);
		assertNull(searchResult.getErrorMessage());

		hits = searchResult.getSearchHits();
		assertNotNull(hits);
		assertEquals(hits.size(), 2);
		assertEquals(hits.get(0).getDocumentType(), "fruit");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 3);
		assertEquals(hits.get(0).getColumnValueMap(), objectValueMapExpected);
		assertEquals(hits.get(1).getDocumentType(), "fruit");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 4);
		assertEquals(hits.get(1).getColumnValueMap(), objectValueMapExpected);

		// Search3
		request = new SearchRequest("fruit", new QueryImpl().search("banana"), Arrays.asList("id"));

		searchResult = searchService.search(request);
		assertNotNull(searchResult);
		assertEquals(searchResult.getTotalHitCount(), 2);
		assertNull(searchResult.getErrorMessage());

		hits = searchResult.getSearchHits();
		assertNotNull(hits);
		assertEquals(hits.size(), 2);
		assertEquals(hits.get(0).getDocumentType(), "fruit");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 2);
		assertEquals(hits.get(0).getColumnValueMap(), objectValueMapExpected);

		assertEquals(hits.get(1).getDocumentType(), "fruit");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 5);
		assertEquals(hits.get(1).getColumnValueMap(), objectValueMapExpected);
	}

	private void waitForIndexUpdate()
	{
		client.admin().indices().prepareRefresh().execute().actionGet();
	}

}
