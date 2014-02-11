package org.molgenis.data;

/**
 * EntityMetaData defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public interface EntityMetaData
{
	/**
	 * Every EntityMetaData has a unique name
	 */
	String getName();

	/**
	 * Optional human readable longer label
	 */
	String getLabel();

	/**
	 * Description of the entity
	 */
	String getDescription();

	/**
	 * Meta data describing all attributes returns all attributes, including those of super classes
	 */
	Iterable<AttributeMetaData> getAttributes();
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	Iterable<AttributeMetaData> getLevelOneAttributes();	

	/**
	 * Attribute that is used as unique Id. If no label exist, returns null.
	 */
	AttributeMetaData getIdAttribute();

	/**
	 * Attribute that is used as unique label. If no label exist, returns getIdAttribute().
	 */
	AttributeMetaData getLabelAttribute();

	/**
	 * Get attribute by name (case insensitive), returns null if not found
	 */
	AttributeMetaData getAttribute(String attributeName);
}
