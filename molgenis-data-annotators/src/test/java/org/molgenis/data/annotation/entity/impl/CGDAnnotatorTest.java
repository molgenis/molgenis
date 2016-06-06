package org.molgenis.data.annotation.entity.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

///***
// * Clinical Genomics Database Test
// */
//@ContextConfiguration(classes =
//{ CGDAnnotatorTest.Config.class, CGDAnnotator.class })
public class CGDAnnotatorTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	RepositoryAnnotator annotator;
	//
	//	@Test
	//	public void annotateTestMatch()
	//	{
	//		EntityMetaData emdIn = new EntityMetaData("Test");
	//		emdIn.addAttribute(GENE.getAttributeName(), ROLE_ID);
	//		Entity inputEntity = new MapEntity(emdIn);
	//		inputEntity.set(GENE.getAttributeName(), "LEPR");
	//
	//		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
	//		assertTrue(results.hasNext());
	//		Entity resultEntity = results.next();
	//		assertFalse(results.hasNext());
	//
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//		resultMap.put(GENE.getAttributeName(), "LEPR");
	//		resultMap.put(HGNC_ID.getAttributeName(), "6554");
	//		resultMap.put(ENTREZ_GENE_ID.getAttributeName(), "3953");
	//		resultMap.put(CONDITION.getAttributeName(), "Leptin receptor deficiency");
	//		resultMap.put(INHERITANCE.getAttributeName(), "AR");
	//		resultMap.put(AGE_GROUP.getAttributeName(), "Pediatric");
	//		resultMap.put(MANIFESTATION_CATEGORIES.getAttributeName(), "Allergy/Immunology/Infectious; Endocrine");
	//		resultMap.put(INTERVENTION_CATEGORIES.getAttributeName(), "Allergy/Immunology/Infectious; Endocrine");
	//		resultMap.put(COMMENTS.getAttributeName(),
	//				"Standard treatments for obesity, such as gastric surgery, have been described as beneficial");
	//		resultMap.put(INTERVENTION_RATIONALE.getAttributeName(),
	//				"In addition to endocrine manifestations, individuals may be susceptible to infections (eg, respiratory infections), which, coupled with other manifestations (eg, severe obesity) can have severe sequelae such that prophylaxis and rapid treatment may be beneficial");
	//		resultMap.put(REFERENCES.getAttributeName(), "8666155; 9537324; 17229951; 21306929; 23275530; 23616257");
	//		Entity expectedEntity = new MapEntity(resultMap);
	//
	//		assertEquals(resultEntity.get(GENE.getAttributeName()), expectedEntity.get(GENE.getAttributeName()));
	//		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()),
	//				expectedEntity.get(ENTREZ_GENE_ID.getAttributeName()));
	//		assertEquals(resultEntity.get(CONDITION.getAttributeName()), expectedEntity.get(CONDITION.getAttributeName()));
	//		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()),
	//				expectedEntity.get(INHERITANCE.getAttributeName()));
	//		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), expectedEntity.get(AGE_GROUP.getAttributeName()));
	//		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()),
	//				expectedEntity.get(ALLELIC_CONDITIONS.getAttributeName()));
	//		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()),
	//				expectedEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()));
	//		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()),
	//				expectedEntity.get(INTERVENTION_CATEGORIES.getAttributeName()));
	//		assertEquals(resultEntity.get(COMMENTS.getAttributeName()), expectedEntity.get(COMMENTS.getAttributeName()));
	//		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()),
	//				expectedEntity.get(INTERVENTION_RATIONALE.getAttributeName()));
	//		assertEquals(resultEntity.get(REFERENCES.getAttributeName()),
	//				expectedEntity.get(REFERENCES.getAttributeName()));
	//	}
	//
	//	@Test
	//	public void annotateTestNoMatch()
	//	{
	//		EntityMetaData emdIn = new EntityMetaData("Test");
	//		emdIn.addAttribute(GENE.getAttributeName(), ROLE_ID);
	//
	//		Entity inputEntity = new MapEntity(emdIn);
	//		inputEntity.set(GENE.getAttributeName(), "BOGUS");
	//
	//		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
	//		assertTrue(results.hasNext());
	//		Entity resultEntity = results.next();
	//		assertFalse(results.hasNext());
	//
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//		resultMap.put(GENE.getAttributeName(), "BOGUS");
	//		Entity expectedEntity = new MapEntity(resultMap);
	//
	//		assertEquals(resultEntity.get(GENE.getAttributeName()), expectedEntity.get(GENE.getAttributeName()));
	//		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()),
	//				expectedEntity.get(ENTREZ_GENE_ID.getAttributeName()));
	//		assertEquals(resultEntity.get(CONDITION.getAttributeName()), expectedEntity.get(CONDITION.getAttributeName()));
	//		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()),
	//				expectedEntity.get(INHERITANCE.getAttributeName()));
	//		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), expectedEntity.get(AGE_GROUP.getAttributeName()));
	//		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()),
	//				expectedEntity.get(ALLELIC_CONDITIONS.getAttributeName()));
	//		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()),
	//				expectedEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()));
	//		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()),
	//				expectedEntity.get(INTERVENTION_CATEGORIES.getAttributeName()));
	//		assertEquals(resultEntity.get(COMMENTS.getAttributeName()), expectedEntity.get(COMMENTS.getAttributeName()));
	//		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()),
	//				expectedEntity.get(INTERVENTION_RATIONALE.getAttributeName()));
	//		assertEquals(resultEntity.get(REFERENCES.getAttributeName()),
	//				expectedEntity.get(REFERENCES.getAttributeName()));
	//	}
	//
	//	public static class Config
	//	{
	//		@Autowired
	//		private DataService dataService;
	//
	//		@Bean
	//		public Entity CGDAnnotatorSettings()
	//		{
	//			Entity settings = new MapEntity();
	//			settings.set(CGDAnnotatorSettings.Meta.CGD_LOCATION,
	//					ResourceUtils.getFile(getClass(), "/cgd_example.txt").getPath());
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
