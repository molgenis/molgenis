package org.molgenis.data.meta;

import org.molgenis.data.AttributeMetaData;

public interface AttributeMetaDataRepository
{
	/**
	 * Get an entity attribute
	 * 
	 * @param entityName
	 * @return
	 */
	Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName);

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
}