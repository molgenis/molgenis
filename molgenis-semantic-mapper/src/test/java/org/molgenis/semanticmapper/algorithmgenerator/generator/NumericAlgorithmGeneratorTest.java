package org.molgenis.semanticmapper.algorithmgenerator.generator;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticmapper.service.impl.UnitResolverImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.testng.Assert.*;

@ContextConfiguration(classes = NumericAlgorithmGeneratorTest.Config.class)
public class NumericAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private NumericAlgorithmGenerator numericAlgorithmGenerator;

	private EntityType targetEntityType;

	private Attribute targetAttribute;

	private EntityType sourceEntityType;

	private Attribute sourceAttribute;

	private Attribute sourceAttribute1;

	@BeforeMethod
	public void setup()
	{
		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl")).thenReturn(
				Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));

		targetEntityType = entityTypeFactory.create("target");
		targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(DECIMAL);
		targetEntityType.addAttribute(targetAttribute);

		sourceEntityType = entityTypeFactory.create("source");
		sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDataType(DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityType.addAttribute(sourceAttribute);

		sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
		sourceAttribute1.setDataType(DECIMAL);
		sourceAttribute1.setLabel("body length in cm second time");
		sourceEntityType.addAttribute(sourceAttribute1);
	}

	@Test
	public void generate()
	{
		String generate = numericAlgorithmGenerator.generate(targetAttribute, asList(sourceAttribute), targetEntityType,
				sourceEntityType);
		assertEquals(generate, "$('sourceHeight').unit('cm').toUnit('m').value();");

		String generateAverageValue = numericAlgorithmGenerator.generate(targetAttribute,
				asList(sourceAttribute, sourceAttribute1), targetEntityType, sourceEntityType);
		String expected = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('sourceHeight').isNull().value()){\n\tSUM.plus($('sourceHeight').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(!$('sourceHeight1').isNull().value()){\n\tSUM.plus($('sourceHeight1').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\n\tSUM.value();\n}else{\n\tnull;\n}";

		assertEquals(generateAverageValue, expected);
	}

	@Test
	public void generateUnitConversionAlgorithm()
	{
		String generateUnitConversionAlgorithm = numericAlgorithmGenerator.generateUnitConversionAlgorithm(
				targetAttribute, targetEntityType, sourceAttribute, sourceEntityType);
		assertEquals(generateUnitConversionAlgorithm, "$('sourceHeight').unit('cm').toUnit('m').value();");
	}

	@Test
	public void isSuitable()
	{
		Attribute stringAttribute = attrMetaFactory.create().setName("source_string");
		assertTrue(numericAlgorithmGenerator.isSuitable(targetAttribute, singletonList(sourceAttribute)));
		assertFalse(numericAlgorithmGenerator.isSuitable(targetAttribute, asList(sourceAttribute, stringAttribute)));
	}

	@Configuration
	public static class Config
	{
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
		public NumericAlgorithmGenerator numericAlgorithmGenerator()
		{
			return new NumericAlgorithmGenerator(unitResolver());
		}
	}
}
