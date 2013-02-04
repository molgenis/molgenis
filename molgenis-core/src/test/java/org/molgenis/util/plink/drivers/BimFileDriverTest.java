package org.molgenis.util.plink.drivers;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BimFileDriverTest extends AbstractResourceTest
{
	private BimFileDriver bimfd;

	@BeforeClass
	public void setup() throws Exception
	{
		bimfd = new BimFileDriver(getTestResource("/test.bim"), '\t');
	}

	@Test
	public void BIM_construct() throws Exception
	{
		assertEquals(2, bimfd.getNrOfElements());
	}

	@Test
	public void BIM_getEntries() throws Exception
	{
		assertEquals(1, bimfd.getEntries(0, 1).size());
		assertEquals(1, bimfd.getEntries(1, 2).size());
		assertEquals(2, bimfd.getEntries(0, 2).size());
		assertEquals(2, bimfd.getAllEntries().size());

		assertEquals("snp1", bimfd.getEntries(0, 1).get(0).getSNP());
		assertEquals('A', bimfd.getEntries(0, 1).get(0).getBiallele().getAllele1());
		assertEquals(0.0, bimfd.getEntries(1, 2).get(0).getcM());
		assertEquals("1", bimfd.getEntries(1, 2).get(0).getChromosome());

		assertEquals('C', bimfd.getAllEntries().get(0).getBiallele().getAllele2());
		assertEquals('T', bimfd.getAllEntries().get(1).getBiallele().getAllele2());
		assertEquals(1, bimfd.getAllEntries().get(0).getBpPos());
		assertEquals(2, bimfd.getAllEntries().get(1).getBpPos());
		assertEquals("snp2", bimfd.getAllEntries().get(1).getSNP());
	}

	@AfterClass
	public void close() throws IOException
	{
		bimfd.close();
	}
}
