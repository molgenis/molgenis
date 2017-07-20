package org.molgenis.metadata.manager.mapper;

import org.molgenis.data.Sort;
import org.molgenis.metadata.manager.model.EditorOrder;
import org.molgenis.metadata.manager.model.EditorSort;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.google.common.collect.ImmutableList.of;
import static org.testng.Assert.*;

public class SortMapperTest
{
	private SortMapper sortMapper;

	@BeforeMethod
	public void setUp()
	{
		sortMapper = new SortMapper();
	}

	@Test
	public void testToSort()
	{
		String attributeName = "attr";
		String direction = Sort.Direction.DESC.name();
		EditorSort editorSort = EditorSort.create(of(EditorOrder.create(attributeName, direction)));
		Sort sort = sortMapper.toSort(editorSort);
		Iterator<Sort.Order> iterator = sort.iterator();
		assertTrue(iterator.hasNext());
		Sort.Order order = iterator.next();
		assertEquals(order.getAttr(), attributeName);
		assertEquals(order.getDirection(), Sort.Direction.DESC);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testToSortNull()
	{
		assertNull(sortMapper.toSort(null));
	}

	@Test
	public void testToEditorSort()
	{
		String attr = "attr";
		Sort sort = new Sort(of(new Sort.Order(attr, Sort.Direction.ASC)));
		EditorSort editorSort = sortMapper.toEditorSort(sort);
		assertEquals(editorSort, EditorSort.create(of(EditorOrder.create(attr, "ASC"))));
	}

	@Test
	public void testToEditorSortNull()
	{
		assertNull(sortMapper.toEditorSort(null));
	}
}