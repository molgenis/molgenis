package org.molgenis.data.meta;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

public interface MetaDataService extends ApplicationListener<ContextRefreshedEvent>, Ordered
{
	/**
	 * Sets the Backend, in wich the meta data and the user data is saved
	 *
	 * @param ManageableRepositoryCollection
	 */
	void setDefaultBackend(ManageableRepositoryCollection backend);

	/**
	 * Get the default backend
	 * 
	 * @return
	 */
	ManageableRepositoryCollection getDefaultBackend();

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
	Repository addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Deletes an EntityMeta of the default backend
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Updates EntityMeta for the default backend
	 * 
	 * @param entityMeta
	 * @return added attributes
	 * 
	 *         FIXME remove return value or change it to ChangeSet with all changes
	 */
	List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta for the default backend
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	// FIXME remove this method
	void addAttributeSync(String entityName, AttributeMetaData attribute);

	/**
	 * Deletes an Attribute for the default backend
	 * 
	 * @param entityName
	 * @param attributeName
	 */
	void deleteAttribute(String entityName, String attributeName);

	// FIXME remove this method
	List<AttributeMetaData> updateSync(EntityMetaData sourceEntityMetaData);
}
