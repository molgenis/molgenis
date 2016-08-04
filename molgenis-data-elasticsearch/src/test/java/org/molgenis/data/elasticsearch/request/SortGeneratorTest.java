package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

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
		EntityMetaData entityMetaData = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData intAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("int").getMock();
		when(intAttr.getDataType()).thenReturn(INT);
		when(entityMetaData.getAttribute("int")).thenReturn(intAttr);
		AttributeMetaData stringAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("string").getMock();
		when(stringAttr.getDataType()).thenReturn(STRING);
		when(entityMetaData.getAttribute("string")).thenReturn(stringAttr);
		this.entityMetaData = entityMetaData;
	}

	@Test
	public void testGenerateNoSort()
	{
		QueryImpl<Entity> query = new QueryImpl<>();
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		verifyZeroInteractions(searchRequestBuilder);
	}

	@Test
	public void testGenerateAsc()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("int", Sort.Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateAscRaw()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Sort.Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""),
				"\"string.raw\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDesc()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("int", Sort.Direction.DESC));
		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDescRaw()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Sort.Direction.DESC));
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
		Query<Entity> query = new QueryImpl<>()
				.sort(new Sort().on("int", Sort.Direction.DESC).on("string", Sort.Direction.ASC));
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
