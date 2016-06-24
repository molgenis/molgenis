package org.molgenis.data.mapper.algorithmgenerator.generator;

import static com.google.common.collect.ImmutableMap.of;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class OneToManyCategoryAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	private OneToManyCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	private AttributeMetaData targetAttributeMetaData;

	private AttributeMetaData sourceAttributeMetaData;

	private AttributeMetaData sourceAttributeMetaData1;

	private AttributeMetaData sourceAttributeMetaData2;

	private EntityMetaData targetEntityMetaData;

	private EntityMetaData sourceEntityMetaData;

	@BeforeMethod
	public void init()
	{
		DataService dataService = Mockito.mock(DataService.class);
		categoryAlgorithmGenerator = new OneToManyCategoryAlgorithmGenerator(dataService);

		EntityMetaData targetRefEntityMeta = entityMetaFactory.create("POTATO_REF");
		AttributeMetaData targetCodeAttributeMetaData = attrMetaFactory.create().setName("code").setDataType(INT);
		AttributeMetaData targetLabelAttributeMetaData = attrMetaFactory.create().setName("label");
		targetRefEntityMeta.addAttribute(targetCodeAttributeMetaData, ROLE_ID);
		targetRefEntityMeta.addAttribute(targetLabelAttributeMetaData, ROLE_LABEL);

		targetAttributeMetaData = attrMetaFactory.create().setName("Current Consumption Frequency of Potatoes")
				.setDataType(CATEGORICAL);
		targetAttributeMetaData.setRefEntity(targetRefEntityMeta);

		Entity targetEntity1 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "Almost daily + daily"));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "Several times a week"));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "About once a week"));
		Entity targetEntity4 = new DynamicEntity(targetRefEntityMeta,
				of("code", 4, "label", "Never + fewer than once a week"));
		Entity targetEntity5 = new DynamicEntity(targetRefEntityMeta, of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(targetRefEntityMeta.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5);
			}
		});

		targetEntityMetaData = entityMetaFactory.create("target");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);

		EntityMetaData sourceRefEntityMetaData = createEntityMetaData("LifeLines_POTATO_REF");

		sourceAttributeMetaData = attrMetaFactory.create().setName("MESHED_POTATO").setDataType(CATEGORICAL);
		sourceAttributeMetaData.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData.setRefEntity(sourceRefEntityMetaData);

		Entity sourceEntity1 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "Not this month"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "1 day per month"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "2-3 days per month"));
		Entity sourceEntity4 = new DynamicEntity(targetRefEntityMeta, of("code", 4, "label", "1 day per week"));
		Entity sourceEntity5 = new DynamicEntity(targetRefEntityMeta, of("code", 5, "label", "2-3 days per week"));
		Entity sourceEntity6 = new DynamicEntity(targetRefEntityMeta, of("code", 6, "label", "4-5 days per week"));
		Entity sourceEntity7 = new DynamicEntity(targetRefEntityMeta, of("code", 7, "label", "6-7 days per week"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream
						.of(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5, sourceEntity6,
								sourceEntity7);
			}
		});

		EntityMetaData sourceRefEntityMetaData1 = createEntityMetaData("Mitchelstown_POTATO_REF");

		sourceAttributeMetaData1 = attrMetaFactory.create().setName("MESHED_POTATO_1").setDataType(CATEGORICAL);
		sourceAttributeMetaData1.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData1.setRefEntity(sourceRefEntityMetaData1);

		Entity sourceEntity8 = new DynamicEntity(targetRefEntityMeta,
				of("code", 1, "label", "never/less than 1 per month"));
		Entity sourceEntity9 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "1-3 per month"));
		Entity sourceEntity10 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "once a week"));
		Entity sourceEntity11 = new DynamicEntity(targetRefEntityMeta, of("code", 4, "label", "2-4 per week"));
		Entity sourceEntity12 = new DynamicEntity(targetRefEntityMeta, of("code", 5, "label", "5-6 per week"));
		Entity sourceEntity13 = new DynamicEntity(targetRefEntityMeta, of("code", 6, "label", "once a day"));
		Entity sourceEntity14 = new DynamicEntity(targetRefEntityMeta, of("code", 7, "label", "2-3 per day"));
		Entity sourceEntity15 = new DynamicEntity(targetRefEntityMeta, of("code", 8, "label", "4-5 per day"));
		Entity sourceEntity16 = new DynamicEntity(targetRefEntityMeta, of("code", 9, "label", "6+ per day"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData1.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(sourceEntity8, sourceEntity9, sourceEntity10, sourceEntity11, sourceEntity12,
						sourceEntity13, sourceEntity14, sourceEntity15, sourceEntity16);
			}
		});

		EntityMetaData sourceRefEntityMetaData2 = createEntityMetaData("Mitchelstown_Stroke_REF");

		sourceAttributeMetaData2 = attrMetaFactory.create().setName("Stroke").setDataType(CATEGORICAL);
		sourceAttributeMetaData2.setLabel("History of stroke");
		sourceAttributeMetaData2.setRefEntity(sourceRefEntityMetaData2);

		Entity sourceEntity17 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "yes"));
		Entity sourceEntity18 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "no"));
		Entity sourceEntity19 = new DynamicEntity(targetRefEntityMeta, of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData2.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(sourceEntity17, sourceEntity18, sourceEntity19);
			}
		});

		sourceEntityMetaData = entityMetaFactory.create("source");
		sourceEntityMetaData.addAttributes(
				Lists.newArrayList(sourceAttributeMetaData, sourceAttributeMetaData1, sourceAttributeMetaData2));
	}

	private EntityMetaData createEntityMetaData(String entityName)
	{
		EntityMetaData sourceRefEntityMetaData = entityMetaFactory.create(entityName);

		AttributeMetaData sourceCodeAttributeMetaData = attrMetaFactory.create().setName("code").setDataType(INT);
		AttributeMetaData sourceLabelAttributeMetaData = attrMetaFactory.create().setName("label");
		sourceRefEntityMetaData.addAttribute(sourceCodeAttributeMetaData, ROLE_ID);
		sourceRefEntityMetaData.addAttribute(sourceLabelAttributeMetaData, ROLE_LABEL);
		return sourceRefEntityMetaData;
	}

	@Test
	public void testIsSuitable()
	{
		Assert.assertTrue(categoryAlgorithmGenerator
				.isSuitable(targetAttributeMetaData, Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1)));
	}

	@Test
	public void testGenerate()
	{
		String expected = "var SUM_WEIGHT;\nif($('MESHED_POTATO').isNull().value() && $('MESHED_POTATO_1').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		String generateMultipleAttributes = categoryAlgorithmGenerator
				.generate(targetAttributeMetaData, Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1),
						targetEntityMetaData, sourceEntityMetaData);
		Assert.assertEquals(generateMultipleAttributes, expected);
	}

	@Test
	public void testGenerateWeightedMap()
	{
		String expected = "$('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value()";
		String actual = categoryAlgorithmGenerator.generateWeightedMap(sourceAttributeMetaData);

		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testGenerateWeightedMapForTarget()
	{
		Assert.assertEquals(categoryAlgorithmGenerator.groupCategoryValues(targetAttributeMetaData),
				".group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();");
	}

	@Test
	public void testGenerateWeightedMapForSource()
	{
		Assert.assertEquals(categoryAlgorithmGenerator.groupCategoryValues(sourceAttributeMetaData),
				".group([0,1,2,3,4,5,6,7]).map({\"-0\":\"1\",\"0-1\":\"4\",\"1-2\":\"4\",\"2-3\":\"5\",\"4-5\":\"6\",\"6-7\":\"7\",\"7+\":\"7\"}, null, null).value();");
	}

	@Test
	public void testSuitableForGeneratingWeightedMap()
	{
		Assert.assertTrue(categoryAlgorithmGenerator.suitableForGeneratingWeightedMap(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1)));

		Assert.assertFalse(categoryAlgorithmGenerator.suitableForGeneratingWeightedMap(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1, sourceAttributeMetaData2)));
	}

	@Test
	public void testHomogenousGenerator()
	{
		String expectedAlgorithm = "var SUM_WEIGHT;\nif($('MESHED_POTATO').isNull().value() && $('MESHED_POTATO_1').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		Assert.assertEquals(categoryAlgorithmGenerator
				.generate(targetAttributeMetaData, Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1),
						targetEntityMetaData, sourceEntityMetaData), expectedAlgorithm);
	}

	@Test
	public void testHeterogenousGenerator()
	{
		String expectedAlgorithm = "$('MESHED_POTATO_1').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"3\",\"4\":\"2\",\"5\":\"2\",\"6\":\"1\",\"7\":\"1\",\"8\":\"1\",\"9\":\"1\"}, null, null).value();$('MESHED_POTATO').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"3\",\"5\":\"2\",\"6\":\"2\",\"7\":\"1\"}, null, null).value();$('Stroke').map({\"1\":\"2\",\"2\":\"4\",\"9\":\"9\"}, null, null).value();";
		String actual = categoryAlgorithmGenerator.generate(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData1, sourceAttributeMetaData, sourceAttributeMetaData2),
				targetEntityMetaData, sourceEntityMetaData);
		Assert.assertEquals(actual, expectedAlgorithm);
	}

	@Test
	public void testCreateAlgorithmNullCheck()
	{
		String actual = "var SUM_WEIGHT;\nif($('MESHED_POTATO_1').isNull().value() && $('MESHED_POTATO').isNull().value() && $('Stroke').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}";
		String createAlgorithmNullCheck = categoryAlgorithmGenerator.createAlgorithmNullCheckIfStatement(
				Arrays.asList(sourceAttributeMetaData1, sourceAttributeMetaData, sourceAttributeMetaData2));
		Assert.assertEquals(createAlgorithmNullCheck, actual);
	}

	@Test
	void testCreateAlgorithmElseBlock()
	{
		String actual = "else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		String createAlgorithmElseBlock = categoryAlgorithmGenerator.createAlgorithmElseBlock(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData1, sourceAttributeMetaData, sourceAttributeMetaData2));
		Assert.assertEquals(createAlgorithmElseBlock, actual);
	}
}
