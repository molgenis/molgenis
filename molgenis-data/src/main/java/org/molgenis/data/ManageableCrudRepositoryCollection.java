package org.molgenis.data;

public interface ManageableCrudRepositoryCollection extends CrudRepositoryCollection, Iterable<CrudRepository>
{
	/**
	 * Create and add a new CrudRepository for an EntityMetaData
	 */
	CrudRepository addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Removes an entity definition from this ManageableCrudRepositoryCollection
	 * 
	 * @param entityName
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Updates EntityMetaData
	 * 
	 * At the moment only adding of AttributeMetaData is supported.
	 * 
	 * @param entityMeta
	 */
	void updateEntityMeta(EntityMetaData entityMeta);

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

	/**
	 * Updates an Attribute
	 * 
	 * @param entityName
	 * @param attribute
	 */
	void updateAttribute(String entityName, AttributeMetaData attribute);
}
