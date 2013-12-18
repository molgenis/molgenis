package org.molgenis.catalogmanager;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class CatalogManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private CatalogManagerController catalogManagerController;

	@Autowired
	private CatalogManagerService catalogManagerService;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(catalogManagerController)
				.setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();
	}

	@Configuration
	public static class Config
	{
		@Bean
		public CatalogManagerController catalogManagerController()
		{
			return new CatalogManagerController(catalogManagerService());
		}

		@Bean
		public CatalogManagerService catalogManagerService()
		{
			return mock(CatalogManagerService.class);
		}
	}

	@Test
	public void listCatalogs() throws Exception
	{
		String catalogId1 = "1";
		String catalogId2 = "2";
		String catalogName1 = "name1";
		String catalogName2 = "name2";
		boolean isLoaded1 = true;
		boolean isLoaded2 = false;
		CatalogMeta catalogMeta1 = new CatalogMeta(catalogId1, catalogName1);
		CatalogMeta catalogMeta2 = new CatalogMeta(catalogId2, catalogName2);
		when(catalogManagerService.getCatalogs()).thenReturn(Arrays.asList(catalogMeta1, catalogMeta2));
		when(catalogManagerService.isCatalogLoaded(catalogId1)).thenReturn(isLoaded1);
		when(catalogManagerService.isCatalogLoaded(catalogId2)).thenReturn(isLoaded2);
		this.mockMvc
				.perform(get(CatalogManagerController.URI))
				.andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager"))
				.andExpect(
						model().attribute(
								"catalogs",
								Arrays.asList(new CatalogMetaModel(catalogId1, catalogName1, isLoaded1),
										new CatalogMetaModel(catalogId2, catalogName2, isLoaded2))));
	}

	@Test
	public void loadCatalog() throws Exception
	{
		String catalogId = "1";
		String catalogName = "name";
		boolean isLoaded = false;
		CatalogMeta catalogInfo1 = new CatalogMeta(catalogId, catalogName);
		when(catalogManagerService.getCatalogs()).thenReturn(Arrays.asList(catalogInfo1));
		when(catalogManagerService.isCatalogLoaded(catalogId)).thenReturn(isLoaded, !isLoaded);

		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", "1").param("load", "load")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager"))
				.andExpect(model().attributeExists("successMessage"))
				.andExpect(
						model().attribute("catalogs",
								Arrays.asList(new CatalogMetaModel(catalogId, catalogName, !isLoaded))));
	}

	@Test
	public void loadLoadedCatalog() throws Exception
	{
		String catalogId = "1";
		String catalogName = "name";
		boolean isLoaded = true;
		CatalogMeta catalogInfo1 = new CatalogMeta(catalogId, catalogName);
		when(catalogManagerService.getCatalogs()).thenReturn(Arrays.asList(catalogInfo1));
		when(catalogManagerService.isCatalogLoaded(catalogId)).thenReturn(isLoaded);

		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", "1").param("load", "load")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager"))
				.andExpect(model().attributeExists("errorMessage"))
				.andExpect(
						model().attribute("catalogs",
								Arrays.asList(new CatalogMetaModel(catalogId, catalogName, isLoaded))));
	}

	@Test
	public void loadNonExistingCatalog() throws Exception
	{
		doThrow(new UnknownCatalogException("catalog does not exist")).when(catalogManagerService).loadCatalog("bogus");

		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", "bogus").param("load", "load")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager")).andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	public void unloadLoadedCatalog() throws Exception
	{
		String catalogId = "id1";
		String catalogName = "name1";
		boolean isLoaded = true;
		CatalogMeta catalogInfo1 = new CatalogMeta(catalogId, catalogName);
		when(catalogManagerService.getCatalogs()).thenReturn(Arrays.asList(catalogInfo1));
		when(catalogManagerService.isCatalogLoaded(catalogId)).thenReturn(isLoaded);

		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", catalogId).param("unload", "unload")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager"))
				.andExpect(model().attributeExists("successMessage"))
				.andExpect(
						model().attribute("catalogs",
								Arrays.asList(new CatalogMetaModel(catalogId, catalogName, isLoaded))));
	}

	@Test
	public void unloadUnloadedCatalog() throws Exception
	{
		String catalogId = "1";
		String catalogName = "name1";
		boolean isLoaded = false;
		CatalogMeta catalogInfo1 = new CatalogMeta(catalogId, catalogName);
		when(catalogManagerService.getCatalogs()).thenReturn(Arrays.asList(catalogInfo1));
		when(catalogManagerService.isCatalogLoaded(catalogId)).thenReturn(isLoaded);
		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", "1").param("unload", "unload")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager"))
				.andExpect(model().attributeExists("errorMessage"))
				.andExpect(
						model().attribute("catalogs",
								Arrays.asList(new CatalogMetaModel(catalogId, catalogName, isLoaded))));
	}

	@Test
	public void unloadNonExistingCatalog() throws Exception
	{
		doThrow(new UnknownCatalogException("catalog does not exist")).when(catalogManagerService).unloadCatalog(
				"bogus");

		this.mockMvc
				.perform(
						post(CatalogManagerController.URI + "/load").param("id", "bogus").param("unload", "unload")
								.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk())
				.andExpect(view().name("view-catalogmanager")).andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	public void viewCatalog() throws Exception
	{
		CatalogItem catalogItem1 = mock(CatalogItem.class);
		when(catalogItem1.getId()).thenReturn("4");
		when(catalogItem1.getName()).thenReturn("item1");

		CatalogItem catalogItem2 = mock(CatalogItem.class);
		when(catalogItem2.getId()).thenReturn("5");
		when(catalogItem2.getName()).thenReturn("item2");

		CatalogFolder catalogFolder1 = mock(CatalogFolder.class);
		when(catalogFolder1.getId()).thenReturn("3");
		when(catalogFolder1.getName()).thenReturn("root");
		when(catalogFolder1.getItems()).thenReturn(Arrays.asList(catalogItem1, catalogItem2));

		CatalogFolder catalogRootFolder = mock(CatalogFolder.class);
		when(catalogRootFolder.getId()).thenReturn("2");
		when(catalogRootFolder.getName()).thenReturn("root");
		when(catalogRootFolder.getChildren()).thenReturn(Arrays.asList(catalogFolder1));

		String catalogId = "1";
		String catalogName = "catalog";
		Catalog catalog = mock(Catalog.class);
		when(catalog.getName()).thenReturn(catalogName);
		when(catalog.getChildren()).thenReturn(Collections.singletonList(catalogRootFolder));
		when(catalog.getVersion()).thenReturn("1");

		String expectedContent = "{\"title\":\"catalog\",\"version\":\"1\",\"authors\":[],\"name\":\"catalog\",\"selected\":false,\"children\":[{\"id\":\"2\",\"name\":\"root\",\"selected\":false,\"children\":[{\"id\":\"3\",\"name\":\"root\",\"selected\":false,\"items\":[{\"id\":\"4\",\"name\":\"item1\",\"selected\":false},{\"id\":\"5\",\"name\":\"item2\",\"selected\":false}]}]}]}";
		when(catalogManagerService.getCatalog(catalogId)).thenReturn(catalog);
		this.mockMvc.perform(get(CatalogManagerController.URI + "/view/1")).andExpect(status().isOk())
				.andExpect(content().string(expectedContent));
	}

	@Test
	public void viewNonExistingCatalog() throws Exception
	{
		String catalogId = "bogus";
		doThrow(new UnknownCatalogException("catalog does not exist")).when(catalogManagerService)
				.getCatalog(catalogId);
		this.mockMvc.perform(get(CatalogManagerController.URI + "/view/" + catalogId)).andExpect(
				status().isBadRequest());
	}
}
