package org.molgenis.util.plink.drivers;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BedFileDriverTest extends AbstractResourceTest
{
	private BedFileDriver bedfd;

	@BeforeClass
	public void setup() throws Exception
	{
		bedfd = new BedFileDriver(getTestResource("/test.bed"));
	}

	@Test
	public void BED_construct() throws Exception
	{
		assertEquals(1, bedfd.getMode());
		assertEquals(16, bedfd.getNrOfElements());
	}

	@Test
	public void BED_getElement() throws Exception
	{
		assertEquals("00", bedfd.getElement(0));
		assertEquals("01", bedfd.getElement(1));
		assertEquals("11", bedfd.getElement(2));
		assertEquals("01", bedfd.getElement(3));
		assertEquals("11", bedfd.getElement(4));
		assertEquals("11", bedfd.getElement(5));
		assertEquals("00", bedfd.getElement(6));
		assertEquals("00", bedfd.getElement(7));
		assertEquals("01", bedfd.getElement(8));
		assertEquals("01", bedfd.getElement(9));
		assertEquals("00", bedfd.getElement(10));
		assertEquals("11", bedfd.getElement(11));
		assertEquals("01", bedfd.getElement(12));
		assertEquals("11", bedfd.getElement(13));
		assertEquals("00", bedfd.getElement(14));
		assertEquals("00", bedfd.getElement(15));
	}
}
