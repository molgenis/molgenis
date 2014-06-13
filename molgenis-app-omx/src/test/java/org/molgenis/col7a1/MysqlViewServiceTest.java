package org.molgenis.col7a1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.col7a1.MysqlViewServiceTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class MysqlViewServiceTest extends AbstractTestNGSpringContextTests
{	
	@Autowired
	public MysqlViewService mysqlViewService;

	@Autowired
	public DataService dataService;

	final List<String> headers = Arrays.asList("header1", "header2", "header3");

	@BeforeMethod
	public void beforeMethod()
	{
	}

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

	/**
	 * Add values row.
	 * 
	 * @param idHeader
	 *            can only contain one value.
	 * @param headers
	 *            the names of the headers
	 * @param valuesByHeader
	 * @return
	 */
	public Row createRow(List<String> headers, Map<String, List<Value>> valuesByHeader)
	{
		Row row = new Row();
//		for (String header : headers)
//		{
//			List<Value> values = valuesByHeader.get(header);
//			if (null != values)
//			{
//				Cell cell = new Cell();
//				cell.addAll(values);
//				row.add(cell);
//			}
//		}

		return row;
	}

	/**
	 * Create row from entity
	 * 
	 * @param headers
	 * @param entity
	 * @return
	 */
	public Row createRow(List<String> headers, Entity entity)
	{
		Row row = new Row();

//		for (String header : headers)
//		{
//			final Value value;
//			if (null != entity.get(header))
//			{
//				value = new Value(entity.get(header).toString());
//			}
//			else
//			{
//				value = new Value("");
//			}
//
//			final Cell cell = new Cell();
//			cell.add(value);
//			row.add(cell);
//		}

		return row;
	}

	public boolean areAllValuesEquals(List<Value> values)
	{
		return false;
//		if (values.isEmpty()) return false;
//
//		Value lastValue = null;
//		for (Value value : values)
//		{
//			if (lastValue == null)
//			{
//				lastValue = value;
//			}
//
//			if (!lastValue.equals(value))
//			{
//				return false;
//			}
//		}
//
//		return true;
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MysqlViewService mysqlViewService()
		{
			return new MysqlViewService();
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}
