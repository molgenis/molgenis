package org.molgenis.metadata.manager.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { MetadataManagerServiceTest.Config.class })
public class MetadataManagerServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetadataManagerService metadataManagerService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private PackageMapper packageMapper;

	@Autowired
	private EntityTypeMapper entityTypeMapper;

	@Autowired
	private AttributeMapper attributeMapper;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Test
	public void testGetEditorPackages()
	{
		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn("test");
		List<Package> packages = newArrayList(package_);
		when(metaDataService.getPackages()).thenReturn(packages);

		EditorPackageIdentifier editorPackage = getEditorPackageIdentifier();
		when(packageMapper.toEditorPackage(package_)).thenReturn(editorPackage);

		List<EditorPackageIdentifier> actual = metadataManagerService.getEditorPackages();
		List<EditorPackageIdentifier> expected = newArrayList(editorPackage);

		assertEquals(actual, expected);
	}

	@Test
	public void testGetEditorEntityType()
	{
		EntityType entityType = mock(EntityType.class);

		Repository<EntityType> repository = mock(Repository.class);
		when(repository.findOneById("id_1")).thenReturn(entityType);
		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);

		EditorEntityType editorEntityType = getEditorEntityType();
		when(entityTypeMapper.toEditorEntityType(entityType)).thenReturn(editorEntityType);

		EditorEntityTypeResponse actual = metadataManagerService.getEditorEntityType("id_1");
		EditorEntityTypeResponse expected = getEditorEntityTypeResponse();

		assertEquals(actual, expected);
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "Unknown EntityType \\[unknownId\\]")
	public void testGetNonExistingEditorEntityType()
	{
		Repository<EntityType> repository = mock(Repository.class);
		when(repository.findOneById("unknownId")).thenReturn(null);

		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);
		metadataManagerService.getEditorEntityType("unknownId");
	}

	@Test
	public void testCreateEditorEntityType()
	{
		when(entityTypeMapper.createEditorEntityType()).thenReturn(getEditorEntityType());

		EditorEntityTypeResponse actual = metadataManagerService.createEditorEntityType();
		EditorEntityTypeResponse expected = getEditorEntityTypeResponse();

		assertEquals(actual, expected);
	}

	@Test
	public void testUpsertEntityType()
	{
		EditorEntityType editorEntityType = getEditorEntityType();
		EntityType entityType = getEntityType();
		when(entityTypeMapper.toEntityType(editorEntityType)).thenReturn(entityType);

		metadataManagerService.upsertEntityType(editorEntityType);
		verify(metaDataService, times(1)).upsertEntityTypes(newArrayList(entityType));
	}

	@Test
	public void testCreateEditorAttribute()
	{
		when(attributeMapper.createEditorAttribute()).thenReturn(getEditorAttribute());

		EditorAttributeResponse actual = metadataManagerService.createEditorAttribute();
		EditorAttributeResponse expected = getEditorAttributeResponse();

		assertEquals(actual, expected);
	}

	private EditorPackageIdentifier getEditorPackageIdentifier()
	{
		return EditorPackageIdentifier.create("test", "test");
	}

	private EntityType getEntityType()
	{
		return entityTypeFactory.create("id_1");
	}

	private EditorEntityType getEditorEntityType()
	{
		return EditorEntityType.create("id_1", null, ImmutableMap.of(), null, ImmutableMap.of(), false, "backend", null,
				null, ImmutableList.of(), ImmutableList.of(), null, null, ImmutableList.of());
	}

	private EditorEntityTypeResponse getEditorEntityTypeResponse()
	{
		return EditorEntityTypeResponse.create(getEditorEntityType(),
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	private EditorAttribute getEditorAttribute()
	{
		return EditorAttribute.create("1", null, null, null, null, null, null, null, false, false, false, null,
				ImmutableMap.of(), null, ImmutableMap.of(), false, ImmutableList.of(), null, null, false, false,
				ImmutableList.of(), null, null, null, 1);
	}

	private EditorAttributeResponse getEditorAttributeResponse()
	{
		return EditorAttributeResponse.create(getEditorAttribute(),
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public PackageMapper packageMapper()
		{
			return mock(PackageMapper.class);
		}

		@Bean
		public EntityTypeMapper entityTypeMapper()
		{
			return mock(EntityTypeMapper.class);
		}

		@Bean
		public AttributeMapper attributeMapper()
		{
			return mock(AttributeMapper.class);
		}

		@Bean
		public EntityTypeFactory entityTypeFactory()
		{
			return mock(EntityTypeFactory.class);
		}

		@Bean
		public MetadataManagerService metadataManagerService()
		{
			return new MetadataManagerServiceImpl(metaDataService(), packageMapper(), entityTypeMapper(),
					attributeMapper());
		}
	}
}