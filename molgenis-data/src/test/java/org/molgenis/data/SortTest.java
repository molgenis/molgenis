package org.molgenis.data;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;

public class SortTest
{
	@Test
	public void sortSingleAttrDefault()
	{
		Sort sort = new Sort("attr");
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
	}

	@Test
	public void sortSingleAttrAsc()
	{
		Sort sort = new Sort("attr", Sort.Direction.ASC);
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
	}

	@Test
	public void sortSingleAttrDesc()
	{
		Sort sort = new Sort("attr", Sort.Direction.DESC);
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.DESC);
	}

	@Test
	public void sortSingleAttrBuilderDefault()
	{
		Sort sort = new Sort().on("attr");
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
	}

	@Test
	public void sortSingleAttrBuilderAsc()
	{
		Sort sort = new Sort().on("attr", Sort.Direction.ASC);
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
	}

	@Test
	public void sortSingleAttrBuilderDesc()
	{
		Sort sort = new Sort().on("attr", Sort.Direction.DESC);
		assertEquals(sort.iterator().next().getAttr(), "attr");
		assertEquals(sort.iterator().next().getDirection(), Sort.Direction.DESC);
	}

	@Test
	public void sortMultipleAttrDefault()
	{
		Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0"), new Sort.Order("attr1")));
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0"));
		assertEquals(it.next(), new Sort.Order("attr1"));
	}

	@Test
	public void sortMultipleAttrAsc()
	{
		Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0", Sort.Direction.ASC),
				new Sort.Order("attr1", Sort.Direction.ASC)));
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.ASC));
	}

	@Test
	public void sortMultipleAttrDesc()
	{
		Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0", Sort.Direction.DESC),
				new Sort.Order("attr1", Sort.Direction.DESC)));
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.DESC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
	}

	@Test
	public void sortMultipleAttrAscAndDesc()
	{
		Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0", Sort.Direction.ASC),
				new Sort.Order("attr1", Sort.Direction.DESC)));
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
	}

	@Test
	public void sortMultipleAttrBuilderDefault()
	{
		Sort sort = new Sort().on("attr0").on("attr1");
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0"));
		assertEquals(it.next(), new Sort.Order("attr1"));
	}

	@Test
	public void sortMultipleAttrBuilderAsc()
	{
		Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC);
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.ASC));
	}

	@Test
	public void sortMultipleAttrBuilderDesc()
	{
		Sort sort = new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC);
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.DESC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
	}

	@Test
	public void sortMultipleAttrBuilderAscAndDesc()
	{
		Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC);
		Iterator<Sort.Order> it = sort.iterator();
		assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
		assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
	}
}
