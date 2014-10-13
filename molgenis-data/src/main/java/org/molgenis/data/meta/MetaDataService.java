package org.molgenis.data.meta;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;

public interface MetaDataService
{
	/**
	 * Lists all packages.
	 * 
	 * @return Iterable of all Packages
	 */
	Iterable<Package> getPackages();

	/**
	 * Retrieves a package with a given name.
	 * 
	 * @param name
	 *            the name of the Package to retrieve
	 * @return the Package, or null if the package does not exist.
	 */
	Package getPackage(String name);

	/**
	 * Returns an iterable over all entity meta data, including abstract.
	 * 
	 * @return
	 */
	Iterable<EntityMetaData> getEntityMetaDatas();

	/**
	 * Gets all EntityMetaData in a package.
	 * 
	 * @param packageName
	 *            the name of the package
	 */
	public List<EntityMetaData> getPackageEntityMetaDatas(String packageName);

	/**
	 * Gets the entity meta data for a given entity.
	 * 
	 * @param name
	 *            the fullyQualifiedName of the entity
	 * @return EntityMetaData of the entity, or null if the entity does not exist
	 */
	EntityMetaData getEntityMetaData(String name);

	/**
	 * Get an entity's attributes
	 * 
	 * @param entityName
	 * @return
	 */
	Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName);

	/**
	 * Indicates if an entity exists.
	 * 
	 * @param entityMetaData
	 * @return
	 */
	boolean hasEntity(EntityMetaData entityMetaData);

}
