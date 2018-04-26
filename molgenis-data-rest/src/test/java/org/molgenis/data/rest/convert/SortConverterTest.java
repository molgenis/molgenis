package org.molgenis.data.rest.convert;

import org.molgenis.data.Sort;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SortConverterTest
{
	@Test
	public void convertSingleAttrDefault()
	{
		assertEquals(new SortConverter().convert("attr"), new Sort().on("attr"));
	}

	@Test
	public void convertSingleAttrAsc()
	{
		assertEquals(new SortConverter().convert("attr:asc"), new Sort().on("attr", Sort.Direction.ASC));
	}

	@Test
	public void convertSingleAttrDesc()
	{
		assertEquals(new SortConverter().convert("attr:desc"), new Sort().on("attr", Sort.Direction.DESC));
	}

	@Test
	public void convertMultiAttrDefault()
	{
		assertEquals(new SortConverter().convert("attr0,attr1"), new Sort().on("attr0").on("attr1"));
	}

	@Test
	public void convertMultiAttrAsc()
	{
		assertEquals(new SortConverter().convert("attr0:asc,attr1:asc"),
				new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC));
	}

	@Test
	public void convertMultiAttrDesc()
	{
		assertEquals(new SortConverter().convert("attr0:desc,attr1:desc"),
				new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC));
	}

	@Test
	public void convertMultiAttrAscAndDesc()
	{
		assertEquals(new SortConverter().convert("attr0:asc,attr1:desc"),
				new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC));
	}
}
