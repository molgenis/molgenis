package org.molgenis.data.mapper.algorithmgenerator.generator;

import com.google.common.collect.Lists;
import org.mockito.Mockito;
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

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static org.molgenis.AttributeType.CATEGORICAL;
import static org.molgenis.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;

public class OneToOneCategoryAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	private AbstractCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	private AttributeMetaData targetAttributeMetaData;

	private AttributeMetaData sourceAttributeMetaData;

	private EntityMetaData targetEntityMetaData;

	private EntityMetaData sourceEntityMetaData;

	private DataService dataService;

	@BeforeMethod
	public void init()
	{
		dataService = Mockito.mock(DataService.class);

		categoryAlgorithmGenerator = new OneToOneCategoryAlgorithmGenerator(dataService);

		EntityMetaData targetRefEntityMeta = createCategoricalRefEntityMetaData("POTATO_REF");
		Entity targetEntity1 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "Almost daily + daily"));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "Several times a week"));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "About once a week"));
		Entity targetEntity4 = new DynamicEntity(targetRefEntityMeta,
				of("code", 4, "label", "Never + fewer than once a week"));
		Entity targetEntity5 = new DynamicEntity(targetRefEntityMeta, of("code", 9, "label", "missing"));

		targetAttributeMetaData = attrMetaFactory.create().setName("Current Consumption Frequency of Potatoes")
				.setDataType(CATEGORICAL);
		targetAttributeMetaData.setRefEntity(targetRefEntityMeta);

		Mockito.when(dataService.findAll(targetRefEntityMeta.getName()))
				.thenReturn(Stream.of(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5));

		targetEntityMetaData = entityMetaFactory.create("target");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);

		EntityMetaData sourceRefEntityMetaData = createCategoricalRefEntityMetaData("LifeLines_POTATO_REF");
		Entity sourceEntity1 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "Not this month"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "1 day per month"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "2-3 days per month"));
		Entity sourceEntity4 = new DynamicEntity(targetRefEntityMeta, of("code", 4, "label", "1 day per week"));
		Entity sourceEntity5 = new DynamicEntity(targetRefEntityMeta, of("code", 5, "label", "2-3 days per week"));
		Entity sourceEntity6 = new DynamicEntity(targetRefEntityMeta, of("code", 6, "label", "4-5 days per week"));
		Entity sourceEntity7 = new DynamicEntity(targetRefEntityMeta, of("code", 7, "label", "6-7 days per week"));
		Entity sourceEntity8 = new DynamicEntity(targetRefEntityMeta, of("code", 8, "label", "9 days per week"));

		sourceAttributeMetaData = attrMetaFactory.create().setName("MESHED_POTATO").setDataType(CATEGORICAL);
		sourceAttributeMetaData.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttributeMetaData.setRefEntity(sourceRefEntityMetaData);

		Mockito.when(dataService.findAll(sourceRefEntityMetaData.getName())).thenReturn(
				Stream.of(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5, sourceEntity6,
						sourceEntity7, sourceEntity8));

		sourceEntityMetaData = entityMetaFactory.create("source");
		sourceEntityMetaData.addAttributes(Lists.newArrayList(sourceAttributeMetaData));
	}

	@Test
	public void testIsSuitable()
	{
		Assert.assertTrue(
				categoryAlgorithmGenerator.isSuitable(targetAttributeMetaData, singletonList(sourceAttributeMetaData)));
	}

	@Test
	public void testGenerate()
	{
		String generatedAlgorithm = categoryAlgorithmGenerator
				.generate(targetAttributeMetaData, singletonList(sourceAttributeMetaData), targetEntityMetaData,
						sourceEntityMetaData);
		String expectedAlgorithm = "$('MESHED_POTATO').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"3\",\"5\":\"2\",\"6\":\"2\",\"7\":\"1\",\"8\":\"1\"}, null, null).value();";

		Assert.assertEquals(generatedAlgorithm, expectedAlgorithm);
	}

	@Test
	public void testGenerateRules()
	{
		EntityMetaData targetRefEntityMeta = createCategoricalRefEntityMetaData("HOP_HYPERTENSION");
		Entity targetEntity1 = new DynamicEntity(targetRefEntityMeta,
				of("code", 0, "label", "Never had high blood pressure "));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityMeta,
				of("code", 1, "label", "Ever had high blood pressure "));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 9, "label", "Missing"));
		Mockito.when(dataService.findAll(targetRefEntityMeta.getName()))
				.thenReturn(Stream.of(targetEntity1, targetEntity2, targetEntity3));
		targetAttributeMetaData = attrMetaFactory.create().setName("History of Hypertension").setDataType(CATEGORICAL);
		targetAttributeMetaData.setRefEntity(targetRefEntityMeta);

		EntityMetaData sourceRefEntityMetaData = createCategoricalRefEntityMetaData("High_blood_pressure_ref");
		Entity sourceEntity1 = new DynamicEntity(targetRefEntityMeta, of("code", 1, "label", "yes"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityMeta, of("code", 2, "label", "no"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityMeta, of("code", 3, "label", "I do not know"));
		Mockito.when(dataService.findAll(sourceRefEntityMetaData.getName()))
				.thenReturn(Stream.of(sourceEntity1, sourceEntity2, sourceEntity3));

		sourceAttributeMetaData = attrMetaFactory.create().setName("High_blood_pressure").setDataType(CATEGORICAL);
		sourceAttributeMetaData.setRefEntity(sourceRefEntityMetaData);

		String generatedAlgorithm = categoryAlgorithmGenerator
				.generate(targetAttributeMetaData, singletonList(sourceAttributeMetaData), targetEntityMetaData,
						sourceEntityMetaData);

		String expectedAlgorithm = "$('High_blood_pressure').map({\"1\":\"1\",\"2\":\"0\",\"3\":\"9\"}, null, null).value();";

		Assert.assertEquals(generatedAlgorithm, expectedAlgorithm);
	}

	private EntityMetaData createCategoricalRefEntityMetaData(String entityName)
	{
		EntityMetaData targetRefEntityMetaData = entityMetaFactory.create(entityName);
		AttributeMetaData targetCodeAttributeMetaData = attrMetaFactory.create().setName("code").setDataType(INT);
		AttributeMetaData targetLabelAttributeMetaData = attrMetaFactory.create().setName("label");
		targetRefEntityMetaData.addAttribute(targetCodeAttributeMetaData, ROLE_ID);
		targetRefEntityMetaData.addAttribute(targetLabelAttributeMetaData, ROLE_LABEL);
		return targetRefEntityMetaData;
	}
}
