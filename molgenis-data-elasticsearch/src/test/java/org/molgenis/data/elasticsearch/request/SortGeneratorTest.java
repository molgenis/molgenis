package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SortGeneratorTest
{
	private SortGenerator sortGenerator;
	private SearchRequestBuilder searchRequestBuilder;
	private EntityMetaData entityMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		sortGenerator = new SortGenerator();
		searchRequestBuilder = mock(SearchRequestBuilder.class);
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute("int").setDataType(MolgenisFieldTypes.INT);
		entityMetaData.addAttribute("string").setDataType(MolgenisFieldTypes.STRING);
		this.entityMetaData = entityMetaData;
	}

	@Test
	public void testGenerateNoSort()
	{
		QueryImpl query = new QueryImpl();
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		verifyZeroInteractions(searchRequestBuilder);
	}

	@Test
	public void testGenerateAsc()
	{
		Query query = new QueryImpl().sort(new Sort("int", Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateAscRaw()
	{
		Query query = new QueryImpl().sort(new Sort("string", Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"string.raw\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDesc()
	{
		Query query = new QueryImpl().sort(new Sort("int", Direction.DESC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDescRaw()
	{
		Query query = new QueryImpl().sort(new Sort("string", Direction.DESC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""),
				"\"string.raw\"{\"order\":\"desc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDescAscRaw()
	{
		Query query = new QueryImpl().sort(new Sort().on("int", Direction.DESC).on("string", Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder, times(2)).addSort(argument.capture());
		List<FieldSortBuilder> sortBuilder = argument.getAllValues();
		assertEquals(sortBuilder.size(), 2);
		assertEquals(sortBuilder.get(0).toString().replaceAll("\\s", ""),
				"\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
		assertEquals(sortBuilder.get(1).toString().replaceAll("\\s", ""),
				"\"string.raw\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}
}
