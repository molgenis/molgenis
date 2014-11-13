package org.molgenis.data.meta;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;

public interface MetaDataService
{
	/**
	 * Lists all packages.
	 * 
	 * @return Iterable of all Packages
	 */
	Iterable<Package> getRootPackages();

	/**
	 * Retrieves a package with a given name.
	 * 
	 * @param name
	 *            the name of the Package to retrieve
	 * @return the Package, or null if the package does not exist.
	 */
	Package getPackage(String name);

	/**
	 * Gets the entity meta data for a given entity.
	 * 
	 * @param name
	 *            the fullyQualifiedName of the entity
	 * @return EntityMetaData of the entity, or null if the entity does not exist
	 */
	EntityMetaData getEntityMetaData(String name);

	/**
	 * Rebuilds all meta data chaches
	 * 
	 */
	void refreshCaches();

	Iterable<EntityMetaData> getEntityMetaDatas();
}
