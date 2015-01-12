package org.molgenis.data.meta;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
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
	 * Adds a new Package
	 * 
	 * @param pack
	 */
	void addPackage(Package pack);

	/**
	 * Gets the entity meta data for a given entity.
	 * 
	 * @param name
	 *            the fullyQualifiedName of the entity
	 * @return EntityMetaData of the entity, or null if the entity does not exist
	 */
	EntityMetaData getEntityMetaData(String name);

	/**
	 * @deprecated Rebuilds all meta data chaches
	 * 
	 *             TODO remove
	 */
	@Deprecated
	void refreshCaches();

	Iterable<EntityMetaData> getEntityMetaDatas();

	/**
	 * Adds new EntityMeta and creates a new CrudRepository for the default backend
	 * 
	 * @param entityMeta
	 * @return
	 */
	CrudRepository addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Deletes an EntityMeta of the default backend
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Updates EntityMeta for the default backend
	 * 
	 * @param entityMeta
	 */
	void updateEntityMeta(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta for the default backend
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	/**
	 * Updates an Attribute to an EntityMeta for the default backend
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void updateAttribute(String entityName, AttributeMetaData attribute);

	/**
	 * Deletes an Attribute for the default backend
	 * 
	 * @param entityName
	 * @param attributeName
	 */
	void deleteAttribute(String entityName, String attributeName);
}
