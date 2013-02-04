package org.molgenis.util.plink.drivers;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FamFileDriverTest extends AbstractResourceTest
{
	private FamFileDriver famfd;

	@BeforeClass
	public void setup() throws Exception
	{
		famfd = new FamFileDriver(getTestResource("/test.fam"));
	}

	@Test
	public void FAM_construct() throws Exception
	{
		assertEquals(6, famfd.getNrOfElements());
	}

	@Test
	public void FAM_getEntries() throws Exception
	{
		assertEquals(1, famfd.getEntries(0, 1).size());
		assertEquals(1, famfd.getEntries(1, 2).size());
		assertEquals(2, famfd.getEntries(0, 2).size());
		assertEquals(6, famfd.getEntries(0, 6).size());

		assertEquals("1", famfd.getEntries(0, 1).get(0).getFamily());
		assertEquals("2", famfd.getEntries(0, 2).get(1).getFamily());

		assertEquals("5", famfd.getEntries(3, 5).get(1).getFamily());
		assertEquals("6", famfd.getEntries(0, 6).get(5).getFamily());

		assertEquals(1.0, famfd.getAllEntries().get(2).getPhenotype());
		assertEquals(2.0, famfd.getAllEntries().get(3).getPhenotype());
	}

	@AfterClass
	public void close() throws IOException
	{
		famfd.close();
	}
}
