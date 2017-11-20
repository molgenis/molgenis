package org.molgenis.data.validation.meta;

import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.AttributeUtils;
import org.molgenis.data.validation.constraint.EntityTypeConstraintViolation;
import org.molgenis.util.stream.MultimapCollectors;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.validation.constraint.EntityTypeConstraint.*;
import static org.molgenis.util.EntityUtils.asStream;

/**
 * {@link EntityType} validator.
 */
@Component
public class EntityTypeValidator
{
	private final DataService dataService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;

	EntityTypeValidator(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
	}

	/**
	 * Validates entity meta data
	 *
	 * @param entityType entity meta data
	 */
	public Collection<EntityTypeConstraintViolation> validate(EntityType entityType)
	{
		List<EntityTypeConstraintViolation> constraintViolations = new ArrayList<>();

		validateEntityName(entityType).ifPresent(constraintViolations::add);
		validateEntityLabel(entityType).ifPresent(constraintViolations::add);
		validatePackage(entityType).ifPresent(constraintViolations::add);
		validateExtends(entityType).ifPresent(constraintViolations::add);
		validateOwnAttributes(entityType).forEach(constraintViolations::add);

		Map<String, Attribute> ownAllAttrMap = stream(entityType.getOwnAllAttributes().spliterator(), false).collect(
				toMap(Attribute::getIdentifier, Function.identity(), (u, v) -> u, LinkedHashMap::new));

		validateOwnIdAttribute(entityType, ownAllAttrMap).forEach(constraintViolations::add);
		validateOwnLabelAttribute(entityType, ownAllAttrMap).forEach(constraintViolations::add);
		validateOwnLookupAttributes(entityType, ownAllAttrMap).forEach(constraintViolations::add);
		validateBackend(entityType).ifPresent(constraintViolations::add);

		return constraintViolations;
	}

	/**
	 * Validate that the entity meta data backend exists
	 *
	 * @param entityType entity meta data
	 */
	private Optional<EntityTypeConstraintViolation> validateBackend(EntityType entityType)
	{
		// Validate backend exists
		String backendName = entityType.getBackend();
		RepositoryCollection repoCollection = dataService.getMeta().getBackend(backendName);
		if (repoCollection == null)
		{
			return Optional.of(new EntityTypeConstraintViolation(BACKEND_EXISTS, entityType));
		}
		return Optional.empty();
	}

	/**
	 * Validate that the lookup attributes owned by this entity are part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 */
	private static Stream<EntityTypeConstraintViolation> validateOwnLookupAttributes(EntityType entityType,
			Map<String, Attribute> ownAllAttrMap)
	{
		List<EntityTypeConstraintViolation> constraintViolations = new ArrayList<>();

		// Validate lookup attributes
		entityType.getOwnLookupAttributes().forEach(ownLookupAttr ->
		{
			// Validate that lookup attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownLookupAttr.getIdentifier());
			if (ownAttr == null)
			{
				constraintViolations.add(new EntityTypeConstraintViolation(LOOKUP_ATTRIBUTES_EXIST, entityType));
			}
		});

		return constraintViolations.stream();
	}

	/**
	 * Validate that the label attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 */
	private static Stream<EntityTypeConstraintViolation> validateOwnLabelAttribute(EntityType entityType,
			Map<String, Attribute> ownAllAttrMap)
	{
		// Validate label attribute
		Attribute ownLabelAttr = entityType.getOwnLabelAttribute();
		if (ownLabelAttr != null)
		{
			// Validate that label attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownLabelAttr.getIdentifier());
			if (ownAttr == null)
			{
				return Stream.of(new EntityTypeConstraintViolation(LABEL_ATTRIBUTE_EXISTS, entityType));
			}
		}
		return Stream.empty();
	}

	/**
	 * Validate that the ID attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 */
	private static Stream<EntityTypeConstraintViolation> validateOwnIdAttribute(EntityType entityType,
			Map<String, Attribute> ownAllAttrMap)
	{
		List<EntityTypeConstraintViolation> entityTypeConstraintViolations = new ArrayList<>();

		// Validate ID attribute
		Attribute ownIdAttr = entityType.getOwnIdAttribute();
		if (ownIdAttr != null)
		{
			// Validate that ID attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownIdAttr.getIdentifier());
			if (ownAttr == null)
			{
				entityTypeConstraintViolations.add(new EntityTypeConstraintViolation(ID_ATTRIBUTE_EXISTS, entityType));
			}

			// Validate that ID attribute data type is allowed
			if (!AttributeUtils.isIdAttributeTypeAllowed(ownIdAttr))
			{
				entityTypeConstraintViolations.add(new EntityTypeConstraintViolation(ID_ATTRIBUTE_TYPE, entityType));
			}

			// Validate that ID attribute is unique
			if (!ownIdAttr.isUnique())
			{
				entityTypeConstraintViolations.add(new EntityTypeConstraintViolation(ID_ATTRIBUTE_UNIQUE, entityType));
			}

			// Validate that ID attribute is not nillable
			if (ownIdAttr.isNillable())
			{
				entityTypeConstraintViolations.add(
						new EntityTypeConstraintViolation(ID_ATTRIBUTE_NOT_NULL, entityType));
			}
		}
		else
		{
			if (!entityType.isAbstract() && entityType.getIdAttribute() == null)
			{
				entityTypeConstraintViolations.add(
						new EntityTypeConstraintViolation(ID_ATTRIBUTE_REQUIRED, entityType));
			}
		}
		return entityTypeConstraintViolations.stream();
	}

	/**
	 * Validates the attributes owned by this entity:
	 * 1) validates that the parent entity doesn't have attributes with the same name
	 * 2) validates that this entity doesn't have attributes with the same name
	 * 3) validates that this entity has attributes defined at all
	 *
	 * @param entityType entity meta data
	 */
	private static Stream<EntityTypeConstraintViolation> validateOwnAttributes(EntityType entityType)
	{
		// Validate that entity has attributes
		if (asStream(entityType.getAllAttributes()).collect(toList()).isEmpty())
		{
			return Stream.of(new EntityTypeConstraintViolation(HAS_ATTRIBUTES, entityType));
		}

		List<EntityTypeConstraintViolation> constraintViolations = new ArrayList<>();

		// Validate that entity does not contain multiple attributes with the same name
		Multimap<String, Attribute> attrMultiMap = asStream(entityType.getAllAttributes()).collect(
				MultimapCollectors.toArrayListMultimap(Attribute::getName, Function.identity()));
		attrMultiMap.keySet().forEach(attrName ->
		{
			if (attrMultiMap.get(attrName).size() > 1)
			{
				constraintViolations.add(new EntityTypeConstraintViolation(ATTRIBUTES_UNIQUE, entityType));
			}
		});

		// Validate that entity attributes with same name do no exist in parent entity
		EntityType extendsEntityType = entityType.getExtends();
		if (extendsEntityType != null)
		{
			Map<String, Attribute> extendsAllAttrMap = stream(extendsEntityType.getAllAttributes().spliterator(), false)
					.collect(toMap(Attribute::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));

			entityType.getOwnAllAttributes().forEach(attr ->
			{
				if (extendsAllAttrMap.containsKey(attr.getName()))
				{
					constraintViolations.add(new EntityTypeConstraintViolation(ATTRIBUTE_IN_PARENT, entityType));
				}
			});
		}

		return constraintViolations.stream();
	}

	/**
	 * Validates if this entityType extends another entityType. If so, checks whether that parent entityType is abstract.
	 *
	 * @param entityType entity meta data
	 */
	private static Optional<EntityTypeConstraintViolation> validateExtends(EntityType entityType)
	{
		if (entityType.getExtends() != null)
		{
			EntityType extendedEntityType = entityType.getExtends();
			if (!extendedEntityType.isAbstract())
			{
				return Optional.of(new EntityTypeConstraintViolation(EXTENDS_NOT_ABSTRACT, entityType));
			}
		}
		return Optional.empty();
	}

	/**
	 * Validates the entity fully qualified name and simple name:
	 * - Validates that the entity simple name does not contain illegal characters and validates the name length
	 * - Validates that the fully qualified name, simple name and package name are consistent with each other
	 *
	 * @param entityType entity meta data
	 */
	private static Optional<EntityTypeConstraintViolation> validateEntityName(EntityType entityType)
	{
		// validate entity name (e.g. illegal characters, length)
		String name = entityType.getId();
		if (!name.equals(ATTRIBUTE_META_DATA) && !name.equals(ENTITY_TYPE_META_DATA) && !name.equals(PACKAGE))
		{
			try
			{
				NameValidator.validateEntityName(entityType.getId());
			}
			catch (MolgenisDataException e)
			{
				return Optional.of(new EntityTypeConstraintViolation(NAME, entityType));
			}
		}
		return Optional.empty();
	}

	/**
	 * Validates the entity label:
	 * - Validates that the label is not an empty string
	 * - Validates that the label does not only consist of white space
	 *
	 * @param entityType entity meta data
	 */
	private static Optional<EntityTypeConstraintViolation> validateEntityLabel(EntityType entityType)
	{
		String label = entityType.getLabel();
		if (label != null)
		{
			if (label.isEmpty())
			{
				return Optional.of(new EntityTypeConstraintViolation(LABEL_NOT_EMPTY, entityType));
			}
			else if (label.trim().equals(""))
			{
				return Optional.of(new EntityTypeConstraintViolation(LABEL_NOT_WHITESPACE_ONLY, entityType));
			}
		}
		return Optional.empty();
	}

	/**
	 * Validate that non-system entities are not assigned to a system package
	 *
	 * @param entityType entity type
	 */
	private Optional<EntityTypeConstraintViolation> validatePackage(EntityType entityType)
	{
		Package aPackage = entityType.getPackage();
		if (aPackage != null && MetaUtils.isSystemPackage(aPackage) && !systemEntityTypeRegistry.hasSystemEntityType(
				entityType.getId()))
		{
			return Optional.of(new EntityTypeConstraintViolation(PACKAGE_NOT_SYSTEM, entityType));
		}
		return Optional.empty();
	}
}
