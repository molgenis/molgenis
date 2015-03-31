package org.molgenis.data.semanticsearch.service;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.Tag;
import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

import com.google.common.collect.Multimap;

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
	 * Retrieves all tags for an attribute, and groups them by relation
	 */
	Multimap<Relation, ObjectType> getTagsForAttribute(EntityMetaData entityMetaData,
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

	/**
	 * Removes all tags for a given entity
	 * 
	 * @param entityName
	 *            the name of the entity for which all tags should be removed
	 */
	void removeAllTagsFromEntity(String entityName);
}
