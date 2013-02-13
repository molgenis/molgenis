package org.molgenis.framework.db;

/**
 * Class allows modules to access an entities importer through molgenis-core
 * instead of through a generated class or application
 * 
 * FIXME refactor code so that this class can be removed
 */
@Deprecated
public class EntitiesValidatorSingleton
{
	private static EntitiesValidator INSTANCE;

	private EntitiesValidatorSingleton()
	{
	}

	public static EntitiesValidator getInstance()
	{
		return INSTANCE;
	}

	public static void setInstance(EntitiesValidator instance)
	{
		INSTANCE = instance;
	}
}
