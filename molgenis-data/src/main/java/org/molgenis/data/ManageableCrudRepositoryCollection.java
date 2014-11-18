package org.molgenis.data;

import java.util.List;

public interface ManageableCrudRepositoryCollection extends RepositoryCollection, Iterable<CrudRepository>
{
	/**
	 * Create and add a new CrudRepository for an EntityMetaData
	 */
	CrudRepository add(EntityMetaData entityMetaData);

	/**
	 * Update EntityMetaData.
	 * 
	 * At the moment only adding of AttributeMetaData is supported.
	 * 
	 * @param entityMetaData
	 * @return The newly added AttributeMetaData's
	 */
	List<AttributeMetaData> update(EntityMetaData entityMetaData);

	void dropAttributeMetaData(String entityName, String attributeName);

	void dropEntityMetaData(String entityName);
}
