package org.molgenis.data.mapper.repository.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.ALGORITHM;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.IDENTIFIER;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.TARGETATTRIBUTEMETADATA;
import static org.molgenis.data.mapper.repository.impl.AttributeMappingRepositoryImpl.META_DATA;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ AttributeMappingRepositoryImplTest.Config.class, MappingConfig.class })
public class AttributeMappingRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private AttributeMappingRepositoryImpl attributeMappingRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Test
	public void testGetAttributeMappings()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");

		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
		attributeMappings.add(new AttributeMapping("attributeMappingID", targetAttributeMetaData, "algorithm"));

		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
		attributeMappingEntities.add(attributeMappingEntity);

		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);

		assertEquals(attributeMappingRepository.getAttributeMappings(attributeMappingEntities, sourceEntityMetaData,
				targetEntityMetaData), attributeMappings);
	}

	@Test
	public void testUpdate()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		targetAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);

		Collection<AttributeMapping> attributeMappings = Arrays.asList(new AttributeMapping("attributeMappingID",
				targetAttributeMetaData, "algorithm"));

		List<Entity> result = new ArrayList<Entity>();
		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
		attributeMappingEntity.set(ALGORITHM, "algorithm");

		result.add(attributeMappingEntity);

		assertEquals(attributeMappingRepository.upsert(attributeMappings), result);

		verify(dataService).update(AttributeMappingRepositoryImpl.META_DATA.getName(), attributeMappingEntity);
	}

	@Test
	public void testInsert()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		targetAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);

		Collection<AttributeMapping> attributeMappings = Arrays.asList(new AttributeMapping(null,
				targetAttributeMetaData, "algorithm"));

		Mockito.when(idGenerator.generateId()).thenReturn("attributeMappingID");

		List<Entity> result = new ArrayList<Entity>();
		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
		attributeMappingEntity.set(ALGORITHM, "algorithm");

		result.add(attributeMappingEntity);

		assertEquals(attributeMappingRepository.upsert(attributeMappings), result);

		verify(dataService).add(META_DATA.getName(), attributeMappingEntity);
	}

	@Configuration
	public static class Config
	{
		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		AttributeMappingRepositoryImpl attributeMappingRepository()
		{
			return new AttributeMappingRepositoryImpl(dataService());
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
	}
}
