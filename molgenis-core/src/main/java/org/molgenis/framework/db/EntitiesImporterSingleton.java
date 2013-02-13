package org.molgenis.framework.db;

/**
 * Class allows modules to access an entities importer through molgenis-core
 * instead of through a generated class or application
 * 
 * FIXME refactor code so that this class can be removed
 */
@Deprecated
public class EntitiesImporterSingleton
{
	private static EntitiesImporter INSTANCE;

	private EntitiesImporterSingleton()
	{
	}

	public static EntitiesImporter getInstance()
	{
		return INSTANCE;
	}

	public static void setInstance(EntitiesImporter instance)
	{
		INSTANCE = instance;
	}
}
