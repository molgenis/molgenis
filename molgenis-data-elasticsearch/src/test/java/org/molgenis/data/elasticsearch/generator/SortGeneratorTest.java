package org.molgenis.data.elasticsearch.generator;

import org.elasticsearch.search.sort.SortBuilder;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class SortGeneratorTest
{
	private SortGenerator sortGenerator;
	private EntityType entityType;

	@BeforeMethod
	public void beforeMethod()
	{
		DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
		when(documentIdGenerator.generateId(any(Attribute.class)))
				.thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());

		sortGenerator = new SortGenerator(documentIdGenerator);
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
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
		assertEquals(emptyList(), sortGenerator.generate(null, entityType));
	}

	@Test
	public void testGenerateAsc()
	{
		Sort sort = new Sort("int", Sort.Direction.ASC);
		List<SortBuilder> sorts = sortGenerator.generate(sort, entityType);
		assertEquals(sorts.size(), 1);
		assertEquals(sorts.iterator().next().toString().replaceAll("\\s", ""),
				"{\"int\":{\"order\":\"asc\",\"mode\":\"min\"}}");
	}

	@Test
	public void testGenerateAscRaw()
	{
		Sort sort = new Sort("string", Sort.Direction.ASC);
		List<SortBuilder> sorts = sortGenerator.generate(sort, entityType);
		assertEquals(sorts.size(), 1);
		assertEquals(sorts.iterator().next().toString().replaceAll("\\s", ""),
				"{\"string.raw\":{\"order\":\"asc\",\"mode\":\"min\"}}");
	}

	@Test
	public void testGenerateDesc()
	{
		Sort sort = new Sort("int", Sort.Direction.DESC);
		List<SortBuilder> sorts = sortGenerator.generate(sort, entityType);
		assertEquals(sorts.size(), 1);
		assertEquals(sorts.iterator().next().toString().replaceAll("\\s", ""),
				"{\"int\":{\"order\":\"desc\",\"mode\":\"min\"}}");
	}

	@Test
	public void testGenerateDescRaw()
	{
		Sort sort = new Sort("string", Sort.Direction.DESC);
		List<SortBuilder> sorts = sortGenerator.generate(sort, entityType);
		assertEquals(sorts.size(), 1);
		assertEquals(sorts.iterator().next().toString().replaceAll("\\s", ""),
				"{\"string.raw\":{\"order\":\"desc\",\"mode\":\"min\"}}");
	}

	@Test
	public void testGenerateDescAscRaw()
	{
		Sort sort = new Sort().on("int", Sort.Direction.DESC).on("string", Sort.Direction.ASC);
		List<SortBuilder> sorts = sortGenerator.generate(sort, entityType);
		assertEquals(sorts.size(), 2);
		assertEquals(sorts.get(0).toString().replaceAll("\\s", ""), "{\"int\":{\"order\":\"desc\",\"mode\":\"min\"}}");
		assertEquals(sorts.get(1).toString().replaceAll("\\s", ""),
				"{\"string.raw\":{\"order\":\"asc\",\"mode\":\"min\"}}");
	}
}
