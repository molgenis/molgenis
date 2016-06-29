package org.molgenis.data.mapper.algorithmgenerator.generator;

import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.UnitResolverImpl;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
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
import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.testng.Assert.*;

@ContextConfiguration(classes = NumericAlgorithmGeneratorTest.Config.class)
public class NumericAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private NumericAlgorithmGenerator numericAlgorithmGenerator;

	private EntityMetaData targetEntityMetaData;

	private AttributeMetaData targetAttribute;

	private EntityMetaData sourceEntityMetaData;

	private AttributeMetaData sourceAttribute;

	private AttributeMetaData sourceAttribute1;

	@BeforeMethod
	public void setup()
	{
		when(ontologyService.getOntology("http://purl.obolibrary.org/obo/uo.owl"))
				.thenReturn(Ontology.create("1", "http://purl.obolibrary.org/obo/uo.owl", "unit ontology"));

		targetEntityMetaData = entityMetaFactory.create("target");
		targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setLabel("height in m");
		targetAttribute.setDataType(DECIMAL);
		targetEntityMetaData.addAttribute(targetAttribute);

		sourceEntityMetaData = entityMetaFactory.create("source");
		sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDataType(DECIMAL);
		sourceAttribute.setLabel("body length in cm");
		sourceEntityMetaData.addAttribute(sourceAttribute);

		sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
		sourceAttribute1.setDataType(DECIMAL);
		sourceAttribute1.setLabel("body length in cm second time");
		sourceEntityMetaData.addAttribute(sourceAttribute1);
	}

	@Test
	public void generate()
	{
		String generate = numericAlgorithmGenerator
				.generate(targetAttribute, asList(sourceAttribute), targetEntityMetaData, sourceEntityMetaData);
		assertEquals(generate, "$('sourceHeight').unit('cm').toUnit('m').value();");

		String generateAverageValue = numericAlgorithmGenerator
				.generate(targetAttribute, asList(sourceAttribute, sourceAttribute1), targetEntityMetaData,
						sourceEntityMetaData);
		String expected = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('sourceHeight').isNull().value()){\n\tSUM.plus($('sourceHeight').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(!$('sourceHeight1').isNull().value()){\n\tSUM.plus($('sourceHeight1').unit('cm').toUnit('m').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\n\tSUM.value();\n}else{\n\tnull;\n}";

		assertEquals(generateAverageValue, expected);
	}

	@Test
	public void generateUnitConversionAlgorithm()
	{
		String generateUnitConversionAlgorithm = numericAlgorithmGenerator
				.generateUnitConversionAlgorithm(targetAttribute, targetEntityMetaData, sourceAttribute,
						sourceEntityMetaData);
		assertEquals(generateUnitConversionAlgorithm, "$('sourceHeight').unit('cm').toUnit('m').value();");
	}

	@Test
	public void isSuitable()
	{
		AttributeMetaData stringAttribute = attrMetaFactory.create().setName("source_string");
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
