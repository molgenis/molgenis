package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class SortGeneratorTest
{
	private SortGenerator sortGenerator;
	private SearchRequestBuilder searchRequestBuilder;
	private EntityType entityType;

	@BeforeMethod
	public void beforeMethod()
	{
		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
		when(documentIdGenerator.generateId(any(Attribute.class)))
				.thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());

		sortGenerator = new SortGenerator(documentIdGenerator);
		searchRequestBuilder = mock(SearchRequestBuilder.class);
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		Attribute intAttr = when(mock(Attribute.class).getName()).thenReturn("int").getMock();
		when(intAttr.getDataType()).thenReturn(INT);
		when(entityType.getAttribute("int")).thenReturn(intAttr);
		Attribute stringAttr = when(mock(Attribute.class).getName()).thenReturn("string").getMock();
		when(stringAttr.getDataType()).thenReturn(STRING);
		when(entityType.getAttribute("string")).thenReturn(stringAttr);
		this.entityType = entityType;
	}

	@Test
	public void testGenerateNoSort()
	{
		QueryImpl<Entity> query = new QueryImpl<>();
		sortGenerator.generate(searchRequestBuilder, query, entityType);
		verifyZeroInteractions(searchRequestBuilder);
	}

	@Test
	public void testGenerateAsc()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("int", Sort.Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityType);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"asc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateAscRaw()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Sort.Direction.ASC));
		sortGenerator.generate(searchRequestBuilder, query, entityType);
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
		sortGenerator.generate(searchRequestBuilder, query, entityType);
		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
		verify(searchRequestBuilder).addSort(argument.capture());
		FieldSortBuilder sortBuilder = argument.getValue();
		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
	}

	@Test
	public void testGenerateDescRaw()
	{
		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Sort.Direction.DESC));
		sortGenerator.generate(searchRequestBuilder, query, entityType);
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
		sortGenerator.generate(searchRequestBuilder, query, entityType);
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
