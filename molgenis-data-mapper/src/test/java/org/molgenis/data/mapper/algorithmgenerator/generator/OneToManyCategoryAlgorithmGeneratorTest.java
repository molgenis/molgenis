package org.molgenis.data.mapper.algorithmgenerator.generator;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class OneToManyCategoryAlgorithmGeneratorTest
{
	OneToManyCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	DefaultAttributeMetaData targetAttributeMetaData;

	DefaultAttributeMetaData sourceAttributeMetaData;

	DefaultAttributeMetaData sourceAttributeMetaData1;

	DefaultAttributeMetaData sourceAttributeMetaData2;

	DefaultEntityMetaData targetEntityMetaData;

	DefaultEntityMetaData sourceEntityMetaData;

	@BeforeMethod
	public void init()
	{
		DataService dataService = Mockito.mock(DataService.class);
		categoryAlgorithmGenerator = new OneToManyCategoryAlgorithmGenerator(dataService);

		DefaultEntityMetaData targetRefEntityMetaData = new DefaultEntityMetaData("POTATO_REF");
		DefaultAttributeMetaData targetCodeAttributeMetaData = new DefaultAttributeMetaData("code", FieldTypeEnum.INT);
		targetCodeAttributeMetaData.setIdAttribute(true);
		DefaultAttributeMetaData targetLabelAttributeMetaData = new DefaultAttributeMetaData("label",
				FieldTypeEnum.STRING);
		targetLabelAttributeMetaData.setLabelAttribute(true);
		targetRefEntityMetaData.addAttributeMetaData(targetCodeAttributeMetaData);
		targetRefEntityMetaData.addAttributeMetaData(targetLabelAttributeMetaData);

		targetAttributeMetaData = new DefaultAttributeMetaData("Current Consumption Frequency of Potatoes",
				FieldTypeEnum.CATEGORICAL);
		targetAttributeMetaData.setRefEntity(targetRefEntityMetaData);

		MapEntity targetEntity1 = new MapEntity(ImmutableMap.of("code", 1, "label", "Almost daily + daily"));
		MapEntity targetEntity2 = new MapEntity(ImmutableMap.of("code", 2, "label", "Several times a week"));
		MapEntity targetEntity3 = new MapEntity(ImmutableMap.of("code", 3, "label", "About once a week"));
		MapEntity targetEntity4 = new MapEntity(ImmutableMap.of("code", 4, "label", "Never + fewer than once a week"));
		MapEntity targetEntity5 = new MapEntity(ImmutableMap.of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(targetRefEntityMetaData.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5);
			}
		});

		targetEntityMetaData = new DefaultEntityMetaData("target");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);

		DefaultEntityMetaData sourceRefEntityMetaData = createEntityMetaData("LifeLines_POTATO_REF");

		sourceAttributeMetaData = new DefaultAttributeMetaData("MESHED_POTATO", FieldTypeEnum.CATEGORICAL);
		sourceAttributeMetaData.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData.setRefEntity(sourceRefEntityMetaData);

		MapEntity sourceEntity1 = new MapEntity(ImmutableMap.of("code", 1, "label", "Not this month"));
		MapEntity sourceEntity2 = new MapEntity(ImmutableMap.of("code", 2, "label", "1 day per month"));
		MapEntity sourceEntity3 = new MapEntity(ImmutableMap.of("code", 3, "label", "2-3 days per month"));
		MapEntity sourceEntity4 = new MapEntity(ImmutableMap.of("code", 4, "label", "1 day per week"));
		MapEntity sourceEntity5 = new MapEntity(ImmutableMap.of("code", 5, "label", "2-3 days per week"));
		MapEntity sourceEntity6 = new MapEntity(ImmutableMap.of("code", 6, "label", "4-5 days per week"));
		MapEntity sourceEntity7 = new MapEntity(ImmutableMap.of("code", 7, "label", "6-7 days per week"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5,
						sourceEntity6, sourceEntity7);
			}
		});

		DefaultEntityMetaData sourceRefEntityMetaData1 = createEntityMetaData("Mitchelstown_POTATO_REF");

		sourceAttributeMetaData1 = new DefaultAttributeMetaData("MESHED_POTATO_1", FieldTypeEnum.CATEGORICAL);
		sourceAttributeMetaData1.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData1.setRefEntity(sourceRefEntityMetaData1);

		MapEntity sourceEntity8 = new MapEntity(ImmutableMap.of("code", 1, "label", "never/less than 1 per month"));
		MapEntity sourceEntity9 = new MapEntity(ImmutableMap.of("code", 2, "label", "1-3 per month"));
		MapEntity sourceEntity10 = new MapEntity(ImmutableMap.of("code", 3, "label", "once a week"));
		MapEntity sourceEntity11 = new MapEntity(ImmutableMap.of("code", 4, "label", "2-4 per week"));
		MapEntity sourceEntity12 = new MapEntity(ImmutableMap.of("code", 5, "label", "5-6 per week"));
		MapEntity sourceEntity13 = new MapEntity(ImmutableMap.of("code", 6, "label", "once a day"));
		MapEntity sourceEntity14 = new MapEntity(ImmutableMap.of("code", 7, "label", "2-3 per day"));
		MapEntity sourceEntity15 = new MapEntity(ImmutableMap.of("code", 8, "label", "4-5 per day"));
		MapEntity sourceEntity16 = new MapEntity(ImmutableMap.of("code", 9, "label", "6+ per day"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData1.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(sourceEntity8, sourceEntity9, sourceEntity10, sourceEntity11, sourceEntity12,
						sourceEntity13, sourceEntity14, sourceEntity15, sourceEntity16);
			}
		});

		DefaultEntityMetaData sourceRefEntityMetaData2 = createEntityMetaData("Mitchelstown_Stroke_REF");

		sourceAttributeMetaData2 = new DefaultAttributeMetaData("Stroke", FieldTypeEnum.CATEGORICAL);
		sourceAttributeMetaData2.setLabel("History of stroke");
		sourceAttributeMetaData2.setRefEntity(sourceRefEntityMetaData2);

		MapEntity sourceEntity17 = new MapEntity(ImmutableMap.of("code", 1, "label", "yes"));
		MapEntity sourceEntity18 = new MapEntity(ImmutableMap.of("code", 2, "label", "no"));
		MapEntity sourceEntity19 = new MapEntity(ImmutableMap.of("code", 9, "label", "missing"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData2.getName())).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(sourceEntity17, sourceEntity18, sourceEntity19);
			}
		});

		sourceEntityMetaData = new DefaultEntityMetaData("source");
		sourceEntityMetaData.addAllAttributeMetaData(
				Lists.newArrayList(sourceAttributeMetaData, sourceAttributeMetaData1, sourceAttributeMetaData2));
	}

	private DefaultEntityMetaData createEntityMetaData(String entityName)
	{
		DefaultEntityMetaData sourceRefEntityMetaData = new DefaultEntityMetaData(entityName);

		DefaultAttributeMetaData sourceCodeAttributeMetaData = new DefaultAttributeMetaData("code", FieldTypeEnum.INT);
		sourceCodeAttributeMetaData.setIdAttribute(true);
		DefaultAttributeMetaData sourceLabelAttributeMetaData = new DefaultAttributeMetaData("label",
				FieldTypeEnum.STRING);
		sourceLabelAttributeMetaData.setLabelAttribute(true);
		sourceRefEntityMetaData.addAttributeMetaData(sourceCodeAttributeMetaData);
		sourceRefEntityMetaData.addAttributeMetaData(sourceLabelAttributeMetaData);
		return sourceRefEntityMetaData;
	}

	@Test
	public void testIsSuitable()
	{
		Assert.assertTrue(categoryAlgorithmGenerator.isSuitable(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1)));
	}

	@Test
	public void testGenerate()
	{
		String expected = "var SUM_WEIGHT;\nif($('MESHED_POTATO').isNull().value() && $('MESHED_POTATO_1').isNull().value()){\n\tSUM_WEIGHT = new newValue();\n\tSUM_WEIGHT.value();\n}else{\n\tSUM_WEIGHT = new newValue(0);\n\tSUM_WEIGHT.plus($('MESHED_POTATO').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());\n\tSUM_WEIGHT.plus($('MESHED_POTATO_1').map({\"1\":0.1,\"2\":0.5,\"3\":1,\"4\":3,\"5\":5.5,\"6\":7,\"7\":17.5,\"8\":31.5,\"9\":42}, null, null).value());\n\tSUM_WEIGHT.group([0,1,3,6,7]).map({\"-0\":\"4\",\"0-1\":\"4\",\"1-3\":\"3\",\"3-6\":\"2\",\"6-7\":\"1\",\"7+\":\"1\"}, null, null).value();\n}";
		String generateMultipleAttributes = categoryAlgorithmGenerator.generate(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1), targetEntityMetaData,
				sourceEntityMetaData);
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
		Assert.assertEquals(categoryAlgorithmGenerator.generate(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1), targetEntityMetaData,
				sourceEntityMetaData), expectedAlgorithm);
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
