package org.molgenis.data.annotation.entity.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ CaddAnnotatorTest.Config.class, CaddAnnotator.class })
public class CaddAnnotatorTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	RepositoryAnnotator annotator;
	//
	//	@Autowired
	//	Resources resourcess;
	//
	//	public EntityMetaData metaDataCanAnnotate = new EntityMetaData("test");
	//	public EntityMetaData metaDataCantAnnotate = new EntityMetaData("test");
	//
	//	public ArrayList<Entity> input;
	//	public ArrayList<Entity> input1;
	//	public ArrayList<Entity> input2;
	//	public ArrayList<Entity> input3;
	//	public ArrayList<Entity> input4;
	//	public ArrayList<Entity> input5;
	//	public ArrayList<Entity> input6;
	//	public ArrayList<Entity> input7;
	//	public static Entity entity;
	//	public static Entity entity1;
	//	public static Entity entity2;
	//	public static Entity entity3;
	//	public static Entity entity4;
	//	public static Entity entity5;
	//	public static Entity entity6;
	//	public static Entity entity7;
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
	//		AttributeMetaData attributeMetaDataCantAnnotateChrom = new AttributeMetaData(CHROM,
	//				LONG);
	//
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);
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
	//		entity5 = new MapEntity(metaDataCanAnnotate);
	//		entity6 = new MapEntity(metaDataCanAnnotate);
	//		entity7 = new MapEntity(metaDataCanAnnotate);
	//	}
	//
	//	@BeforeClass
	//	public void beforeClass() throws IOException
	//	{
	//		input = new ArrayList<>();
	//		input1 = new ArrayList<>();
	//		input2 = new ArrayList<>();
	//		input3 = new ArrayList<>();
	//		input4 = new ArrayList<>();
	//		input5 = new ArrayList<>();
	//		input6 = new ArrayList<>();
	//		input7 = new ArrayList<>();
	//
	//		setValues();
	//
	//		entity1.set(VcfAttributes.CHROM, "1");
	//		entity1.set(VcfAttributes.POS, 100);
	//		entity1.set(VcfAttributes.REF, "C");
	//		entity1.set(VcfAttributes.ALT, "T");
	//
	//		input1.add(entity1);
	//
	//		entity2.set(VcfAttributes.CHROM, "2");
	//		entity2.set(VcfAttributes.POS, new Long(200));
	//		entity2.set(VcfAttributes.REF, "A");
	//		entity2.set(VcfAttributes.ALT, "C");
	//
	//		input2.add(entity2);
	//
	//		entity3.set(VcfAttributes.CHROM, "3");
	//		entity3.set(VcfAttributes.POS, new Long(300));
	//		entity3.set(VcfAttributes.REF, "G");
	//		entity3.set(VcfAttributes.ALT, "C");
	//
	//		input3.add(entity3);
	//
	//		entity4.set(VcfAttributes.CHROM, "3");
	//		entity4.set(VcfAttributes.POS, new Long(300));
	//		entity4.set(VcfAttributes.REF, "G");
	//		entity4.set(VcfAttributes.ALT, "T,A,C");
	//
	//		input4.add(entity4);
	//
	//		entity5.set(VcfAttributes.CHROM, "3");
	//		entity5.set(VcfAttributes.POS, new Long(300));
	//		entity5.set(VcfAttributes.REF, "GC");
	//		entity5.set(VcfAttributes.ALT, "T,A");
	//
	//		input5.add(entity5);
	//
	//		entity6.set(VcfAttributes.CHROM, "3");
	//		entity6.set(VcfAttributes.POS, new Long(300));
	//		entity6.set(VcfAttributes.REF, "C");
	//		entity6.set(VcfAttributes.ALT, "GX,GC");
	//
	//		input6.add(entity6);
	//
	//		entity7.set(VcfAttributes.CHROM, "3");
	//		entity7.set(VcfAttributes.POS, new Long(300));
	//		entity7.set(VcfAttributes.REF, "C");
	//		entity7.set(VcfAttributes.ALT, "GC");
	//
	//		input7.add(entity7);
	//	}
	//
	//	@Test
	//	public void testThreeOccurencesOneMatch()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "-0.03");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "2.003");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input1);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testTwoOccurencesNoMatch()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input2);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testFourOccurences()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "0.5");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "14.5");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input3);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testFiveMultiAllelic()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "-2.4,0.2,0.5");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "0.123,23.1,14.5");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input4);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testSixMultiAllelicDel()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "-3.4,1.2");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "1.123,24.1");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input5);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testSevenMultiAllelicIns()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "-1.002,1.5");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "3.3,15.5");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input6);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
	//	}
	//
	//	@Test
	//	public void testEightSingleAllelicIns()
	//	{
	//		List<Entity> expectedList = new ArrayList<Entity>();
	//		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
	//
	//		resultMap.put(CaddAnnotator.CADD_ABS, "1.5");
	//		resultMap.put(CaddAnnotator.CADD_SCALED, "15.5");
	//
	//		Entity expectedEntity = new MapEntity(resultMap);
	//		expectedList.add(expectedEntity);
	//
	//		Iterator<Entity> results = annotator.annotate(input7);
	//		Entity resultEntity = results.next();
	//
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), expectedEntity.get(CaddAnnotator.CADD_ABS));
	//		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), expectedEntity.get(CaddAnnotator.CADD_SCALED));
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
	//		public Entity caddAnnotatorSettings()
	//		{
	//			Entity settings = new MapEntity();
	//			settings.set(CaddAnnotatorSettings.Meta.CADD_LOCATION,
	//					ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz").getPath());
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
