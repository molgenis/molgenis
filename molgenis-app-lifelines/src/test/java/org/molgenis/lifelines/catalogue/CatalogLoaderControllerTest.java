package org.molgenis.lifelines.catalogue;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.mock.MockDatabase;
import org.molgenis.util.Entity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CatalogLoaderControllerTest
{
	private CatalogLoaderController catalogLoaderController;
	private Model model;
	private MockDatabase database;

	@BeforeMethod
	public void setUp() throws UnknownCatalogException
	{
		database = new MockDatabase();
		database.setEntities(Collections.<Entity> emptyList());
		CatalogLoaderService catalogLoaderService = Mockito.mock(CatalogLoaderService.class);
		when(catalogLoaderService.findCatalogs()).thenReturn(Arrays.asList(new CatalogInfo("id", "name")));
		doThrow(new UnknownCatalogException()).when(catalogLoaderService).loadCatalog("bogus");
		catalogLoaderController = new CatalogLoaderController(catalogLoaderService, database);
		model = new ExtendedModelMap();
	}

	@Test
	public void listCatalogs() throws DatabaseException
	{
		String view = catalogLoaderController.listCatalogs(model);
		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertTrue(model.containsAttribute("catalogs"));
		assertTrue(model.asMap().get("catalogs") instanceof List);

		@SuppressWarnings("unchecked")
		List<CatalogModel> catalogs = (List<CatalogModel>) model.asMap().get("catalogs");
		assertEquals(catalogs.size(), 1);
		assertEquals(catalogs.get(0).getId(), "id");
		assertEquals(catalogs.get(0).getName(), "name");
		assertFalse(catalogs.get(0).isLoaded());
	}

	@Test
	public void loadExistingCatalog() throws DatabaseException
	{
		String view = catalogLoaderController.loadCatalog("id", model);

		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertTrue(model.containsAttribute("successMessage"));
		assertFalse(model.containsAttribute("errorMessage"));

		assertTrue(model.containsAttribute("catalogs"));
		assertTrue(model.asMap().get("catalogs") instanceof List);

		@SuppressWarnings("unchecked")
		List<CatalogModel> catalogs = (List<CatalogModel>) model.asMap().get("catalogs");
		assertEquals(catalogs.size(), 1);
		assertEquals(catalogs.get(0).getId(), "id");
		assertEquals(catalogs.get(0).getName(), "name");
		assertFalse(catalogs.get(0).isLoaded());
	}

	@Test
	public void loadNonExistingCatalog() throws DatabaseException
	{
		String view = catalogLoaderController.loadCatalog("bogus", model);

		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertFalse(model.containsAttribute("successMessage"));
		assertTrue(model.containsAttribute("errorMessage"));
	}

}
