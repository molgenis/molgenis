package org.molgenis.data.annotation.entity.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ DannAnnotatorTest.Config.class, DannAnnotator.class })
public class DannAnnotatorTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	RepositoryAnnotator annotator;
	//
	//	@Autowired
	//	Resources resourcess;
	//
	//	// Can annotate
	//	public EntityMetaData metaDataCanAnnotate = new EntityMetaData("test");
	//
	//	// Negative test cannot annotate
	//	public EntityMetaData metaDataCantAnnotate = new EntityMetaData("test");
	//
	//	public ArrayList<Entity> input1;
	//	public ArrayList<Entity> input2;
	//	public ArrayList<Entity> input3;
	//	public ArrayList<Entity> input4;
	//	public static Entity entity;
	//	public static Entity entity1;
	//	public static Entity entity2;
	//	public static Entity entity3;
	//	public static Entity entity4;
	//
	//	public ArrayList<Entity> entities;
	//
	//	public void setValues()
	//	{
	//		AttributeMetaData attributeMetaDataChrom = new AttributeMetaData(CHROM,
	//				STRING);
	//		AttributeMetaData attributeMetaDataPos = new AttributeMetaData(POS,
	//				LONG);
	//		AttributeMetaData attributeMetaDataRef = new AttributeMetaData(REF,
	//				TEXT);
	//		AttributeMetaData attributeMetaDataAlt = new AttributeMetaData(ALT,
	//				TEXT);
	//
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		AttributeMetaData attributeMetaDataCantAnnotateChrom = new AttributeMetaData(CHROM,
	//				LONG);
	//
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataCantAnnotateChrom);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		entity = new MapEntity(metaDataCanAnnotate);
	//		entity1 = new MapEntity(metaDataCanAnnotate);
	//		entity2 = new MapEntity(metaDataCanAnnotate);
	//		entity3 = new MapEntity(metaDataCanAnnotate);
	//		entity4 = new MapEntity(metaDataCanAnnotate);
	//
	//		entities = new ArrayList<>();
	//		entities.add(entity);
	//	}
	//
	//	@BeforeClass
	//	public void beforeClass() throws IOException
	//	{
	//		// Test file
	//		// 1 10001 T A 0.164613914
	//		// 1 10001 T C 0.439699405
	//		// 1 10001 T G 0.381086294
	//
	//		input1 = new ArrayList<>();
	//		input2 = new ArrayList<>();
	//		input3 = new ArrayList<>();
	//		input4 = new ArrayList<>();
	//
	//		setValues();
	//
	//		entity1.set(VcfAttributes.CHROM, "1");
	//		entity1.set(VcfAttributes.POS, 10001L);
	//		entity1.set(VcfAttributes.REF, "T");
	//		entity1.set(VcfAttributes.ALT, "A");
	//
	//		input1.add(entity1);
	//
	//		entity2.set(VcfAttributes.CHROM, "1");
	//		entity2.set(VcfAttributes.POS, 10001L);
	//		entity2.set(VcfAttributes.REF, "T");
	//		entity2.set(VcfAttributes.ALT, "X");
	//
	//		input2.add(entity2);
	//
	//		entity3.set(VcfAttributes.CHROM, "3");
	//		entity3.set(VcfAttributes.POS, 10001L);
	//		entity3.set(VcfAttributes.REF, "T");
	//		entity3.set(VcfAttributes.ALT, "G");
	//
	//		input3.add(entity3);
	//
	//		entity4.set(VcfAttributes.CHROM, "1");
	//		entity4.set(VcfAttributes.POS, 10001L);
	//		entity4.set(VcfAttributes.REF, "T");
	//		entity4.set(VcfAttributes.ALT, "G");
	//
	//		input4.add(entity4);
	//	}
	//
	//	@Test
	//	public void testThreeOccurencesOneMatchEntity1()
	//	{
	//		this.testMatch(input1, "0.16461391399220135");
	//	}
	//
	//	@Test
	//	public void testThreeOccurencesNoMatchEntity2()
	//	{
	//		this.testNoMatch(input2);
	//
	//	}
	//
	//	@Test
	//	public void testNoOccurencesNoMatchEntity3()
	//	{
	//		this.testNoMatch(input3);
	//	}
	//
	//	@Test
	//	public void testThreeOccurencesOneMatchEntity4()
	//	{
	//		this.testMatch(input4, "0.38108629377072734");
	//	}
	//
	//	private void testMatch(List<Entity> inputToAnnotate, Object dannScore)
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(DannAnnotator.DANN_SCORE, dannScore);
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(inputToAnnotate);
	//
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), expectedEntity.get(DannAnnotator.DANN_SCORE));
	//	}
	//
	//	private void testNoMatch(List<Entity> inputToAnnotate)
	//	{
	//		Iterator<Entity> results = annotator.annotate(inputToAnnotate);
	//		Entity resultEntity = results.next();
	//		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), null);
	//	}
	//
	//	@Test
	//	public void canAnnotateTrueTest()
	//	{
	//		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	//	}
	//
	//	@Test
	//	public void canAnnotateFalseTest()
	//	{
	//		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	//	}
	//
	//	public static class Config
	//	{
	//		@Autowired
	//		private DataService dataService;
	//
	//		@Bean
	//		public Entity dannAnnotatorSettings()
	//		{
	//			Entity settings = new MapEntity();
	//			settings.set(DannAnnotatorSettings.Meta.DANN_LOCATION,
	//					ResourceUtils.getFile(getClass(), "/dann/DANN_test_set.tsv.bgz").getPath());
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
