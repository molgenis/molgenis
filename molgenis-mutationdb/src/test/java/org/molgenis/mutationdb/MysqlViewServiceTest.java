package org.molgenis.mutationdb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.mutationdb.MysqlViewServiceTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class MysqlViewServiceTest extends AbstractTestNGSpringContextTests
{	
	@Autowired
	public MysqlViewService mysqlViewService;

	@Autowired
	public DataService dataService;

	@Autowired
	public DataSource dataSource;

	final List<String> headers = Arrays.asList("header1", "header2", "header3");

	@Test
	public void valuesPerHeader()
	{
		final Entity entity1 = mock(Entity.class);
		when(entity1.get("header1")).thenReturn("M1");
		when(entity1.get("header2")).thenReturn("P1");
		when(entity1.get("header3")).thenReturn("J1");
		final Entity entity2 = mock(Entity.class);
		when(entity2.get("header1")).thenReturn("M2");
		when(entity2.get("header2")).thenReturn("P2");
		when(entity2.get("header3")).thenReturn("J2");
		final Entity entity3 = mock(Entity.class);
		when(entity3.get("header1")).thenReturn("M3");
		when(entity3.get("header2")).thenReturn("P3");
		when(entity3.get("header3")).thenReturn("J3");
		final Iterable<Entity> entities = Arrays.asList(entity1, entity2, entity3);
		
		Map<String, List<Value>> valuesPerHeader = mysqlViewService.valuesPerHeader(headers, entities);

		// Results
		Map<String, List<Value>> expected = new HashMap<String, List<Value>>();
		expected.put("header1", Arrays.asList(new Value("M1"), new Value("M2"), new Value("M3")));
		expected.put("header2", Arrays.asList(new Value("P1"), new Value("P2"), new Value("P3")));
		expected.put("header3", Arrays.asList(new Value("J1"), new Value("J2"), new Value("J3")));

		assertEquals(valuesPerHeader, expected);
	}

	@Test
	public void createRowByMergingValuesIfEquales()
	{
		final Entity entity1 = mock(Entity.class);
		when(entity1.get("header1")).thenReturn("M1");
		when(entity1.get("header2")).thenReturn("P1");
		when(entity1.get("header3")).thenReturn("J2");
		final Entity entity2 = mock(Entity.class);
		when(entity2.get("header1")).thenReturn("M1");
		when(entity2.get("header2")).thenReturn("P2");
		when(entity2.get("header3")).thenReturn("J2");
		final Entity entity3 = mock(Entity.class);
		when(entity3.get("header1")).thenReturn("M1");
		when(entity3.get("header2")).thenReturn("P3");
		when(entity3.get("header3")).thenReturn("J3");
		final Iterable<Entity> entities = Arrays.asList(entity1, entity2, entity3);

		Map<String, List<Value>> valuesPerHeader = mysqlViewService.valuesPerHeader(headers, entities);
		Row row = mysqlViewService.createRowByMergingValuesIfEquales(headers, valuesPerHeader);

		Row expected = new Row();
		expected.add((new Cell()).addAll(Arrays.asList(new Value("M1"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("P1"), new Value("P2"), new Value("P3"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("J2"), new Value("J2"), new Value("J3"))));
		
		assertEquals(row, expected);
	}

	@Test
	public void createRowByMergingValuesIfEqualesWrong()
	{
		final Entity entity1 = mock(Entity.class);
		when(entity1.get("header1")).thenReturn("M1");
		when(entity1.get("header2")).thenReturn("P2");
		when(entity1.get("header3")).thenReturn("J2");
		final Entity entity2 = mock(Entity.class);
		when(entity2.get("header1")).thenReturn("M2");
		when(entity2.get("header2")).thenReturn("P2");
		when(entity2.get("header3")).thenReturn("J2");
		final Entity entity3 = mock(Entity.class);
		when(entity3.get("header1")).thenReturn("M3");
		when(entity3.get("header2")).thenReturn("P3");
		when(entity3.get("header3")).thenReturn("J3");
		final Iterable<Entity> entities = Arrays.asList(entity1, entity2, entity3);

		Map<String, List<Value>> valuesPerHeader = mysqlViewService.valuesPerHeader(headers, entities);
		Row row = mysqlViewService.createRowByMergingValuesIfEquales(headers, valuesPerHeader);

		Row expected = new Row();
		expected.add((new Cell()).addAll(Arrays.asList(new Value("M1"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("P1"), new Value("P2"), new Value("P3"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("J2"), new Value("J2"), new Value("J3"))));

		assertNotEquals(row, expected);
	}

	@Test
	public void createRowFromEntities()
	{
		final Entity entity1 = mock(Entity.class);
		when(entity1.get("header1")).thenReturn("M1");
		when(entity1.get("header2")).thenReturn("P2");
		when(entity1.get("header3")).thenReturn("J2");
		final Entity entity2 = mock(Entity.class);
		when(entity2.get("header1")).thenReturn("M2");
		when(entity2.get("header2")).thenReturn("P2");
		when(entity2.get("header3")).thenReturn("J2");
		final Entity entity3 = mock(Entity.class);
		when(entity3.get("header1")).thenReturn("M3");
		when(entity3.get("header2")).thenReturn("P3");
		when(entity3.get("header3")).thenReturn("J3");
		final Iterable<Entity> entities = Arrays.asList(entity1, entity2, entity3);

		Map<String, List<Value>> valuesPerHeader = mysqlViewService.valuesPerHeader(headers, entities);
		Row row = mysqlViewService.createRow(headers, valuesPerHeader);

		Row expected = new Row();
		expected.add((new Cell()).addAll(Arrays.asList(new Value("M1"), new Value("M2"), new Value("M3"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("P2"), new Value("P2"), new Value("P3"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("J2"), new Value("J2"), new Value("J3"))));

		assertEquals(row, expected);
	}

	@Test
	public void createRowFromEntity()
	{
		final Entity entity = mock(Entity.class);
		when(entity.get("header1")).thenReturn("M1");
		when(entity.get("header2")).thenReturn("P1");
		when(entity.get("header3")).thenReturn("J1");

		Row row = mysqlViewService.createRow(headers, entity);
		Row expected = new Row();
		expected.add((new Cell()).addAll(Arrays.asList(new Value("M1"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("P1"))));
		expected.add((new Cell()).addAll(Arrays.asList(new Value("J1"))));

		assertEquals(row, expected);
	}

	@Test
	public void areAllValuesEquals()
	{
		List<Value> values1 = new ArrayList<Value>();
		values1.add(new Value("M1"));
		values1.add(new Value("m1"));
		values1.add(new Value("1m"));
		
		assertFalse(mysqlViewService.areAllValuesEquals(values1));
		
		List<Value> values2 = new ArrayList<Value>();
		values2.add(new Value("M1"));
		values2.add(new Value("M1"));
		values2.add(new Value("M1"));
		
		assertTrue(mysqlViewService.areAllValuesEquals(values2));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public DataSource dataSource()
		{
			return mock(DataSource.class);
		}

		@Bean
		public MysqlViewService mysqlViewService()
		{
			return new MysqlViewService(this.dataSource());
		}
	}
}
