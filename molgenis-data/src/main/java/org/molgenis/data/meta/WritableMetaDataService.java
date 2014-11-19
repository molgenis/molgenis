package org.molgenis.data.meta;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;

/**
 * Interface for the metadata repositories. Used to change the registered metadata.
 */
public interface WritableMetaDataService extends MetaDataService
{
	/**
	 * Removes all metadata for an entity.
	 * 
	 * @param name
	 *            fully qualified name of the entity
	 */
	void removeEntityMetaData(String name);

	/**
	 * Adds a new attribute to an existing entity
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void addAttributeMetaData(String entityName, AttributeMetaData attribute);

	/**
	 * Removes a existing attribute from an existing entity
	 * 
	 * @param entityName
	 * @param attributeName
	 */
	void removeAttributeMetaData(String entityName, String attributeName);

	/**
	 * Adds entity meta data for a new entity
	 * 
	 * @param entityMetaData
	 */
	void addEntityMetaData(EntityMetaData entityMetaData);

	void addPackage(Package p);

}
