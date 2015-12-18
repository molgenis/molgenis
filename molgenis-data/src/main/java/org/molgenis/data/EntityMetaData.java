package org.molgenis.data;

import java.util.Set;

/**
 * EntityMetaData defines the structure and attributes of an Entity. Attributes are unique. Other software components
 * can use this to interact with Entity and/or to configure backends and frontends, including Repository instances.
 */
public interface EntityMetaData
{
	/**
	 * Gets the package.
	 * 
	 * @return
	 */
	Package getPackage();

	/**
	 * Gets the fully qualified entity name.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Gets the name of the entity without the trailing packagename
	 * 
	 * @return
	 */
	String getSimpleName();

	/**
	 * The name of the backend where the entities of this type are stored
	 * 
	 * @return
	 */
	String getBackend();

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
	 * Label of the entity in the requested language
	 */
	String getLabel(String languageCode);

	/**
	 * Get available label language codes
	 */
	Set<String> getLabelLanguageCodes();

	/**
	 * Description of the entity
	 */
	String getDescription();

	/**
	 * Description of the entity in the requested language
	 */
	String getDescription(String languageCode);

	/**
	 * Get available description language codes
	 */
	Set<String> getDescriptionLanguageCodes();

	/**
	 * Returns all attributes. In case of compound attributes (attributes consisting of atomic attributes) only the
	 * compound attribute is returned. This attribute can be used to retrieve parts of the compound attribute.
	 * 
	 * In case EntityMetaData extends other EntityMetaData then the attributes of this EntityMetaData as well as its
	 * parent class are returned.
	 */
	Iterable<AttributeMetaData> getAttributes();

	/**
	 * Same as {@link #getAttributes()} but does not return attributes of its parent class.
	 * 
	 * @return
	 */
	Iterable<AttributeMetaData> getOwnAttributes();

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes consisting of atomic attributes) only
	 * the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 * 
	 * In case EntityMetaData extends other EntityMetaData then the attributes of this EntityMetaData as well as its
	 * parent class are returned.
	 */
	Iterable<AttributeMetaData> getAtomicAttributes();

	/**
	 * Same as {@link #getAtomicAttributes()} but does not return attributes of its parent class.
	 * 
	 * @return
	 */
	Iterable<AttributeMetaData> getOwnAtomicAttributes();

	/**
	 * Attribute that is used as unique Id. Id attribute should always be provided.
	 */
	AttributeMetaData getIdAttribute();

	/**
	 * Attribute that is used as unique label. If no label exist, returns getIdAttribute().
	 */
	AttributeMetaData getLabelAttribute();

	/**
	 * Gets the correct label attribute for the given language, or the default if not found
	 */
	AttributeMetaData getLabelAttribute(String languageCode);

	/**
	 * Returns attributes that must be searched in case of xref/mref search
	 */
	Iterable<AttributeMetaData> getLookupAttributes();

	/**
	 * Get attribute by name (case insensitive), returns null if not found
	 */
	AttributeMetaData getAttribute(String attributeName);

	/**
	 * Returns whether this entity has a attribute with expression
	 * 
	 * @return whether this entity has a attribute with expression
	 */
	boolean hasAttributeWithExpression();

	/**
	 * Entity can extend another entity, adding its properties to their own
	 */
	public EntityMetaData getExtends();

	Class<? extends Entity> getEntityClass();
}
