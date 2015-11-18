package org.molgenis.data.mapper.algorithmgenerator.generator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.UnitResolverImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = NumericAlgorithmGeneratorTest.Config.class)
public class NumericAlgorithmGeneratorTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	OntologyService ontologyService;

	@Autowired
	NumericAlgorithmGenerator numericAlgorithmGenerator;

	DefaultEntityMetaData targetEntityMetaData;

	DefaultAttributeMetaData targetAttribute;

	DefaultEntityMetaData sourceEntityMetaData;

	DefaultAttributeMetaData sourceAttribute;

	DefaultAttributeMetaData sourceAttribute1;

	@BeforeMethod
	public void setup()
	{
		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl"))
				.thenReturn(Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));

		targetEntityMetaData = new DefaultEntityMetaData("target");
		targetAttribute = new DefaultAttributeMetaData("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		targetEntityMetaData.addAttributeMetaData(targetAttribute);

		sourceEntityMetaData = new DefaultEntityMetaData("source");
		sourceAttribute = new DefaultAttributeMetaData("sourceHeight");
		sourceAttribute.setDataType(MolgenisFieldTypes.DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityMetaData.addAttributeMetaData(sourceAttribute);

		sourceAttribute1 = new DefaultAttributeMetaData("sourceHeight1");
		sourceAttribute1.setDataType(MolgenisFieldTypes.DECIMAL);
		sourceAttribute1.setLabel("body length in cm second time");
		sourceEntityMetaData.addAttributeMetaData(sourceAttribute1);
	}

	@Test
	public void generate()
	{
		String generate = numericAlgorithmGenerator.generate(targetAttribute, Arrays.asList(sourceAttribute),
				targetEntityMetaData, sourceEntityMetaData);
		assertEquals(generate, "$('sourceHeight').unit('cm').toUnit('m').value();");

		String generateAverageValue = numericAlgorithmGenerator.generate(targetAttribute,
				Arrays.asList(sourceAttribute, sourceAttribute1), targetEntityMetaData, sourceEntityMetaData);
		String expected = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('sourceHeight').isNull().value()){\n\tSUM.plus($('sourceHeight').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(!$('sourceHeight1').isNull().value()){\n\tSUM.plus($('sourceHeight1').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\n\tSUM.value();\n}else{\n\tnull;\n}";

		assertEquals(generateAverageValue, expected);
	}

	@Test
	public void generateUnitConversionAlgorithm()
	{
		String generateUnitConversionAlgorithm = numericAlgorithmGenerator.generateUnitConversionAlgorithm(
				targetAttribute, targetEntityMetaData, sourceAttribute, sourceEntityMetaData);
		assertEquals(generateUnitConversionAlgorithm, "$('sourceHeight').unit('cm').toUnit('m').value();");
	}

	@Test
	public void isSuitable()
	{
		DefaultAttributeMetaData stringAttribute = new DefaultAttributeMetaData("source_string",
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		assertTrue(numericAlgorithmGenerator.isSuitable(targetAttribute, Arrays.asList(sourceAttribute)));
		assertFalse(
				numericAlgorithmGenerator.isSuitable(targetAttribute, Arrays.asList(sourceAttribute, stringAttribute)));
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
