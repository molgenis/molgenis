package org.molgenis.util.plink.drivers;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.List;

import org.molgenis.util.plink.datatypes.Biallele;
import org.molgenis.util.plink.datatypes.TpedEntry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TpedFileDriverTest extends AbstractResourceTest
{
	private TpedFileDriver tpedfd;

	@BeforeClass
	public void setup() throws Exception
	{
		tpedfd = new TpedFileDriver(getTestResource("/test.tped"));
	}

	@Test
	public void TPED_construct() throws Exception
	{
		assertEquals(2, tpedfd.getNrOfElements());
	}

	@Test
	public void TPED_getEntries() throws Exception
	{
		List<TpedEntry> entries = tpedfd.getAllEntries();

		List<Biallele> bialleles = entries.get(0).getBialleles();
		assertEquals(6, bialleles.size());
		assertEquals('A', bialleles.get(0).getAllele1());
		assertEquals('A', bialleles.get(0).getAllele2());
		assertEquals('A', bialleles.get(1).getAllele1());
		assertEquals('C', bialleles.get(1).getAllele2());
		assertEquals('C', bialleles.get(2).getAllele1());
		assertEquals('C', bialleles.get(2).getAllele2());
		assertEquals('A', bialleles.get(3).getAllele1());
		assertEquals('C', bialleles.get(3).getAllele2());
		assertEquals('C', bialleles.get(4).getAllele1());
		assertEquals('C', bialleles.get(4).getAllele2());
		assertEquals('C', bialleles.get(5).getAllele1());
		assertEquals('C', bialleles.get(5).getAllele2());
	}

	@AfterClass
	public void close() throws IOException
	{
		tpedfd.close();
	}
}
