package org.molgenis.data;

import org.molgenis.data.meta.AttributeMetaData;

public interface ManageableRepositoryCollection extends RepositoryCollection
{
	/**
	 * Removes an entity definition from this ManageableCrudRepositoryCollection
	 * 
	 * @param entityName
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Adds an Attribute to an EntityMeta
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	/**
	 * Removes an attribute from an entity
	 * 
	 * @param entityName
	 * @param attributeName
	 */
	void deleteAttribute(String entityName, String attributeName);

	void addAttributeSync(String entityName, AttributeMetaData attribute);
}
