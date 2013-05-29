package org.molgenis.lifelines.catalogue;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class CatalogIdConverterTest
{

	@Test
	public void catalogIdToOmxIdentifier()
	{
		assertEquals(CatalogIdConverter.catalogIdToOmxIdentifier("4"), "catalog_4");
		assertEquals(CatalogIdConverter.catalogIdToOmxIdentifier("catalog_4"), "catalog_catalog_4");
	}

	@Test
	public void omxIdentifierToCatalogId()
	{
		assertEquals(CatalogIdConverter.omxIdentifierToCatalogId("catalog_4"), "4");
		assertEquals(CatalogIdConverter.omxIdentifierToCatalogId("catalog_catalog_4"), "catalog_4");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void omxIdentifierToCatalogIdWithInvalidIdentifier()
	{
		CatalogIdConverter.omxIdentifierToCatalogId("bogus");
	}

	@Test
	public void catalogOfStudyDefinitionIdToOmxIdentifier()
	{
		assertEquals(CatalogIdConverter.catalogOfStudyDefinitionIdToOmxIdentifier("4"), "studydefinition_4");
		assertEquals(CatalogIdConverter.catalogOfStudyDefinitionIdToOmxIdentifier("studydefinition_4"),
				"studydefinition_studydefinition_4");
	}

	@Test
	public void omxIdentifierToCatalogOfStudyDefinitionId()
	{
		assertEquals(CatalogIdConverter.omxIdentifierToCatalogOfStudyDefinitionId("studydefinition_4"), "4");
		assertEquals(CatalogIdConverter.omxIdentifierToCatalogOfStudyDefinitionId("studydefinition_studydefinition_4"),
				"studydefinition_4");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void omxIdentifierToCatalogOfStudyDefinitionIdWithInvalidIdentifier()
	{
		CatalogIdConverter.omxIdentifierToCatalogOfStudyDefinitionId("bogus");
	}
}
