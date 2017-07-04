package org.molgenis.metadata.manager.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.metadata.manager.controller.MetadataManagerFormEditingController;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.EditorAttribute;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration(classes = { MetadataManagerFormEditingControllerTest.Config.class, GsonConfig.class })
public class MetadataManagerFormEditingControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private PackageMapper packageMapper;

	@Autowired
	private EntityTypeMapper entityTypeMapper;

	@Autowired
	private AttributeMapper attributeMapper;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");
		MetadataManagerFormEditingController metadataManagerFormEditingController = new MetadataManagerFormEditingController(metaDataService, packageMapper,
				entityTypeMapper, attributeMapper);

		mockMvc = MockMvcBuilders.standaloneSetup(metadataManagerFormEditingController)
				.setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter).build();
	}

	@Test
	public void testGetPackages() throws Exception
	{
		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn("test");
		List<Package> packages = newArrayList(package_);

		when(metaDataService.getPackages()).thenReturn(packages);
		when(packageMapper.toEditorPackage(package_)).thenReturn(getEditorPackage());

		this.mockMvc.perform(get("/plugin/metadata-manager-service/editorPackages")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorPackageResponse()));
	}

	@Test
	public void testGetEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);

		// metadataService.getEntityType is not used due to https://github.com/molgenis/molgenis/issues/5783
		Repository<EntityType> repository = mock(Repository.class);
		when(repository.findOneById("1")).thenReturn(entityType);
		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);

		when(entityTypeMapper.toEditorEntityType(entityType)).thenReturn(getEditorEntityType());

		this.mockMvc.perform(get("/plugin/metadata-manager-service/entityType/1")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testGetEntityTypeRepositoryNotExists() throws Exception
	{
		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
				.thenThrow(new UnknownEntityException("Unknown entity [unknownId]"));

		this.mockMvc.perform(get("/plugin/metadata-manager-service/entityType/unknownId")).andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"errors\":[{\"message\":\"Unknown entity [unknownId]\"}]}"));
	}

	@Test
	public void testGetEntityTypeEntityDoesNotExist() throws Exception
	{
		Repository<EntityType> repository = mock(Repository.class);
		when(repository.findOneById("unknownId")).thenReturn(null);

		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);

		mockMvc.perform(get("/plugin/metadata-manager-service/entityType/unknownId")).andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"errors\":[{\"message\":\"Unknown EntityType [unknownId]\"}]}"));
	}

	@Test
	public void testCreateEntityType() throws Exception
	{
		when(entityTypeMapper.createEditorEntityType()).thenReturn(getEditorEntityType());
		this.mockMvc.perform(get("/plugin/metadata-manager-service/create/entityType")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testUpsertEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(entityTypeMapper.toEntityType(getEditorEntityType())).thenReturn(entityType);
		this.mockMvc.perform(post("/plugin/metadata-manager-service/entityType").contentType(APPLICATION_JSON)
				.content(getEditorEntityTypeJson())).andExpect(status().isOk());
		Mockito.verify(metaDataService).upsertEntityTypes(newArrayList(entityType));
	}

	@Test
	public void testCreateAttribute() throws Exception
	{
		when(attributeMapper.createEditorAttribute()).thenReturn(getEditorAttribute());
		this.mockMvc.perform(get("/plugin/metadata-manager-service/create/attribute")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorAttributeResponse()));
	}

	private EditorPackageIdentifier getEditorPackage()
	{
		return EditorPackageIdentifier.create("test", "test");
	}

	private String getEditorPackageResponse()
	{
		return "[{\"id\":\"test\",\"label\":\"test\"}]";
	}

	private EditorAttribute getEditorAttribute()
	{
		return EditorAttribute
				.create("1", null, null, null, null, null, null, null, false, false, false, null, ImmutableMap.of(),
						null, ImmutableMap.of(), false, ImmutableList.of(), null, null, false, false,
						ImmutableList.of(), null, null, null, 1);
	}

	private String getEditorAttributeResponse()
	{
		return "{\"attribute\":{\"id\":\"1\",\"nullable\":false,\"auto\":false,\"visible\":false,\"labelI18n\":{},\"descriptionI18n\":{},\"aggregatable\":false,\"enumOptions\":[],\"readonly\":false,\"unique\":false,\"tags\":[],\"sequenceNumber\":1},\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private EditorEntityType getEditorEntityType()
	{
		return EditorEntityType
				.create("1", null, ImmutableMap.of(), null, ImmutableMap.of(), false, "backend", null, null,
						ImmutableList.of(), ImmutableList.of(), null, null, ImmutableList.of());
	}

	private String getEditorEntityTypeResponseJson()
	{
		return "{\"entityType\":" + getEditorEntityTypeJson()
				+ ",\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private String getEditorEntityTypeJson()
	{
		return "{\"id\":\"1\",\"labelI18n\":{},\"descriptionI18n\":{},\"abstract0\":false,\"backend\":\"backend\",\"attributes\":[],\"tags\":[],\"lookupAttributes\":[]}";
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
	}
}