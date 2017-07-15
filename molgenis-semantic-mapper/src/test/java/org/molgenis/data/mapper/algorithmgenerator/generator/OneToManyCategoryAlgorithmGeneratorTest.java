package org.molgenis.data.mapper.algorithmgenerator.generator;

import com.google.common.collect.Lists;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

public class OneToManyCategoryAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private OneToManyCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	private Attribute targetAttribute;

	private Attribute sourceAttribute;

	private Attribute sourceAttribute1;

	private Attribute sourceAttribute2;

	private EntityType targetEntityType;

	private EntityType sourceEntityType;

	@BeforeMethod
	public void init()
	{
		DataService dataService = Mockito.mock(DataService.class);
		categoryAlgorithmGenerator = new OneToManyCategoryAlgorithmGenerator(dataService);

		EntityType targetRefEntityType = entityTypeFactory.create("POTATO_REF");
		Attribute targetCodeAttribute = attrMetaFactory.create().setName("code").setDataType(INT);
		Attribute targetLabelAttribute = attrMetaFactory.create().setName("label");
		targetRefEntityType.addAttribute(targetCodeAttribute, ROLE_ID);
		targetRefEntityType.addAttribute(targetLabelAttribute, ROLE_LABEL);

		targetAttribute = attrMetaFactory.create()
										 .setName("Current Consumption Frequency of Potatoes")
										 .setDataType(CATEGORICAL);
		targetAttribute.setRefEntity(targetRefEntityType);

		Entity targetEntity1 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "Almost daily + daily"));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "Several times a week"));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "About once a week"));
		Entity targetEntity4 = new DynamicEntity(targetRefEntityType,
				of("code", 4, "label", "Never + fewer than once a week"));
		Entity targetEntity5 = new DynamicEntity(targetRefEntityType, of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(targetRefEntityType.getId())).thenAnswer(
				invocation -> Stream.of(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5));

		targetEntityType = entityTypeFactory.create("target");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceRefEntityType = createEntityType("LifeLines_POTATO_REF");

		sourceAttribute = attrMetaFactory.create().setName("MESHED_POTATO").setDataType(CATEGORICAL);
		sourceAttribute.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttribute.setRefEntity(sourceRefEntityType);

		Entity sourceEntity1 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "Not this month"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "1 day per month"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "2-3 days per month"));
		Entity sourceEntity4 = new DynamicEntity(targetRefEntityType, of("code", 4, "label", "1 day per week"));
		Entity sourceEntity5 = new DynamicEntity(targetRefEntityType, of("code", 5, "label", "2-3 days per week"));
		Entity sourceEntity6 = new DynamicEntity(targetRefEntityType, of("code", 6, "label", "4-5 days per week"));
		Entity sourceEntity7 = new DynamicEntity(targetRefEntityType, of("code", 7, "label", "6-7 days per week"));

		Mockito.when(dataService.findAll(sourceRefEntityType.getId())).thenAnswer(
				invocation -> Stream.of(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5,
						sourceEntity6, sourceEntity7));

		EntityType sourceRefEntityType1 = createEntityType("Mitchelstown_POTATO_REF");

		sourceAttribute1 = attrMetaFactory.create().setName("MESHED_POTATO_1").setDataType(CATEGORICAL);
		sourceAttribute1.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttribute1.setRefEntity(sourceRefEntityType1);

		Entity sourceEntity8 = new DynamicEntity(targetRefEntityType,
				of("code", 1, "label", "never/less than 1 per month"));
		Entity sourceEntity9 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "1-3 per month"));
		Entity sourceEntity10 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "once a week"));
		Entity sourceEntity11 = new DynamicEntity(targetRefEntityType, of("code", 4, "label", "2-4 per week"));
		Entity sourceEntity12 = new DynamicEntity(targetRefEntityType, of("code", 5, "label", "5-6 per week"));
		Entity sourceEntity13 = new DynamicEntity(targetRefEntityType, of("code", 6, "label", "once a day"));
		Entity sourceEntity14 = new DynamicEntity(targetRefEntityType, of("code", 7, "label", "2-3 per day"));
		Entity sourceEntity15 = new DynamicEntity(targetRefEntityType, of("code", 8, "label", "4-5 per day"));
		Entity sourceEntity16 = new DynamicEntity(targetRefEntityType, of("code", 9, "label", "6+ per day"));

		Mockito.when(dataService.findAll(sourceRefEntityType1.getId())).thenAnswer(
				invocation -> Stream.of(sourceEntity8, sourceEntity9, sourceEntity10, sourceEntity11, sourceEntity12,
						sourceEntity13, sourceEntity14, sourceEntity15, sourceEntity16));

		EntityType sourceRefEntityType2 = createEntityType("Mitchelstown_Stroke_REF");

		sourceAttribute2 = attrMetaFactory.create().setName("Stroke").setDataType(CATEGORICAL);
		sourceAttribute2.setLabel("History of stroke");
		sourceAttribute2.setRefEntity(sourceRefEntityType2);

		Entity sourceEntity17 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "yes"));
		Entity sourceEntity18 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "no"));
		Entity sourceEntity19 = new DynamicEntity(targetRefEntityType, of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(sourceRefEntityType2.getId())).thenAnswer(
				invocation -> Stream.of(sourceEntity17, sourceEntity18, sourceEntity19));

		sourceEntityType = entityTypeFactory.create("source");
		sourceEntityType.addAttributes(Lists.newArrayList(sourceAttribute, sourceAttribute1, sourceAttribute2));
	}

	private EntityType createEntityType(String entityTypeId)
	{
		EntityType sourceRefEntityType = entityTypeFactory.create(entityTypeId);

		Attribute sourceCodeAttribute = attrMetaFactory.create().setName("code").setDataType(INT);
		Attribute sourceLabelAttribute = attrMetaFactory.create().setName("label");
		sourceRefEntityType.addAttribute(sourceCodeAttribute, ROLE_ID);
		sourceRefEntityType.addAttribute(sourceLabelAttribute, ROLE_LABEL);
		return sourceRefEntityType;
	}

	@Test
	public void testIsSuitable()
	{
		Assert.assertTrue(categoryAlgorithmGenerator.isSuitable(targetAttribute,
				Arrays.asList(sourceAttribute, sourceAttribute1)));
	}

	@Test
	public void testGenerate()
	{
		String expected = "var SUM_WEIGHT;\nif($('MESHED_POTATO').isNull().value() && $('MESHED_POTATO_1').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		String generateMultipleAttributes = categoryAlgorithmGenerator.generate(targetAttribute,
				Arrays.asList(sourceAttribute, sourceAttribute1), targetEntityType, sourceEntityType);
		Assert.assertEquals(generateMultipleAttributes, expected);
	}

	@Test
	public void testGenerateWeightedMap()
	{
		String expected = "$('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value()";
		String actual = categoryAlgorithmGenerator.generateWeightedMap(sourceAttribute);

		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testGenerateWeightedMapForTarget()
	{
		Assert.assertEquals(categoryAlgorithmGenerator.groupCategoryValues(targetAttribute),
				".group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();");
	}

	@Test
	public void testGenerateWeightedMapForSource()
	{
		Assert.assertEquals(categoryAlgorithmGenerator.groupCategoryValues(sourceAttribute),
				".group([0,1,2,3,4,5,6,7]).map({\"-0\":\"1\",\"0-1\":\"4\",\"1-2\":\"4\",\"2-3\":\"5\",\"4-5\":\"6\",\"6-7\":\"7\",\"7+\":\"7\"}, null, null).value();");
	}

	@Test
	public void testSuitableForGeneratingWeightedMap()
	{
		Assert.assertTrue(categoryAlgorithmGenerator.suitableForGeneratingWeightedMap(targetAttribute,
				Arrays.asList(sourceAttribute, sourceAttribute1)));

		Assert.assertFalse(categoryAlgorithmGenerator.suitableForGeneratingWeightedMap(targetAttribute,
				Arrays.asList(sourceAttribute, sourceAttribute1, sourceAttribute2)));
	}

	@Test
	public void testHomogenousGenerator()
	{
		String expectedAlgorithm = "var SUM_WEIGHT;\nif($('MESHED_POTATO').isNull().value() && $('MESHED_POTATO_1').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		Assert.assertEquals(
				categoryAlgorithmGenerator.generate(targetAttribute, Arrays.asList(sourceAttribute, sourceAttribute1),
						targetEntityType, sourceEntityType), expectedAlgorithm);
	}

	@Test
	public void testHeterogenousGenerator()
	{
		String expectedAlgorithm = "$('MESHED_POTATO_1').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"3\",\"4\":\"2\",\"5\":\"2\",\"6\":\"1\",\"7\":\"1\",\"8\":\"1\",\"9\":\"1\"}, null, null).value();$('MESHED_POTATO').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"3\",\"5\":\"2\",\"6\":\"2\",\"7\":\"1\"}, null, null).value();$('Stroke').map({\"1\":\"2\",\"2\":\"4\",\"9\":\"9\"}, null, null).value();";
		String actual = categoryAlgorithmGenerator.generate(targetAttribute,
				Arrays.asList(sourceAttribute1, sourceAttribute, sourceAttribute2), targetEntityType, sourceEntityType);
		Assert.assertEquals(actual, expectedAlgorithm);
	}

	@Test
	public void testCreateAlgorithmNullCheck()
	{
		String actual = "var SUM_WEIGHT;\nif($('MESHED_POTATO_1').isNull().value() && $('MESHED_POTATO').isNull().value() && $('Stroke').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}";
		String createAlgorithmNullCheck = categoryAlgorithmGenerator.createAlgorithmNullCheckIfStatement(
				Arrays.asList(sourceAttribute1, sourceAttribute, sourceAttribute2));
		Assert.assertEquals(createAlgorithmNullCheck, actual);
	}

	@Test
	void testCreateAlgorithmElseBlock()
	{
		String actual = "else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		String createAlgorithmElseBlock = categoryAlgorithmGenerator.createAlgorithmElseBlock(targetAttribute,
				Arrays.asList(sourceAttribute1, sourceAttribute, sourceAttribute2));
		Assert.assertEquals(createAlgorithmElseBlock, actual);
	}
}
