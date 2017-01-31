package org.molgenis.ui.metadataeditor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ui.metadataeditor.mapper.AttributeMapper;
import org.molgenis.ui.metadataeditor.mapper.EntityTypeMapper;
import org.molgenis.ui.metadataeditor.model.EditorAttribute;
import org.molgenis.ui.metadataeditor.model.EditorEntityType;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { MetadataEditorControllerTest.Config.class, GsonConfig.class })
public class MetadataEditorControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

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
		MetadataEditorController metadataEditorController = new MetadataEditorController(metaDataService,
				entityTypeMapper, attributeMapper);

		mockMvc = MockMvcBuilders.standaloneSetup(metadataEditorController)
				.setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter).build();
	}

	@Test
	public void testInit() throws Exception
	{
		this.mockMvc.perform(get("/plugin/metadataeditor")).andExpect(status().isOk())
				.andExpect(view().name("view-metadataeditor"));
	}

	@Test
	public void testGetEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);

		// see note in MetadataEditorController why we cannot do the following:
		// when(metaDataService.getEntityType("1")).thenReturn(entityType);

		// instead we have to do:
		Repository<EntityType> repository = mock(Repository.class);
		when(repository.findOneById("1")).thenReturn(entityType);
		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);

		when(entityTypeMapper.toEditorEntityType(entityType)).thenReturn(getEditorEntityType());

		this.mockMvc.perform(get("/plugin/metadataeditor/entityType/1")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testGetEntityTypeNotExists() throws Exception
	{
		when(metaDataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
				.thenThrow(new UnknownEntityException("Unknown entity [unknownId]"));
		this.mockMvc.perform(get("/plugin/metadataeditor/entityType/unknownId")).andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"errors\":[{\"message\":\"Unknown entity [unknownId]\"}]}"));
	}

	@Test
	public void testCreateEntityType() throws Exception
	{
		when(entityTypeMapper.createEditorEntityType()).thenReturn(getEditorEntityType());
		this.mockMvc.perform(get("/plugin/metadataeditor/create/entityType")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testUpsertEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(entityTypeMapper.toEntityType(getEditorEntityType())).thenReturn(entityType);
		this.mockMvc.perform(post("/plugin/metadataeditor/entityType").contentType(APPLICATION_JSON)
				.content(getEditorEntityTypeJson())).andExpect(status().isOk());
		Mockito.verify(metaDataService).upsertEntityType(entityType);
	}

	@Test
	public void testCreateAttribute() throws Exception
	{
		when(attributeMapper.createEditorAttribute()).thenReturn(getEditorAttribute());
		this.mockMvc.perform(get("/plugin/metadataeditor/create/attribute")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(getEditorAttributeResponse()));
	}

	private EditorAttribute getEditorAttribute()
	{
		return EditorAttribute
				.create("1", null, null, null, null, null, null, null, false, false, false, null, ImmutableMap.of(),
						null, ImmutableMap.of(), false, ImmutableList.of(), null, null, false, false,
						ImmutableList.of(), null, null, null);
	}

	private String getEditorAttributeResponse()
	{
		return "{\"attribute\":{\"id\":\"1\",\"nullable\":false,\"auto\":false,\"visible\":false,\"labelI18n\":{},\"descriptionI18n\":{},\"aggregatable\":false,\"enumOptions\":[],\"readonly\":false,\"unique\":false,\"tags\":[]},\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private EditorEntityType getEditorEntityType()
	{
		return EditorEntityType
				.create("1", null, null, ImmutableMap.of(), null, ImmutableMap.of(), false, "backend", null, null,
						ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), null, null, ImmutableList.of());
	}

	private String getEditorEntityTypeResponseJson()
	{
		return "{\"entityType\":" + getEditorEntityTypeJson()
				+ ",\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private String getEditorEntityTypeJson()
	{
		return "{\"id\":\"1\",\"labelI18n\":{},\"descriptionI18n\":{},\"abstract0\":false,\"backend\":\"backend\",\"children\":[],\"attributes\":[],\"tags\":[],\"lookupAttributes\":[]}";
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