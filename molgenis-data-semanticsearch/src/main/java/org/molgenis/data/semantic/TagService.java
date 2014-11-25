package org.molgenis.data.semantic;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyTerm;

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
	 */
	Iterable<Tag<AttributeMetaData, ObjectType, CodeSystemType>> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData);

	/**
	 * Retrieves all tags for a package
	 */
	Iterable<Tag<Package, ObjectType, CodeSystemType>> getTagsForPackage(Package p);

	/**
	 * Retrieves all tags for an entity.
	 */
	Iterable<Tag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(EntityMetaData entityMetaData);

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

	/**
	 * Tags an entity.
	 * 
	 * @param entityMetaData
	 *            the entity the tagged attribute sits on
	 * @param tag
	 *            the tag to add
	 */
	void addEntityTag(Tag<EntityMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Removes an entity tag.
	 * 
	 * @param tag
	 *            the tag to remove
	 */
	void removeEntityTag(Tag<EntityMetaData, ObjectType, CodeSystemType> tag);

}
