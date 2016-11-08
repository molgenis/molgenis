package org.molgenis.data.mapper.repository.impl;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.molgenis.AttributeType.STRING;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
		attributeMappings
				.add(new AttributeMapping("attributeMappingID", targetAttribute, "algorithm", sourceAttributes));

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

		assertEquals(attributeMappingRepository
				.getAttributeMappings(attributeMappingEntities, sourceEntityType, targetEntityType), attributeMappings);
	}

	@Test
	public void testUpdate()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = newArrayList();

		targetAttribute.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping("attributeMappingID", targetAttribute, "algorithm", sourceAttributes,
						CURATED.toString()));

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
			assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
		}
	}

	@Test
	public void testInsert()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = newArrayList();
		targetAttribute.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping(null, targetAttribute, "algorithm", sourceAttributes, CURATED.toString()));

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
			assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
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

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth" })
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
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}
	}
}
