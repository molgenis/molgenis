package org.molgenis.semanticmapper.algorithmgenerator.generator;

import com.google.common.collect.Lists;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
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

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

public class OneToOneCategoryAlgorithmGeneratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private AbstractCategoryAlgorithmGenerator categoryAlgorithmGenerator;

	private Attribute targetAttribute;

	private Attribute sourceAttribute;

	private EntityType targetEntityType;

	private EntityType sourceEntityType;

	private DataService dataService;

	public OneToOneCategoryAlgorithmGeneratorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void init()
	{
		dataService = Mockito.mock(DataService.class);

		categoryAlgorithmGenerator = new OneToOneCategoryAlgorithmGenerator(dataService);

		EntityType targetRefEntityType = createCategoricalRefEntityType("POTATO_REF");
		Entity targetEntity1 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "Almost daily + daily"));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "Several times a week"));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "About once a week"));
		Entity targetEntity4 = new DynamicEntity(targetRefEntityType,
				of("code", 4, "label", "Never + fewer than once a week"));
		Entity targetEntity5 = new DynamicEntity(targetRefEntityType, of("code", 9, "label", "missing"));

		targetAttribute = attrMetaFactory.create()
										 .setName("Current Consumption Frequency of Potatoes")
										 .setDataType(CATEGORICAL);
		targetAttribute.setRefEntity(targetRefEntityType);

		Mockito.when(dataService.findAll(targetRefEntityType.getId()))
			   .thenReturn(Stream.of(targetEntity1, targetEntity2, targetEntity3, targetEntity4, targetEntity5));

		targetEntityType = entityTypeFactory.create("target");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceRefEntityType = createCategoricalRefEntityType("LifeLines_POTATO_REF");
		Entity sourceEntity1 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "Not this month"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "1 day per month"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "2-3 days per month"));
		Entity sourceEntity4 = new DynamicEntity(targetRefEntityType, of("code", 4, "label", "1 day per week"));
		Entity sourceEntity5 = new DynamicEntity(targetRefEntityType, of("code", 5, "label", "2-3 days per week"));
		Entity sourceEntity6 = new DynamicEntity(targetRefEntityType, of("code", 6, "label", "4-5 days per week"));
		Entity sourceEntity7 = new DynamicEntity(targetRefEntityType, of("code", 7, "label", "6-7 days per week"));
		Entity sourceEntity8 = new DynamicEntity(targetRefEntityType, of("code", 8, "label", "9 days per week"));

		sourceAttribute = attrMetaFactory.create().setName("MESHED_POTATO").setDataType(CATEGORICAL);
		sourceAttribute.setLabel(
				"How often did you eat boiled or mashed potatoes (also in stew) in the past month? Baked potatoes are asked later");
		sourceAttribute.setRefEntity(sourceRefEntityType);

		Mockito.when(dataService.findAll(sourceRefEntityType.getId()))
			   .thenReturn(Stream.of(sourceEntity1, sourceEntity2, sourceEntity3, sourceEntity4, sourceEntity5,
					   sourceEntity6, sourceEntity7, sourceEntity8));

		sourceEntityType = entityTypeFactory.create("source");
		sourceEntityType.addAttributes(Lists.newArrayList(sourceAttribute));
	}

	@Test
	public void testIsSuitable()
	{
		Assert.assertTrue(categoryAlgorithmGenerator.isSuitable(targetAttribute, singletonList(sourceAttribute)));
	}

	@Test
	public void testGenerate()
	{
		String generatedAlgorithm = categoryAlgorithmGenerator.generate(targetAttribute, singletonList(sourceAttribute),
				targetEntityType, sourceEntityType);
		String expectedAlgorithm = "$('MESHED_POTATO').map({\"1\":\"4\",\"2\":\"4\",\"3\":\"4\",\"4\":\"3\",\"5\":\"2\",\"6\":\"2\",\"7\":\"1\",\"8\":\"1\"}, null, null).value();";

		Assert.assertEquals(generatedAlgorithm, expectedAlgorithm);
	}

	@Test
	public void testGenerateRules()
	{
		EntityType targetRefEntityType = createCategoricalRefEntityType("HOP_HYPERTENSION");
		Entity targetEntity1 = new DynamicEntity(targetRefEntityType,
				of("code", 0, "label", "Never had high blood pressure "));
		Entity targetEntity2 = new DynamicEntity(targetRefEntityType,
				of("code", 1, "label", "Ever had high blood pressure "));
		Entity targetEntity3 = new DynamicEntity(targetRefEntityType, of("code", 9, "label", "Missing"));
		Mockito.when(dataService.findAll(targetRefEntityType.getId()))
			   .thenReturn(Stream.of(targetEntity1, targetEntity2, targetEntity3));
		targetAttribute = attrMetaFactory.create().setName("History of Hypertension").setDataType(CATEGORICAL);
		targetAttribute.setRefEntity(targetRefEntityType);

		EntityType sourceRefEntityType = createCategoricalRefEntityType("High_blood_pressure_ref");
		Entity sourceEntity1 = new DynamicEntity(targetRefEntityType, of("code", 1, "label", "yes"));
		Entity sourceEntity2 = new DynamicEntity(targetRefEntityType, of("code", 2, "label", "no"));
		Entity sourceEntity3 = new DynamicEntity(targetRefEntityType, of("code", 3, "label", "I do not know"));
		Mockito.when(dataService.findAll(sourceRefEntityType.getId()))
			   .thenReturn(Stream.of(sourceEntity1, sourceEntity2, sourceEntity3));

		sourceAttribute = attrMetaFactory.create().setName("High_blood_pressure").setDataType(CATEGORICAL);
		sourceAttribute.setRefEntity(sourceRefEntityType);

		String generatedAlgorithm = categoryAlgorithmGenerator.generate(targetAttribute, singletonList(sourceAttribute),
				targetEntityType, sourceEntityType);

		String expectedAlgorithm = "$('High_blood_pressure').map({\"1\":\"1\",\"2\":\"0\",\"3\":\"9\"}, null, null).value();";

		Assert.assertEquals(generatedAlgorithm, expectedAlgorithm);
	}

	private EntityType createCategoricalRefEntityType(String entityTypeId)
	{
		EntityType targetRefEntityType = entityTypeFactory.create(entityTypeId);
		Attribute targetCodeAttribute = attrMetaFactory.create().setName("code").setDataType(INT);
		Attribute targetLabelAttribute = attrMetaFactory.create().setName("label");
		targetRefEntityType.addAttribute(targetCodeAttribute, ROLE_ID);
		targetRefEntityType.addAttribute(targetLabelAttribute, ROLE_LABEL);
		return targetRefEntityType;
	}
}
