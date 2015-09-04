package org.molgenis.data.mapper.algorithmgenerator.categorygenerator;

import java.util.Arrays;

import org.mockito.Mockito;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class OneToManyCategoryAlgorithmGeneratorTest
{

	OneToManyCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	DefaultAttributeMetaData targetAttributeMetaData;

	DefaultAttributeMetaData sourceAttributeMetaData;

	DefaultAttributeMetaData sourceAttributeMetaData1;

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

		Mockito.when(dataService.findAll(targetRefEntityMetaData.getName())).thenReturn(
				Arrays.asList(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5));

		DefaultEntityMetaData sourceRefEntityMetaData = new DefaultEntityMetaData("LifeLines_POTATO_REF");

		DefaultAttributeMetaData sourceCodeAttributeMetaData = new DefaultAttributeMetaData("code", FieldTypeEnum.INT);
		sourceCodeAttributeMetaData.setIdAttribute(true);
		DefaultAttributeMetaData sourceLabelAttributeMetaData = new DefaultAttributeMetaData("label",
				FieldTypeEnum.STRING);
		sourceLabelAttributeMetaData.setLabelAttribute(true);
		sourceRefEntityMetaData.addAttributeMetaData(sourceCodeAttributeMetaData);
		sourceRefEntityMetaData.addAttributeMetaData(sourceLabelAttributeMetaData);

		sourceAttributeMetaData = new DefaultAttributeMetaData("MESHED_POTATO", FieldTypeEnum.CATEGORICAL);
		sourceAttributeMetaData
				.setLabel("How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData.setRefEntity(sourceRefEntityMetaData);

		MapEntity sourceEntity1 = new MapEntity(ImmutableMap.of("code", 1, "label", "Not this month"));
		MapEntity sourceEntity2 = new MapEntity(ImmutableMap.of("code", 2, "label", "1 day per month"));
		MapEntity sourceEntity3 = new MapEntity(ImmutableMap.of("code", 3, "label", "2-3 days per month"));
		MapEntity sourceEntity4 = new MapEntity(ImmutableMap.of("code", 4, "label", "1 day per week"));
		MapEntity sourceEntity5 = new MapEntity(ImmutableMap.of("code", 5, "label", "2-3 days per week"));
		MapEntity sourceEntity6 = new MapEntity(ImmutableMap.of("code", 6, "label", "4-5 days per week"));
		MapEntity sourceEntity7 = new MapEntity(ImmutableMap.of("code", 7, "label", "6-7 days per week"));

		Mockito.when(dataService.findAll(sourceRefEntityMetaData.getName())).thenReturn(
				Arrays.asList(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5, sourceEntity6,
						sourceEntity7));

		DefaultEntityMetaData sourceRefEntityMetaData1 = new DefaultEntityMetaData("Mitchelstown_POTATO_REF");

		DefaultAttributeMetaData sourceCodeAttributeMetaData1 = new DefaultAttributeMetaData("code", FieldTypeEnum.INT);
		sourceCodeAttributeMetaData1.setIdAttribute(true);
		DefaultAttributeMetaData sourceLabelAttributeMetaData1 = new DefaultAttributeMetaData("label",
				FieldTypeEnum.STRING);
		sourceLabelAttributeMetaData1.setLabelAttribute(true);
		sourceRefEntityMetaData1.addAttributeMetaData(sourceCodeAttributeMetaData1);
		sourceRefEntityMetaData1.addAttributeMetaData(sourceLabelAttributeMetaData1);

		sourceAttributeMetaData1 = new DefaultAttributeMetaData("MESHED_POTATO_1", FieldTypeEnum.CATEGORICAL);
		sourceAttributeMetaData1
				.setLabel("How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
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

		Mockito.when(dataService.findAll(sourceRefEntityMetaData1.getName())).thenReturn(
				Arrays.asList(sourceEntity8, sourceEntity9, sourceEntity10, sourceEntity11, sourceEntity12,
						sourceEntity13, sourceEntity14, sourceEntity15, sourceEntity16));
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
		String generateMultipleAttributes = categoryAlgorithmGenerator.generate(targetAttributeMetaData,
				Arrays.asList(sourceAttributeMetaData, sourceAttributeMetaData1));
		System.out.println(generateMultipleAttributes);
	}

	@Test
	public void testGenerateWeightedMap()
	{
		String expected = "$('MESHED_POTATO').map({\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value();";
		String actual = categoryAlgorithmGenerator.generateWeightedMap(sourceAttributeMetaData);

		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testGenerateWeightedMapForTarget()
	{
		System.out.println(categoryAlgorithmGenerator.groupCategoryValues(targetAttributeMetaData));
	}
}
