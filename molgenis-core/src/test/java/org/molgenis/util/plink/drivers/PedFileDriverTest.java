package org.molgenis.util.plink.drivers;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PedFileDriverTest extends AbstractResourceTest
{
	private PedFileDriver pedfd;

	@BeforeClass
	public void setup() throws Exception
	{
		pedfd = new PedFileDriver(getTestResource("/test.ped"));
	}

	@Test
	public void PED_construct() throws Exception
	{
		assertEquals(6, pedfd.getNrOfElements());
	}

	@Test
	public void PED_getEntries() throws Exception
	{
		// 1 1 0 0 1 1 A A G T
		assertEquals('A', pedfd.getAllEntries().get(0).getBialleles().get(0).getAllele1());
		assertEquals('A', pedfd.getAllEntries().get(0).getBialleles().get(0).getAllele2());
		assertEquals('G', pedfd.getAllEntries().get(0).getBialleles().get(1).getAllele1());
		assertEquals('T', pedfd.getAllEntries().get(0).getBialleles().get(1).getAllele2());

		assertEquals('A', pedfd.getAllEntries().get(1).getBialleles().get(0).getAllele1());
		assertEquals('C', pedfd.getAllEntries().get(1).getBialleles().get(0).getAllele2());
		assertEquals('T', pedfd.getAllEntries().get(1).getBialleles().get(1).getAllele1());
		assertEquals('G', pedfd.getAllEntries().get(1).getBialleles().get(1).getAllele2());

		assertEquals('C', pedfd.getAllEntries().get(5).getBialleles().get(0).getAllele1());
		assertEquals('C', pedfd.getAllEntries().get(5).getBialleles().get(0).getAllele2());
		assertEquals('T', pedfd.getAllEntries().get(5).getBialleles().get(1).getAllele1());
		assertEquals('T', pedfd.getAllEntries().get(5).getBialleles().get(1).getAllele2());

		assertEquals("3", pedfd.getAllEntries().get(2).getFamily());
		assertEquals("0", pedfd.getAllEntries().get(2).getFather());
		assertEquals("0", pedfd.getAllEntries().get(2).getMother());
		assertEquals(1.0, pedfd.getAllEntries().get(2).getPhenotype());
		assertEquals("1", pedfd.getAllEntries().get(2).getIndividual());
		assertEquals(1, pedfd.getAllEntries().get(2).getSex());

		assertEquals("3", pedfd.getEntries(2, 3).get(0).getFamily());
		assertEquals('G', pedfd.getEntries(2, 3).get(0).getBialleles().get(1).getAllele2());
		assertEquals("3", pedfd.getEntries(0, 3).get(2).getFamily());
		assertEquals('G', pedfd.getEntries(0, 3).get(2).getBialleles().get(1).getAllele1());
		assertEquals("4", pedfd.getEntries(2, 4).get(1).getFamily());
		assertEquals(2.0, pedfd.getEntries(2, 4).get(1).getPhenotype());

		assertEquals('C', pedfd.getEntries(0, 6).get(4).getBialleles().get(0).getAllele1());
		assertEquals('C', pedfd.getEntries(1, 6).get(3).getBialleles().get(0).getAllele2());
		assertEquals('G', pedfd.getEntries(2, 6).get(2).getBialleles().get(1).getAllele1());
		assertEquals('T', pedfd.getEntries(3, 6).get(1).getBialleles().get(1).getAllele2());
	}

	@AfterClass
	public void close() throws IOException
	{
		pedfd.close();
	}
}
