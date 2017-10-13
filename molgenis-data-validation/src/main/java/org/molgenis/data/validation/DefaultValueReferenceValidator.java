package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

/**
 * Validates whether attributes exist with default values referencing given entities.
 *
 * @see org.molgenis.data.meta.model.AttributeMetadata
 */
public interface DefaultValueReferenceValidator
{
	/**
	 * @throws MolgenisValidationException if the given entity is referenced by an Attribute
	 */
	void validateEntityNotReferenced(Entity entity);

	/**
	 * Appends a filter to the given stream that throws a MolgenisValidationException if an entity is referenced
	 * by an Attribute.
	 */
	Stream<Entity> validateEntitiesNotReferenced(Stream<Entity> entityStream, EntityType entityType);

	/**
	 * @throws MolgenisValidationException if the entity with the given id is referenced by an Attribute
	 */
	void validateEntityNotReferencedById(Object entityId, EntityType entityType);

	/**
	 * Appends a filter to the given stream that throws a MolgenisValidationException if an entity with the given id is
	 * referenced by an Attribute.
	 */
	Stream<Object> validateEntitiesNotReferencedById(Stream<Object> entityIdStream, EntityType entityType);

	/**
	 * @throws MolgenisValidationException if the given entity is referenced by an Attribute
	 */
	void validateEntityTypeNotReferenced(EntityType entityType);
}
