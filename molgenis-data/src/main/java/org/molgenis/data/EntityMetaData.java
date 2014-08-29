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
	 * Entities can be abstract (analogous an 'interface' or 'protocol'). Use is to define reusable Entity model
	 * components that cannot be instantiated themselves (i.e. there cannot be data attached to this entity meta data).
	 */
	public boolean isAbstract();

	/**
	 * Optional human readable longer label
	 */
	String getLabel();

	/**
	 * Description of the entity
	 */
	String getDescription();

	/**
	 * Returns all attributes. In case of compound attributes (attributes consisting of multiple atomic attributes) only
	 * the compound attribute is returned. This attribute can be used to retrieve parts of the compound attribute.
	 */
	Iterable<AttributeMetaData> getAttributes();

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes consisting of multiple atomic
	 * attributes) only the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 */
	Iterable<AttributeMetaData> getAtomicAttributes();

	/**
	 * Attribute that is used as unique Id. Id attribute should always be provided.
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

	/**
	 * Entity can extend another entity, adding its properties to their own
	 */
	public EntityMetaData getExtends();

	Class<? extends Entity> getEntityClass();
}
