package org.molgenis.data.validation.meta;

import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.AttributeUtils;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.stream.MultimapCollectors;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.util.EntityUtils.asStream;

/**
 * Entity metadata validator
 */
@Component
public class EntityTypeValidator
{
	private final DataService dataService;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;

	public EntityTypeValidator(DataService dataService, SystemEntityTypeRegistry systemEntityTypeRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
	}

	/**
	 * Validates entity meta data
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if entity meta data is not valid
	 */
	public void validate(EntityType entityType)
	{
		validateEntityId(entityType);
		validateEntityLabel(entityType);
		validatePackage(entityType);
		validateExtends(entityType);
		validateOwnAttributes(entityType);

		Map<String, Attribute> ownAllAttrMap = stream(entityType.getOwnAllAttributes().spliterator(), false).collect(
				toMap(Attribute::getIdentifier, Function.identity(), (u, v) ->
				{
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));

		validateOwnIdAttribute(entityType, ownAllAttrMap);
		validateOwnLabelAttribute(entityType, ownAllAttrMap);
		validateOwnLookupAttributes(entityType, ownAllAttrMap);
		validateBackend(entityType);
	}

	/**
	 * Validate that the entity meta data backend exists
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if the entity meta data backend does not exist
	 */
	void validateBackend(EntityType entityType)
	{
		// Validate backend exists
		String backendName = entityType.getBackend();
		if (!dataService.getMeta().hasBackend(backendName))
		{
			throw new MolgenisValidationException(new ConstraintViolation(format("Unknown backend [%s]", backendName)));
		}
	}

	/**
	 * Validate that the lookup attributes owned by this entity are part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if one or more lookup attributes are not entity attributes
	 */
	static void validateOwnLookupAttributes(EntityType entityType, Map<String, Attribute> ownAllAttrMap)
	{
		// Validate lookup attributes
		entityType.getOwnLookupAttributes().forEach(ownLookupAttr ->
		{
			// Validate that lookup attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownLookupAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Lookup attribute [%s] is not one of the attributes of entity [%s]",
								ownLookupAttr.getName(), entityType.getId())));
			}

			// Validate that the lookup attribute is visible
			if (!ownLookupAttr.isVisible())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Lookup attribute [%s] of entity type [%s] must be visible", ownLookupAttr.getName(),
								entityType.getId())));
			}
		});
	}

	/**
	 * Validate that the label attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if the label attribute is not an entity attribute
	 */
	static void validateOwnLabelAttribute(EntityType entityType, Map<String, Attribute> ownAllAttrMap)
	{
		// Validate label attribute
		Attribute ownLabelAttr = entityType.getOwnLabelAttribute();
		if (ownLabelAttr != null)
		{
			// Validate that label attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownLabelAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Label attribute [%s] is not is not one of the attributes of entity [%s]",
								ownLabelAttr.getName(), entityType.getId())));
			}
		}
		else
		{
			if (!entityType.isAbstract() && !entityType.getIdAttribute().isVisible())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] must define a label attribute because the identifier is hidden",
								entityType.getId())));
			}
		}
	}

	/**
	 * Validate that the ID attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityType    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if the ID attribute is not an entity attribute
	 */
	static void validateOwnIdAttribute(EntityType entityType, Map<String, Attribute> ownAllAttrMap)
	{
		// Validate ID attribute
		Attribute ownIdAttr = entityType.getOwnIdAttribute();
		if (ownIdAttr != null)
		{
			// Validate that ID attribute is in the attributes list
			Attribute ownAttr = ownAllAttrMap.get(ownIdAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] ID attribute [%s] is not part of the entity attributes",
								entityType.getId(), ownIdAttr.getName())));
			}

			// Validate that ID attribute data type is allowed
			if (!AttributeUtils.isIdAttributeTypeAllowed(ownIdAttr))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] ID attribute [%s] type [%s] is not allowed", entityType.getId(),
								ownIdAttr.getName(), ownIdAttr.getDataType().toString())));
			}

			// Validate that ID attribute is unique
			if (!ownIdAttr.isUnique())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] ID attribute [%s] is not a unique attribute", entityType.getId(),
								ownIdAttr.getName())));
			}

			// Validate that ID attribute is not nillable
			if (ownIdAttr.isNillable())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] ID attribute [%s] is not a non-nillable attribute", entityType.getId(),
								ownIdAttr.getName())));
			}
		}
		else
		{
			if (!entityType.isAbstract() && entityType.getIdAttribute() == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] is missing required ID attribute", entityType.getId())));
			}
		}
	}

	/**
	 * Validates the attributes owned by this entity:
	 * 1) validates that the parent entity doesn't have attributes with the same name
	 * 2) validates that this entity doesn't have attributes with the same name
	 * 3) validates that this entity has attributes defined at all
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if an attribute is owned by another entity or a parent attribute has the same name
	 */
	static void validateOwnAttributes(EntityType entityType)
	{
		// Validate that entity has attributes
		if (asStream(entityType.getAllAttributes()).collect(toList()).isEmpty())
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("EntityType [%s] does not contain any attributes. Did you use the correct package+entity name combination in both the entities as well as the attributes sheet?",
							entityType.getId())));
		}

		// Validate that entity does not contain multiple attributes with the same name
		Multimap<String, Attribute> attrMultiMap = asStream(entityType.getAllAttributes()).collect(
				MultimapCollectors.toArrayListMultimap(Attribute::getName, Function.identity()));
		attrMultiMap.keySet().forEach(attrName ->
		{
			if (attrMultiMap.get(attrName).size() > 1)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] contains multiple attributes with name [%s]", entityType.getId(),
								attrName)));
			}
		});
	}

	/**
	 * Validates if this entityType extends another entityType. If so, checks whether that parent entityType is abstract.
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if the entity extends from a non-abstract entity
	 */
	static void validateExtends(EntityType entityType)
	{
		if (entityType.getExtends() != null)
		{
			EntityType extendedEntityType = entityType.getExtends();
			if (!extendedEntityType.isAbstract())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("EntityType [%s] is not abstract; EntityType [%s] can't extend it",
								entityType.getExtends().getId(), entityType.getId())));
			}
		}
	}

	/**
	 * Validates the entity ID:
	 * - Validates that the entity ID does not contain illegal characters and validates the name length
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if the entity simple name content is invalid or the fully qualified name, simple name and package name are not consistent
	 */
	static void validateEntityId(EntityType entityType)
	{
		// validate entity name (e.g. illegal characters, length)
		String id = entityType.getId();
		if (!id.equals(ATTRIBUTE_META_DATA) && !id.equals(ENTITY_TYPE_META_DATA) && !id.equals(PACKAGE))
		{
			try
			{
				NameValidator.validateEntityName(entityType.getId());
			}
			catch (MolgenisDataException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(e.getMessage()));
			}
		}
	}

	/**
	 * Validates the entity label:
	 * - Validates that the label is not an empty string
	 * - Validates that the label does not only consist of white space
	 *
	 * @param entityType entity meta data
	 * @throws MolgenisValidationException if the entity label is invalid
	 */
	static void validateEntityLabel(EntityType entityType)
	{
		String label = entityType.getLabel();
		if (label != null)
		{
			if (label.isEmpty())
			{
				throw new MolgenisValidationException(
						new ConstraintViolation(format("Label of EntityType [%s] is empty", entityType.getId())));
			}
			else if (label.trim().equals(""))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Label of EntityType [%s] contains only white space", entityType.getId())));
			}
		}
	}

	/**
	 * Validate that non-system entities are not assigned to a system package
	 *
	 * @param entityType entity type
	 */
	void validatePackage(EntityType entityType)
	{
		Package package_ = entityType.getPackage();
		if (package_ != null)
		{
			if (MetaUtils.isSystemPackage(package_) && !systemEntityTypeRegistry.hasSystemEntityType(
					entityType.getId()))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Adding entity [%s] to system package [%s] is not allowed", entityType.getId(),
								entityType.getPackage().getId())));
			}
		}
	}
}
