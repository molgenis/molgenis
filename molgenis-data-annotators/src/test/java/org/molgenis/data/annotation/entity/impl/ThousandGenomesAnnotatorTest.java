package org.molgenis.data.annotation.entity.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ ThousandGenomesAnnotatorTest.Config.class, ThousandGenomesAnnotator.class })
public class ThousandGenomesAnnotatorTest extends AbstractTestNGSpringContextTests
{
	//	private final static String THOUSAND_GENOME_TEST_PATTERN = "ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz";
	//	private final static String THOUSAND_GENOME_TEST_FOLDER_PROPERTY = "/1000g";
	//	private final static String THOUSAND_GENOME_TEST_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	//	private final static String THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY = "X:ALL.chrX.phase3_shapeit2_mvncall_integrated.20130502.genotypes.vcf.gz,Y:ALL.chrY.phase3_integrated.20130502.genotypes.vcf.gz";
	//
	//	@Autowired
	//	RepositoryAnnotator annotator;
	//
	//	@Test
	//	public void testAnnotate()
	//	{
	//		EntityMetaData emdIn = new EntityMetaDataImpl("test");
	//		emdIn.addAttribute(VcfAttributes.CHROM, ROLE_ID);
	//		emdIn.addAttribute(VcfAttributes.POS_META);
	//		emdIn.addAttribute(VcfAttributes.REF_META);
	//		emdIn.addAttribute(VcfAttributes.ALT_META);
	//
	//		Entity inputEntity = new MapEntity(emdIn);
	//		inputEntity.set(VcfAttributes.CHROM, "1");
	//		inputEntity.set(VcfAttributes.POS, 249240543);
	//		inputEntity.set(VcfAttributes.REF, "AGG");
	//		inputEntity.set(VcfAttributes.ALT, "A");
	//
	//		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
	//		assertTrue(results.hasNext());
	//		Entity resultEntity = results.next();
	//		assertFalse(results.hasNext());
	//
	//		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
	//		expectedMap.put(VcfAttributes.CHROM, "1");
	//		expectedMap.put(VcfAttributes.POS, 249240543);
	//		expectedMap.put(VcfAttributes.REF, "AGG");
	//		expectedMap.put(VcfAttributes.ALT, "A");
	//		expectedMap.put(ThousandGenomesAnnotator.THOUSAND_GENOME_AF, "0.61861");
	//		Entity expectedEntity = new MapEntity(expectedMap);
	//
	//		assertEquals(resultEntity.get(VcfAttributes.CHROM), expectedEntity.get(VcfAttributes.CHROM));
	//		assertEquals(resultEntity.get(VcfAttributes.POS), expectedEntity.get(VcfAttributes.POS));
	//		assertEquals(resultEntity.get(VcfAttributes.REF), expectedEntity.get(VcfAttributes.REF));
	//		assertEquals(resultEntity.get(VcfAttributes.ALT), expectedEntity.get(VcfAttributes.ALT));
	//		assertEquals(resultEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF),
	//				expectedEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));
	//	}
	//
	//	@Test
	//	public void testAnnotateNegative()
	//	{
	//		EntityMetaData emdIn = new EntityMetaDataImpl("test");
	//		emdIn.addAttribute(VcfAttributes.CHROM, ROLE_ID);
	//		emdIn.addAttribute(VcfAttributes.POS_META);
	//		emdIn.addAttribute(VcfAttributes.REF_META);
	//		emdIn.addAttribute(VcfAttributes.ALT_META);
	//
	//		Entity inputEntity = new MapEntity(emdIn);
	//		inputEntity.set(VcfAttributes.CHROM, "1");
	//		inputEntity.set(VcfAttributes.POS, 249240543);
	//		inputEntity.set(VcfAttributes.REF, "A");
	//		inputEntity.set(VcfAttributes.ALT, "G");
	//
	//		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
	//		assertTrue(results.hasNext());
	//		Entity resultEntity = results.next();
	//		assertFalse(results.hasNext());
	//
	//		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
	//		expectedMap.put(VcfAttributes.CHROM, "1");
	//		expectedMap.put(VcfAttributes.POS, 249240543);
	//		expectedMap.put(VcfAttributes.REF, "A");
	//		expectedMap.put(VcfAttributes.ALT, "G");
	//		expectedMap.put(ThousandGenomesAnnotator.THOUSAND_GENOME_AF, null);
	//		Entity expectedEntity = new MapEntity(expectedMap);
	//
	//		assertEquals(resultEntity.get(VcfAttributes.CHROM), expectedEntity.get(VcfAttributes.CHROM));
	//		assertEquals(resultEntity.get(VcfAttributes.POS), expectedEntity.get(VcfAttributes.POS));
	//		assertEquals(resultEntity.get(VcfAttributes.REF), expectedEntity.get(VcfAttributes.REF));
	//		assertEquals(resultEntity.get(VcfAttributes.ALT), expectedEntity.get(VcfAttributes.ALT));
	//		assertEquals(resultEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF),
	//				expectedEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));
	//	}
	//
	//	public static class Config
	//	{
	//		@Autowired
	//		private DataService dataService;
	//
	//		@Bean
	//		public Entity thousendGenomesAnnotatorSettings()
	//		{
	//			Entity settings = new MapEntity();
	//
	//			settings.set(ThousendGenomesAnnotatorSettings.Meta.ROOT_DIRECTORY,
	//					ResourceUtils.getFile(getClass(), THOUSAND_GENOME_TEST_FOLDER_PROPERTY).getPath());
	//			settings.set(ThousendGenomesAnnotatorSettings.Meta.CHROMOSOMES, THOUSAND_GENOME_TEST_CHROMOSOMES);
	//			settings.set(ThousendGenomesAnnotatorSettings.Meta.FILEPATTERN, THOUSAND_GENOME_TEST_PATTERN);
	//			settings.set(ThousendGenomesAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES,
	//					THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);
	//
	//			return settings;
	//		}
	//
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public AnnotationService annotationService()
	//		{
	//			return mock(AnnotationService.class);
	//		}
	//
	//		@Bean
	//		public Resources resources()
	//		{
	//			return new ResourcesImpl();
	//		}
	//	}
}
