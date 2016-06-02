package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptMetaData.TYPE;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplateService;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplateServiceImpl;
import org.molgenis.data.mapper.service.impl.UnitResolverImpl;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@ContextConfiguration(classes = AlgorithmGeneratorServiceImplTest.Config.class)
public class AlgorithmGeneratorServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;

	@Autowired
	DataService dataService;

	@Autowired
	AlgorithmTemplateService algorithmTemplateService;

	@Autowired
	AlgorithmGeneratorService algorithmGeneratorService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl"))
				.thenReturn(Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));
	}

	@Test
	public void testGenerateTemplateBasedAlgorithm()
	{
		EntityMetaData targetEntityMetaData = new EntityMetaDataImpl("target");
		AttributeMetaData targetBMIAttribute = new AttributeMetaData("targetHeight");
		targetBMIAttribute.setLabel("BMI kg/m²");
		targetBMIAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		targetEntityMetaData.addAttribute(targetBMIAttribute);

		EntityMetaData sourceEntityMetaData = new EntityMetaDataImpl("source");
		AttributeMetaData heightSourceAttribute = new AttributeMetaData("sourceHeight");
		heightSourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		heightSourceAttribute.setLabel("body length in cm");

		AttributeMetaData weightSourceAttribute = new AttributeMetaData("sourceWeight");
		weightSourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		weightSourceAttribute.setLabel("weight in kg");

		sourceEntityMetaData.addAttribute(heightSourceAttribute);
		sourceEntityMetaData.addAttribute(weightSourceAttribute);

		Map<AttributeMetaData, ExplainedAttributeMetaData> sourceAttributes = ImmutableMap.of(heightSourceAttribute,
				ExplainedAttributeMetaData.create(heightSourceAttribute,
						Arrays.asList(ExplainedQueryString.create("height", "height", "height", 100)), true),
				weightSourceAttribute, ExplainedAttributeMetaData.create(heightSourceAttribute,
						Arrays.asList(ExplainedQueryString.create("weight", "weight", "weight", 100)), true));

		Script script = mock(Script.class);
		ScriptParameter heightParameter = mock(ScriptParameter.class);
		when(heightParameter.getName()).thenReturn("height");
		ScriptParameter weightParameter = mock(ScriptParameter.class);
		when(weightParameter.getName()).thenReturn("weight");
		when(script.getParameters()).thenReturn(Arrays.asList(heightParameter, weightParameter));
		when(script.getContent()).thenReturn("$('weight').div($('height').pow(2)).value()");

		when(dataService.findAll(SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME), Script.class))
				.thenReturn(Stream.of(script));

		GeneratedAlgorithm generate = algorithmGeneratorService.generate(targetBMIAttribute, sourceAttributes,
				targetEntityMetaData, sourceEntityMetaData);

		assertEquals(generate.getAlgorithm(), "$('sourceWeight').div($('sourceHeight').div(100.0).pow(2)).value()");
		assertEquals(generate.getAlgorithmState(), AlgorithmState.GENERATED_HIGH);
	}

	@Test
	public void testConvertUnitsAlgorithm()
	{
		EntityMetaData targetEntityMetaData = new EntityMetaDataImpl("target");
		AttributeMetaData targetAttribute = new AttributeMetaData("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		targetEntityMetaData.addAttribute(targetAttribute);

		EntityMetaData sourceEntityMetaData = new EntityMetaDataImpl("source");
		AttributeMetaData sourceAttribute = new AttributeMetaData("sourceHeight");
		sourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityMetaData.addAttribute(sourceAttribute);

		String actualAlgorithm = algorithmGeneratorService.generate(targetAttribute,
				Lists.newArrayList(sourceAttribute), targetEntityMetaData, sourceEntityMetaData);

		String expectedAlgorithm = "$('sourceHeight').unit('cm').toUnit('m').value();";

		Assert.assertEquals(actualAlgorithm, expectedAlgorithm);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public UnitResolver unitResolver()
		{
			return new UnitResolverImpl(ontologyService());
		}

		@Bean
		public OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		public AlgorithmTemplateService algorithmTemplateService()
		{
			return new AlgorithmTemplateServiceImpl(dataService());
		}

		@Bean
		public AlgorithmGeneratorService algorithmGeneratorService()
		{
			return new AlgorithmGeneratorServiceImpl(dataService(), unitResolver(), algorithmTemplateService());
		}
	}
}
