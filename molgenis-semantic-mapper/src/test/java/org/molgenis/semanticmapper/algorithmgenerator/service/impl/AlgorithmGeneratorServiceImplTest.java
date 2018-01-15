package org.molgenis.semanticmapper.algorithmgenerator.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptParameter;
import org.molgenis.semanticmapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplateService;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplateServiceImpl;
import org.molgenis.semanticmapper.service.impl.UnitResolverImpl;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.script.core.ScriptMetaData.SCRIPT;
import static org.molgenis.script.core.ScriptMetaData.TYPE;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = AlgorithmGeneratorServiceImplTest.Config.class)
public class AlgorithmGeneratorServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

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
		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl")).thenReturn(
				Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));
	}

	@Test
	public void testGenerateTemplateBasedAlgorithm()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		Attribute targetBMIAttribute = attrMetaFactory.create().setName("targetHeight");
		targetBMIAttribute.setLabel("BMI kg/m²");
		targetBMIAttribute.setDataType(DECIMAL);
		targetEntityType.addAttribute(targetBMIAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		Attribute heightSourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		heightSourceAttribute.setDataType(DECIMAL);
		heightSourceAttribute.setLabel("body length in cm");

		Attribute weightSourceAttribute = attrMetaFactory.create().setName("sourceWeight");
		weightSourceAttribute.setDataType(DECIMAL);
		weightSourceAttribute.setLabel("weight in kg");

		sourceEntityType.addAttribute(heightSourceAttribute);
		sourceEntityType.addAttribute(weightSourceAttribute);

		Map<Attribute, ExplainedAttribute> sourceAttributes = ImmutableMap.of(heightSourceAttribute,
				ExplainedAttribute.create(heightSourceAttribute,
						singletonList(ExplainedQueryString.create("height", "height", "height", 100)), true),
				weightSourceAttribute, ExplainedAttribute.create(heightSourceAttribute,
						Collections.singletonList(ExplainedQueryString.create("weight", "weight", "weight", 100)),
						true));

		Script script = mock(Script.class);
		ScriptParameter heightParameter = mock(ScriptParameter.class);
		when(heightParameter.getName()).thenReturn("height");
		ScriptParameter weightParameter = mock(ScriptParameter.class);
		when(weightParameter.getName()).thenReturn("weight");
		when(script.getParameters()).thenReturn(asList(heightParameter, weightParameter));
		when(script.getContent()).thenReturn("$('weight').div($('height').pow(2)).value()");

		when(dataService.findAll(SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME),
				Script.class)).thenReturn(Stream.of(script));

		GeneratedAlgorithm generate = algorithmGeneratorService.generate(targetBMIAttribute, sourceAttributes,
				targetEntityType, sourceEntityType);

		assertEquals(generate.getAlgorithm(), "$('sourceWeight').div($('sourceHeight').div(100.0).pow(2)).value()");
		assertEquals(generate.getAlgorithmState(), AttributeMapping.AlgorithmState.GENERATED_HIGH);
	}

	@Test
	public void testConvertUnitsAlgorithm()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(DECIMAL);
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		Attribute sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDataType(DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityType.addAttribute(sourceAttribute);

		String actualAlgorithm = algorithmGeneratorService.generate(targetAttribute,
				Lists.newArrayList(sourceAttribute), targetEntityType, sourceEntityType);

		String expectedAlgorithm = "$('sourceHeight').unit('cm').toUnit('m').value();";

		assertEquals(actualAlgorithm, expectedAlgorithm);
	}

	@Configuration
	public static class Config
	{
		@Autowired
		private DataService dataService;

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
			return new AlgorithmTemplateServiceImpl(dataService);
		}

		@Bean
		public AlgorithmGeneratorService algorithmGeneratorService()
		{
			return new AlgorithmGeneratorServiceImpl(dataService, unitResolver(), algorithmTemplateService());
		}
	}
}
