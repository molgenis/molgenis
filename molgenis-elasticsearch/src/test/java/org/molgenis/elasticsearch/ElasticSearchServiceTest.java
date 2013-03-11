package org.molgenis.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticSearchServiceTest
{
	private Node node;
	private ElasticSearchService searchService;

	@BeforeMethod
	public void beforeMethod()
	{
		searchService = new ElasticSearchService(node, "test");
	}

	@BeforeClass
	public void beforeClass()
	{
		Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath("elasticsearchtest.yml").build();
		node = nodeBuilder().settings(settings).local(true).node();

		// We wait now for the yellow (or green) status
		node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	@AfterClass
	public void afterClass()
	{
		node.close();
	}

	@Test
	public void testCount() throws Exception
	{
		List<Entity> entities = new ArrayList<Entity>();
		Entity e1 = new TestEntity(1);
		e1.set("name", "Piet");
		entities.add(e1);

		Entity e2 = new TestEntity(2);
		e2.set("name", "Piet");
		entities.add(e2);

		Entity e3 = new TestEntity(3);
		e3.set("name", "Klaas");
		entities.add(e3);

		searchService.updateIndex("person", entities);
		waitForIndexUpdate();

		long count = searchService.count("person", Arrays.asList(new QueryRule("name", Operator.EQUALS, "Piet")));
		assertEquals(count, 2);
	}

	@Test
	public void testSearch() throws Exception
	{
		List<Entity> fruits = new ArrayList<Entity>();

		Entity apple = new TestEntity(1);
		apple.set("name", "apple");
		apple.set("color", "green");
		fruits.add(apple);

		Entity banana = new TestEntity(2);
		banana.set("name", "banana");
		banana.set("color", "yellow");
		fruits.add(banana);

		Entity orange = new TestEntity(3);
		orange.set("name", "orange");
		orange.set("color", "orange");
		fruits.add(orange);

		Entity clemantine = new TestEntity(4);
		clemantine.set("name", "clemantine");
		clemantine.set("color", "orange");
		fruits.add(clemantine);

		searchService.updateIndex("fruit", fruits);
		waitForIndexUpdate();

		// Search1
		SearchRequest request = new SearchRequest("fruit", Arrays.asList(new QueryRule(Operator.SEARCH, "apple")),
				Arrays.asList("name", "color"));

		SearchResult searchResult = searchService.search(request);
		assertNotNull(searchResult);
		assertEquals(searchResult.getTotalHitCount(), 1);
		assertNull(searchResult.getErrorMessage());

		List<Hit> hits = searchResult.getSearchHits();
		assertNotNull(hits);
		assertEquals(hits.size(), 1);
		assertEquals(hits.get(0).getId(), "1");
		assertEquals(hits.get(0).getType(), "fruit");
		assertEquals(hits.get(0).getHref(), "/api/v1/fruit/1");

		Map<String, Object> objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("name", "apple");
		objectValueMapExpected.put("color", "green");
		assertEquals(hits.get(0).getColumnValueMap(), objectValueMapExpected);

		// Search2
		request = new SearchRequest("fruit", Arrays.asList(new QueryRule("color", Operator.EQUALS, "orange")),
				Arrays.asList("id"));

		searchResult = searchService.search(request);
		assertNotNull(searchResult);
		assertEquals(searchResult.getTotalHitCount(), 2);
		assertNull(searchResult.getErrorMessage());

		hits = searchResult.getSearchHits();
		assertNotNull(hits);
		assertEquals(hits.size(), 2);
		assertEquals(hits.get(0).getId(), "3");
		assertEquals(hits.get(0).getType(), "fruit");
		assertEquals(hits.get(0).getHref(), "/api/v1/fruit/3");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 3);
		assertEquals(hits.get(0).getColumnValueMap(), objectValueMapExpected);
		assertEquals(hits.get(1).getId(), "4");
		assertEquals(hits.get(1).getHref(), "/api/v1/fruit/4");
		assertEquals(hits.get(1).getType(), "fruit");
		objectValueMapExpected = new LinkedHashMap<String, Object>();
		objectValueMapExpected.put("id", 4);
		assertEquals(hits.get(1).getColumnValueMap(), objectValueMapExpected);
	}

	private void waitForIndexUpdate()
	{
		node.client().admin().indices().prepareRefresh().execute().actionGet();
	}

	private class TestEntity implements Entity
	{
		private final Map<String, Object> fields = new LinkedHashMap<String, Object>();

		public TestEntity(int id)
		{
			fields.put("id", id);
		}

		@Override
		public void set(String fieldName, Object value) throws Exception
		{
			fields.put(fieldName, value);
		}

		@Override
		public void set(Tuple values) throws Exception
		{
		}

		@Override
		public void set(Tuple values, boolean strict) throws Exception
		{
		}

		@Override
		public Object get(String columnName)
		{
			return fields.get(columnName);
		}

		@Override
		public String getIdField()
		{
			return "id";
		}

		@Override
		public Object getIdValue()
		{
			return fields.get(getIdField());
		}

		@Override
		public List<String> getLabelFields()
		{
			return null;
		}

		@Override
		public Tuple getValues()
		{
			return null;
		}

		@Override
		public Vector<String> getFields()
		{
			return getFields(false);
		}

		@Override
		public Vector<String> getFields(boolean skipAutoIds)
		{
			Vector<String> fieldsVector = new Vector<String>(fields.keySet());
			if (skipAutoIds)
			{
				fieldsVector.remove(getIdField());
			}

			return fieldsVector;
		}

		@Override
		public void setReadonly(boolean readonly)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isReadonly()
		{
			return false;
		}

		@Override
		@Deprecated
		public String getValues(String sep)
		{
			return null;
		}

		@Override
		@Deprecated
		public String getFields(String sep)
		{
			return null;
		}

		@Override
		public void validate() throws Exception
		{
		}

		@Override
		public Entity create(Tuple tuple) throws Exception
		{
			return null;
		}

		@Override
		public String getXrefIdFieldName(String fieldName)
		{
			return null;
		}

		@Override
		public String getLabelValue()
		{
			return null;
		}

	}
}
