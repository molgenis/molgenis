package org.molgenis.semanticmapper.repository.impl;

import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.config.MappingConfig;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetaData;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { AttributeMappingRepositoryImplTest.Config.class, MappingConfig.class,
		OntologyConfig.class })
public class AttributeMappingRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private AttributeMappingMetaData attrMappingMeta;

	@Autowired
	private AttributeMappingRepositoryImpl attributeMappingRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Test
	public void testGetAttributeMappings()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = newArrayList();

		List<AttributeMapping> attributeMappings = newArrayList();
		attributeMappings.add(
				new AttributeMapping("attributeMappingID", "targetAttribute", targetAttribute, "algorithm",
						sourceAttributes));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGET_ATTRIBUTE, "targetAttribute");
		attributeMappingEntity.set(SOURCE_ATTRIBUTES, "sourceAttributes");
		attributeMappingEntity.set(ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		EntityType targetEntityType = entityTypeFactory.create("target");
		targetEntityType.addAttribute(targetAttribute);

		assertEquals(attributeMappingRepository.getAttributeMappings(attributeMappingEntities, sourceEntityType,
				targetEntityType), attributeMappings);
	}

	@Test
	public void testUpdate()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = newArrayList();

		targetAttribute.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping("attributeMappingID", "targetAttribute", targetAttribute, "algorithm",
						sourceAttributes, CURATED.toString()));

		List<Entity> result = newArrayList();
		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGET_ATTRIBUTE, targetAttribute.getName());
		attributeMappingEntity.set(SOURCE_ATTRIBUTES, "");
		attributeMappingEntity.set(ALGORITHM, "algorithm");
		attributeMappingEntity.set(ALGORITHM_STATE, CURATED.toString());

		result.add(attributeMappingEntity);

		List<Entity> expectedAttrMappings = attributeMappingRepository.upsert(attributeMappings);
		assertEquals(expectedAttrMappings.size(), result.size());
		for (int i = 0; i < expectedAttrMappings.size(); ++i)
		{
			Assert.assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
		}
	}

	@Test
	public void testInsert()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = newArrayList();
		targetAttribute.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping(null, "targetAttribute", targetAttribute, "algorithm", sourceAttributes,
						CURATED.toString()));

		Mockito.when(idGenerator.generateId()).thenReturn("attributeMappingID");

		List<Entity> result = newArrayList();
		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGET_ATTRIBUTE, targetAttribute.getName());
		attributeMappingEntity.set(SOURCE_ATTRIBUTES, "");
		attributeMappingEntity.set(ALGORITHM, "algorithm");
		attributeMappingEntity.set(ALGORITHM_STATE, CURATED.toString());

		result.add(attributeMappingEntity);

		List<Entity> expectedAttrMappings = attributeMappingRepository.upsert(attributeMappings);
		assertEquals(expectedAttrMappings.size(), result.size());
		for (int i = 0; i < expectedAttrMappings.size(); ++i)
		{
			Assert.assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
		}
	}

	@Test
	public void testRetrieveAttributesFromAlgorithm()
	{
		String algorithm = "$('attribute_1').value()$('attribute_2').value()";

		Attribute attr1 = attrMetaFactory.create().setName("attribute_1");
		Attribute attr2 = attrMetaFactory.create().setName("attribute_2");

		EntityType sourceEntityType = entityTypeFactory.create("source");
		sourceEntityType.addAttribute(attr1);
		sourceEntityType.addAttribute(attr2);

		List<Attribute> sourceAttributes = newArrayList();
		sourceAttributes.add(attr1);
		sourceAttributes.add(attr2);

		assertEquals(attributeMappingRepository.retrieveAttributesFromAlgorithm(algorithm, sourceEntityType),
				sourceAttributes);
	}

	@Test
	public void testRetrieveAttributesFromAlgorithmWithDotNotation()
	{
		String algorithm = "$('person.name').value()";

		Attribute id = attrMetaFactory.create().setName("id");
		Attribute name = attrMetaFactory.create().setName("name");
		EntityType referenceEntityType = entityTypeFactory.create("reference");
		referenceEntityType.addAttribute(id);
		referenceEntityType.addAttribute(name);

		Attribute person = attrMetaFactory.create().setName("person").setDataType(XREF).setRefEntity(referenceEntityType);
		EntityType sourceEntityType = entityTypeFactory.create("source");
		sourceEntityType.addAttribute(person);

		List<Attribute> expectedSourceAttributes = newArrayList();
		expectedSourceAttributes.add(person);

		assertEquals(attributeMappingRepository.retrieveAttributesFromAlgorithm(algorithm, sourceEntityType),
				expectedSourceAttributes);
	}

	@Configuration
	@Import(MapperTestConfig.class)
	public static class Config
	{
		@Autowired
		private AttributeMappingMetaData attrMappingMeta;

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
		}

		@Bean
		AttributeMappingRepositoryImpl attributeMappingRepository()
		{
			return new AttributeMappingRepositoryImpl(dataService(), attrMappingMeta);
		}

		@Bean
		UserService userService()
		{
			return mock(UserService.class);
		}

		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		JsMagmaScriptEvaluator jsMagmaScriptEvaluator()
		{
			return mock(JsMagmaScriptEvaluator.class);
		}

		@Bean
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		DefaultPackage defaultPackage()
		{
			return mock(DefaultPackage.class);
		}

		@Bean
		SystemPackageRegistry systemPackageRegistry()
		{
			return mock(SystemPackageRegistry.class);
		}
	}
}
