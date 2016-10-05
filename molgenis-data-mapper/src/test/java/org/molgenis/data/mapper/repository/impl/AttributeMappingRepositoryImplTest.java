package org.molgenis.data.mapper.repository.impl;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
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
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { AttributeMappingRepositoryImplTest.Config.class, MappingConfig.class,
		OntologyConfig.class })
public class AttributeMappingRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private AttributeMappingMetaData attrMappingMeta;

	@Autowired
	private AttributeMappingRepositoryImpl attributeMappingRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Test
	public void testGetAttributeMappings()
	{
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = newArrayList();

		List<AttributeMapping> attributeMappings = newArrayList();
		attributeMappings.add(new AttributeMapping("attributeMappingID", targetAttributeMetaData, "algorithm",
				sourceAttributeMetaDatas));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, "sourceAttributes");
		attributeMappingEntity.set(ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);

		assertEquals(attributeMappingRepository
						.getAttributeMappings(attributeMappingEntities, sourceEntityMetaData, targetEntityMetaData),
				attributeMappings);
	}

	@Test
	public void testUpdate()
	{
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = newArrayList();

		targetAttributeMetaData.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping("attributeMappingID", targetAttributeMetaData, "algorithm",
						sourceAttributeMetaDatas, CURATED.toString()));

		List<Entity> result = newArrayList();
		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, "");
		attributeMappingEntity.set(ALGORITHM, "algorithm");
		attributeMappingEntity.set(ALGORITHMSTATE, CURATED.toString());

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
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = newArrayList();
		targetAttributeMetaData.setDataType(STRING);

		Collection<AttributeMapping> attributeMappings = singletonList(
				new AttributeMapping(null, targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas,
						CURATED.toString()));

		Mockito.when(idGenerator.generateId()).thenReturn("attributeMappingID");

		List<Entity> result = newArrayList();
		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, "");
		attributeMappingEntity.set(ALGORITHM, "algorithm");
		attributeMappingEntity.set(ALGORITHMSTATE, CURATED.toString());

		result.add(attributeMappingEntity);

		List<Entity> expectedAttrMappings = attributeMappingRepository.upsert(attributeMappings);
		assertEquals(expectedAttrMappings.size(), result.size());
		for (int i = 0; i < expectedAttrMappings.size(); ++i)
		{
			assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
		}
	}

	@Test
	public void testRetrieveAttributeMetaDatasFromAlgorithm()
	{
		String algorithm = "$('attribute_1').value()$('attribute_2').value()";

		AttributeMetaData attr1 = attrMetaFactory.create().setName("attribute_1");
		AttributeMetaData attr2 = attrMetaFactory.create().setName("attribute_2");

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		sourceEntityMetaData.addAttribute(attr1);
		sourceEntityMetaData.addAttribute(attr2);

		List<AttributeMetaData> sourceAttributeMetaDatas = newArrayList();
		sourceAttributeMetaDatas.add(attr1);
		sourceAttributeMetaDatas.add(attr2);

		assertEquals(
				attributeMappingRepository.retrieveAttributeMetaDatasFromAlgorithm(algorithm, sourceEntityMetaData),
				sourceAttributeMetaDatas);
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
		MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
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
