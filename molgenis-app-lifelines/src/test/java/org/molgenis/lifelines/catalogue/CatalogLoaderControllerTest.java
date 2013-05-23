package org.molgenis.lifelines.catalogue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CatalogLoaderControllerTest
{
	private CatalogLoaderController catalogLoaderController;
	private Model model;

	@BeforeMethod
	public void setUp()
	{
		catalogLoaderController = new CatalogLoaderController(new MockCatalogueLoaderService(
				Arrays.asList(new CatalogInfo("id", "name"))));
		model = new ExtendedModelMap();
	}

	@Test
	public void listCatalogs()
	{
		String view = catalogLoaderController.listCatalogs(model);
		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertTrue(model.containsAttribute("catalogs"));
		assertTrue(model.asMap().get("catalogs") instanceof List);

		@SuppressWarnings("unchecked")
		List<CatalogInfo> catalogs = (List<CatalogInfo>) model.asMap().get("catalogs");
		assertEquals(catalogs.size(), 1);
		assertEquals(catalogs.get(0).getId(), "id");
		assertEquals(catalogs.get(0).getName(), "name");
	}

	@Test
	public void loadExistingCatalog()
	{
		String view = catalogLoaderController.loadCatalog("id", model);

		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertTrue(model.containsAttribute("successMessage"));
		assertFalse(model.containsAttribute("errorMessage"));

		assertTrue(model.containsAttribute("catalogs"));
		assertTrue(model.asMap().get("catalogs") instanceof List);

		@SuppressWarnings("unchecked")
		List<CatalogInfo> catalogs = (List<CatalogInfo>) model.asMap().get("catalogs");
		assertEquals(catalogs.size(), 1);
		assertEquals(catalogs.get(0).getId(), "id");
		assertEquals(catalogs.get(0).getName(), "name");
	}

	@Test
	public void loadNonExistingCatalog()
	{
		String view = catalogLoaderController.loadCatalog("bogus", model);

		assertEquals(view, CatalogLoaderController.VIEW_NAME);
		assertFalse(model.containsAttribute("successMessage"));
		assertTrue(model.containsAttribute("errorMessage"));
	}

}
