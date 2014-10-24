package org.molgenis.data.semantic;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

/**
 * Service that administrates tags on attributes, entities and packages of a particular code system.
 * 
 * @param <ObjectType>
 *            the type of the tag object, for instance {@link OntologyTerm}
 * @param <CodeSystemType>
 *            the type of the code system, for instance {@link Ontology}
 */
public interface TagService<ObjectType, CodeSystemType>
{
	/**
	 * Retrieves all tags for an attribute
	 * 
	 * @param entityMetaData
	 *            the entity that the attribute
	 * @param attributeMetaData
	 * @return
	 */
	Iterable<Tag<AttributeMetaData, ObjectType, CodeSystemType>> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData);

	// void getTagsForEntity();

	// void getTagsForPackage();

	/**
	 * Tags an attribute.
	 * 
	 * @param entityMetaData
	 *            the entity the tagged attribute sits on
	 * @param tag
	 *            the tag to add
	 */
	void addAttributeTag(EntityMetaData entityMetaData, Tag<AttributeMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Removes attribute tag
	 * 
	 * @param entityMetaData
	 *            the entity the tagged attribute sits on
	 * @param tag
	 *            the tag to remove
	 */
	void removeAttributeTag(EntityMetaData entityMetaData, Tag<AttributeMetaData, ObjectType, CodeSystemType> tag);
}
