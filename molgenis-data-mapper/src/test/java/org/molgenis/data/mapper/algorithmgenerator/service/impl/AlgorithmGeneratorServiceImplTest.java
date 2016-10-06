package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplateService;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplateServiceImpl;
import org.molgenis.data.mapper.service.impl.UnitResolverImpl;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptMetaData.TYPE;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = AlgorithmGeneratorServiceImplTest.Config.class)
public class AlgorithmGeneratorServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

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
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetBMIAttribute = attrMetaFactory.create().setName("targetHeight");
		targetBMIAttribute.setLabel("BMI kg/m²");
		targetBMIAttribute.setDataType(DECIMAL);
		targetEntityMetaData.addAttribute(targetBMIAttribute);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		AttributeMetaData heightSourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		heightSourceAttribute.setDataType(DECIMAL);
		heightSourceAttribute.setLabel("body length in cm");

		AttributeMetaData weightSourceAttribute = attrMetaFactory.create().setName("sourceWeight");
		weightSourceAttribute.setDataType(DECIMAL);
		weightSourceAttribute.setLabel("weight in kg");

		sourceEntityMetaData.addAttribute(heightSourceAttribute);
		sourceEntityMetaData.addAttribute(weightSourceAttribute);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> sourceAttributes = ImmutableMap
				.of(heightSourceAttribute, ExplainedMatchCandidate.create(heightSourceAttribute,
						singletonList(ExplainedQueryString.create("height", "height", "height", 100)), true),
						weightSourceAttribute, ExplainedMatchCandidate.create(heightSourceAttribute, Collections
								.singletonList(ExplainedQueryString.create("weight", "weight", "weight", 100)), true));

		Script script = mock(Script.class);
		ScriptParameter heightParameter = mock(ScriptParameter.class);
		when(heightParameter.getName()).thenReturn("height");
		ScriptParameter weightParameter = mock(ScriptParameter.class);
		when(weightParameter.getName()).thenReturn("weight");
		when(script.getParameters()).thenReturn(asList(heightParameter, weightParameter));
		when(script.getContent()).thenReturn("$('weight').div($('height').pow(2)).value()");

		when(dataService.findAll(SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME), Script.class))
				.thenReturn(Stream.of(script));

		GeneratedAlgorithm generate = algorithmGeneratorService
				.generate(targetBMIAttribute, sourceAttributes, targetEntityMetaData, sourceEntityMetaData);

		assertEquals(generate.getAlgorithm(), "$('sourceWeight').div($('sourceHeight').div(100.0).pow(2)).value()");
		assertEquals(generate.getAlgorithmState(), AttributeMapping.AlgorithmState.GENERATED_HIGH);
	}

	@Test
	public void testConvertUnitsAlgorithm()
	{
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(DECIMAL);
		targetEntityMetaData.addAttribute(targetAttribute);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		AttributeMetaData sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDataType(DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityMetaData.addAttribute(sourceAttribute);

		String actualAlgorithm = algorithmGeneratorService
				.generate(targetAttribute, Lists.newArrayList(sourceAttribute), targetEntityMetaData,
						sourceEntityMetaData);

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
