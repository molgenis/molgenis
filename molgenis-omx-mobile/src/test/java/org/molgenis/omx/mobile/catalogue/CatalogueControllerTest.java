package org.molgenis.omx.mobile.catalogue;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CatalogueControllerTest
{
	private CatalogueController catalogueController;

	@BeforeMethod
	public void setUp()
	{
		catalogueController = new CatalogueController();
	}

	@Test
	public void init()
	{
		assertNotNull(catalogueController.init());
	}
}
