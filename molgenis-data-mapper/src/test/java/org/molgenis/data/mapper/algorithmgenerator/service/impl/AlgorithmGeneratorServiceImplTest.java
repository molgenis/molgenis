package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = AlgorithmGeneratorServiceImplTest.Config.class)
public class AlgorithmGeneratorServiceImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	OntologyService ontologyService;
	//
	//	@Autowired
	//	DataService dataService;
	//
	//	@Autowired
	//	AlgorithmTemplateService algorithmTemplateService;
	//
	//	@Autowired
	//	AlgorithmGeneratorService algorithmGeneratorService;
	//
	//	@BeforeMethod
	//	public void setUpBeforeMethod()
	//	{
	//		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl"))
	//				.thenReturn(Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));
	//	}
	//
	//	@Test
	//	public void testGenerateTemplateBasedAlgorithm()
	//	{
	//		EntityMetaData targetEntityMetaData = new EntityMetaData("target");
	//		AttributeMetaData targetBMIAttribute = new AttributeMetaData("targetHeight");
	//		targetBMIAttribute.setLabel("BMI kg/m²");
	//		targetBMIAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
	//		targetEntityMetaData.addAttribute(targetBMIAttribute);
	//
	//		EntityMetaData sourceEntityMetaData = new EntityMetaData("source");
	//		AttributeMetaData heightSourceAttribute = new AttributeMetaData("sourceHeight");
	//		heightSourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
	//		heightSourceAttribute.setLabel("body length in cm");
	//
	//		AttributeMetaData weightSourceAttribute = new AttributeMetaData("sourceWeight");
	//		weightSourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
	//		weightSourceAttribute.setLabel("weight in kg");
	//
	//		sourceEntityMetaData.addAttribute(heightSourceAttribute);
	//		sourceEntityMetaData.addAttribute(weightSourceAttribute);
	//
	//		Map<AttributeMetaData, ExplainedAttributeMetaData> sourceAttributes = ImmutableMap.of(heightSourceAttribute,
	//				ExplainedAttributeMetaData.create(heightSourceAttribute,
	//						Arrays.asList(ExplainedQueryString.create("height", "height", "height", 100)), true),
	//				weightSourceAttribute, ExplainedAttributeMetaData.create(heightSourceAttribute,
	//						Arrays.asList(ExplainedQueryString.create("weight", "weight", "weight", 100)), true));
	//
	//		Script script = mock(Script.class);
	//		ScriptParameter heightParameter = mock(ScriptParameter.class);
	//		when(heightParameter.getName()).thenReturn("height");
	//		ScriptParameter weightParameter = mock(ScriptParameter.class);
	//		when(weightParameter.getName()).thenReturn("weight");
	//		when(script.getParameters()).thenReturn(Arrays.asList(heightParameter, weightParameter));
	//		when(script.getContent()).thenReturn("$('weight').div($('height').pow(2)).value()");
	//
	//		when(dataService.findAll(SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME), Script.class))
	//				.thenReturn(Stream.of(script));
	//
	//		GeneratedAlgorithm generate = algorithmGeneratorService.generate(targetBMIAttribute, sourceAttributes,
	//				targetEntityMetaData, sourceEntityMetaData);
	//
	//		assertEquals(generate.getAlgorithm(), "$('sourceWeight').div($('sourceHeight').div(100.0).pow(2)).value()");
	//		assertEquals(generate.getAlgorithmState(), AlgorithmState.GENERATED_HIGH);
	//	}
	//
	//	@Test
	//	public void testConvertUnitsAlgorithm()
	//	{
	//		EntityMetaData targetEntityMetaData = new EntityMetaData("target");
	//		AttributeMetaData targetAttribute = new AttributeMetaData("targetHeight");
	//		targetAttribute.setLabel("height in m");
	//		targetAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
	//		targetEntityMetaData.addAttribute(targetAttribute);
	//
	//		EntityMetaData sourceEntityMetaData = new EntityMetaData("source");
	//		AttributeMetaData sourceAttribute = new AttributeMetaData("sourceHeight");
	//		sourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
	//		sourceAttribute.setLabel("body length in cm");
	//		sourceEntityMetaData.addAttribute(sourceAttribute);
	//
	//		String actualAlgorithm = algorithmGeneratorService.generate(targetAttribute,
	//				Lists.newArrayList(sourceAttribute), targetEntityMetaData, sourceEntityMetaData);
	//
	//		String expectedAlgorithm = "$('sourceHeight').unit('cm').toUnit('m').value();";
	//
	//		Assert.assertEquals(actualAlgorithm, expectedAlgorithm);
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public UnitResolver unitResolver()
	//		{
	//			return new UnitResolverImpl(ontologyService());
	//		}
	//
	//		@Bean
	//		public OntologyService ontologyService()
	//		{
	//			return mock(OntologyService.class);
	//		}
	//
	//		@Bean
	//		public AlgorithmTemplateService algorithmTemplateService()
	//		{
	//			return new AlgorithmTemplateServiceImpl(dataService());
	//		}
	//
	//		@Bean
	//		public AlgorithmGeneratorService algorithmGeneratorService()
	//		{
	//			return new AlgorithmGeneratorServiceImpl(dataService(), unitResolver(), algorithmTemplateService());
	//		}
	//	}
}
