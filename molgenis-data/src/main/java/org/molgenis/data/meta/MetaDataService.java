package org.molgenis.data.meta;

import java.util.LinkedHashMap;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import com.google.common.collect.ImmutableMap;

public interface MetaDataService extends Iterable<RepositoryCollection>, ApplicationListener<ContextRefreshedEvent>,
		Ordered
{
	/**
	 * Sets the Backend, in wich the meta data and the user data is saved
	 *
	 * @param ManageableRepositoryCollection
	 */
	MetaDataService setDefaultBackend(ManageableRepositoryCollection backend);

	/**
	 * Get a backend by name or null if it does not exists
	 * 
	 * @param name
	 * @return
	 */
	RepositoryCollection getBackend(String name);

	/**
	 * Get the backend the EntityMetaData belongs to
	 * 
	 * @param emd
	 * @return
	 */
	RepositoryCollection getBackend(EntityMetaData emd);

	/**
	 * Get the default backend
	 * 
	 * @return
	 */
	ManageableRepositoryCollection getDefaultBackend();

	/**
	 * Get all packages
	 * 
	 * @return List of Package
	 */
	public List<Package> getPackages();

	/**
	 * Lists all root packages.
	 * 
	 * @return Iterable of all root Packages
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
	 * Adds new EntityMeta and creates a new Repository
	 * 
	 * @param entityMeta
	 * @return
	 */
	Repository addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Create and add a new Repository for an EntityMetaData with repository decorators applied
	 */
	Repository add(EntityMetaData entityMetaData, RepositoryDecoratorFactory decoratorFactory);

	/**
	 * Deletes an EntityMeta
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Deletes a list of EntityMetaData
	 * 
	 * @param entities
	 */
	void delete(List<EntityMetaData> entities);

	/**
	 * Updates EntityMeta
	 * 
	 * @param entityMeta
	 * @return added attributes
	 * 
	 *         FIXME remove return value or change it to ChangeSet with all changes
	 */
	List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	// FIXME remove this method
	void addAttributeSync(String entityName, AttributeMetaData attribute);

	/**
	 * Deletes an Attribute
	 * 
	 * @param entityName
	 * @param attributeName
	 */
	void deleteAttribute(String entityName, String attributeName);

	// FIXME remove this method
	List<AttributeMetaData> updateSync(EntityMetaData sourceEntityMetaData);

	/**
	 * Check the integration of an entity meta data with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 * 
	 * @param repositoryCollection
	 *            the new entities
	 * @param defaultPackage
	 *            the default package for the entities that does not have a package
	 * @return
	 */
	LinkedHashMap<String, Boolean> integrationTestMetaData(RepositoryCollection repositoryCollection);

	/**
	 * Check the integration of an entity meta data with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 * 
	 * @param newEntitiesMetaDataMap
	 *            the new entities in a map where the keys are the names
	 * @param skipEntities
	 *            do not check the entities, returns true.
	 * @param defaultPackage
	 *            the default package for the entities that does not have a package
	 * @return
	 */
	LinkedHashMap<String, Boolean> integrationTestMetaData(ImmutableMap<String, EntityMetaData> newEntitiesMetaDataMap,
			List<String> skipEntities, String defaultPackage);
}
