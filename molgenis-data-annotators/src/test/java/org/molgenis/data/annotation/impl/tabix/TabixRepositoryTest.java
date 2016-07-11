package org.molgenis.data.annotation.impl.tabix;

public class TabixRepositoryTest
{
	//	private TabixRepository tabixRepository;
	//	private EntityMetaDataImpl repoMetaData;
	//
	//	@BeforeTest
	//	public void beforeTest() throws IOException
	//	{
	//		repoMetaData = new EntityMetaDataImpl("CaddTest");
	//		repoMetaData.addAttribute(CHROM_META);
	//		repoMetaData.addAttribute(POS_META);
	//		repoMetaData.addAttribute(REF_META);
	//		repoMetaData.addAttribute(ALT_META);
	//		repoMetaData.addAttribute(new AttributeMetaData("CADD", DECIMAL));
	//		repoMetaData.addAttribute(new AttributeMetaData("CADD_SCALED", DECIMAL));
	//		repoMetaData.addAttribute("id", ROLE_ID).setVisible(false);
	//		File file = ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz");
	//		tabixRepository = new TabixRepository(file, repoMetaData);
	//	}
	//
	//	@Test
	//	public void testGetEntityMetaData()
	//	{
	//		assertEquals(tabixRepository.getEntityMetaData(), repoMetaData);
	//	}
	//
	//	@Test
	//	public void testQuery()
	//	{
	//		Query<Entity> query = tabixRepository.query().eq(VcfAttributes.CHROM, "1").and().eq(VcfAttributes.POS, "100");
	//		assertEquals(tabixRepository.findAll(query).collect(toList()),
	//				Arrays.asList(newEntity("1", 100, "C", "T", -0.03, 2.003), newEntity("1", 100, "C", "G", -0.4, 4.321),
	//						newEntity("1", 100, "C", "A", 2.102, 43.2)));
	//	}
	//
	//	/**
	//	 * If the chromosome send to the TabixIterator is unknown in the inputfile the TabixIterator throws an
	//	 * IndexOutOfBoundsException We want to log this, but we don't want the annotationrun to fail The most frequent
	//	 * example of this is a Variant found in the Mitochondrial DNA, chrom=MT, these are not in our Tabix file for for
	//	 * example CADD. This test checks if the Annotator does not throw an exception but returns an empty list instead.
	//	 */
	//	@Test
	//	public void testUnknownChromosome()
	//	{
	//		Query<Entity> query = tabixRepository.query().eq(VcfAttributes.CHROM, "MT").and().eq(VcfAttributes.POS, "100");
	//		assertEquals(tabixRepository.findAll(query).collect(toList()), emptyList());
	//	}
	//
	//	@Test
	//	public void testIterator()
	//	{
	//		assertEquals(stream(tabixRepository.spliterator(), false).collect(toList()),
	//				Arrays.asList(newEntity("1", 100, "C", "T", -0.03, 2.003), newEntity("1", 100, "C", "G", -0.4, 4.321),
	//						newEntity("1", 100, "C", "A", 2.102, 43.2), newEntity("2", 200, "A", "T", 2.0, 3.012),
	//						newEntity("2", 200, "A", "G", -2.30, 20.2), newEntity("3", 300, "G", "A", 0.2, 23.1),
	//						newEntity("3", 300, "G", "T", -2.4, 0.123), newEntity("3", 300, "G", "X", -0.002, 2.3),
	//						newEntity("3", 300, "G", "C", 0.5, 14.5), newEntity("3", 300, "GC", "A", 1.2, 24.1),
	//						newEntity("3", 300, "GC", "T", -3.4, 1.123), newEntity("3", 300, "C", "GX", -1.002, 3.3),
	//						newEntity("3", 300, "C", "GC", 1.5, 15.5)));
	//	}
	//
	//	private Entity newEntity(String chrom, long pos, String ref, String alt, double cadd, double caddScaled)
	//	{
	//		Entity result = new MapEntity(repoMetaData);
	//		result.set(CHROM, chrom);
	//		result.set(POS, pos);
	//		result.set(REF, ref);
	//		result.set(ALT, alt);
	//		result.set("CADD", cadd);
	//		result.set("CADD_SCALED", caddScaled);
	//		return result;
	//	}
}