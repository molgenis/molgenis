package org.molgenis.data.annotation.entity.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ ExacAnnotatorTest.Config.class, ExacAnnotator.class })
public class ExacAnnotatorTest extends AbstractTestNGSpringContextTests
{
	//
	//	@Autowired
	//	RepositoryAnnotator annotator;
	//
	//	@Test
	//	public void testAnnotate()
	//	{
	//		EntityMetaData emdIn = new EntityMetaDataImpl("exac");
	//		emdIn.addAttribute(VcfAttributes.CHROM, ROLE_ID);
	//		emdIn.addAttribute(VcfAttributes.POS_META);
	//		emdIn.addAttribute(VcfAttributes.REF_META);
	//		emdIn.addAttribute(VcfAttributes.ALT_META);
	//
	//		Entity inputEntity = new MapEntity(emdIn);
	//		inputEntity.set(VcfAttributes.CHROM, "1");
	//		inputEntity.set(VcfAttributes.POS, 13372);
	//		inputEntity.set(VcfAttributes.REF, "G");
	//		inputEntity.set(VcfAttributes.ALT, "C");
	//
	//		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
	//		assertTrue(results.hasNext());
	//		Entity resultEntity = results.next();
	//		assertFalse(results.hasNext());
	//
	//		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
	//		expectedMap.put(VcfAttributes.CHROM, "1");
	//		expectedMap.put(VcfAttributes.POS, 13372);
	//		expectedMap.put(VcfAttributes.REF, "G");
	//		expectedMap.put(VcfAttributes.ALT, "C");
	//		expectedMap.put(ExacAnnotator.EXAC_AF, "6.998e-05");
	//		Entity expectedEntity = new MapEntity(expectedMap);
	//
	//		assertEquals(resultEntity.get(VcfAttributes.CHROM), expectedEntity.get(VcfAttributes.CHROM));
	//		assertEquals(resultEntity.get(VcfAttributes.POS), expectedEntity.get(VcfAttributes.POS));
	//		assertEquals(resultEntity.get(VcfAttributes.REF), expectedEntity.get(VcfAttributes.REF));
	//		assertEquals(resultEntity.get(VcfAttributes.ALT), expectedEntity.get(VcfAttributes.ALT));
	//		assertEquals(resultEntity.get(ExacAnnotator.EXAC_AF), expectedEntity.get(ExacAnnotator.EXAC_AF));
	//	}
	//
	//	public static class Config
	//	{
	//		@Autowired
	//		private DataService dataService;
	//
	//		@Bean
	//		public Entity exacAnnotatorSettings()
	//		{
	//			Entity settings = new MapEntity();
	//			settings.set(ExacAnnotatorSettings.Meta.EXAC_LOCATION,
	//					ResourceUtils.getFile(getClass(), "/exac/exac_test_set.vcf.gz").getPath());
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
