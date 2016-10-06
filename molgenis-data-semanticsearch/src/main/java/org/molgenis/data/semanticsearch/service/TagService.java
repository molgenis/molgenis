package org.molgenis.data.semanticsearch.service;

import com.google.common.collect.Multimap;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.meta.OntologyTermEntity;

/**
 * Service that administrates tags on attributes, entities and packages of a particular code system.
 *
 * @param <ObjectType>     the type of the tag object, for instance {@link OntologyTermEntity}
 * @param <CodeSystemType> the type of the code system, for instance {@link OntologyEntity}
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
	Iterable<SemanticTag<Package, ObjectType, CodeSystemType>> getTagsForPackage(Package p);

	/**
	 * Retrieves all tags for an entity.
	 */
	Iterable<SemanticTag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(
			EntityMetaData entityMetaData);

	/**
	 * Tags an attribute.
	 *
	 * @param entityMetaData the entity the tagged attribute sits on
	 * @param tag            the tag to add
	 */
	void addAttributeTag(EntityMetaData entityMetaData, SemanticTag<AttributeMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Removes attribute tag
	 *
	 * @param entityMetaData the entity the tagged attribute sits on
	 * @param tag            the tag to remove
	 */
	void removeAttributeTag(EntityMetaData entityMetaData,
			SemanticTag<AttributeMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Tags an entity.
	 *
	 * @param tag the tag to add
	 */
	void addEntityTag(SemanticTag<EntityMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Removes an entity tag.
	 *
	 * @param tag the tag to remove
	 */
	void removeEntityTag(SemanticTag<EntityMetaData, ObjectType, CodeSystemType> tag);

	/**
	 * Removes all tags for a given entity
	 *
	 * @param entityName the name of the entity for which all tags should be removed
	 */
	void removeAllTagsFromEntity(String entityName);
}
