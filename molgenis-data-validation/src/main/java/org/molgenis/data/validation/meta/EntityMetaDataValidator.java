package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.AttributeMetaDataUtils;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.MetaValidationUtils.validateName;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;

@Component
public class EntityMetaDataValidator
{
	private final DataService dataService;

	@Autowired
	public EntityMetaDataValidator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public void validate(EntityMetaData entityMeta)
	{
		validateEntityName(entityMeta);
		validateOwnAttributes(entityMeta);

		Map<String, AttributeMetaData> ownAllAttrMap = stream(entityMeta.getOwnAllAttributes().spliterator(), false)
				.collect(toMap(AttributeMetaData::getIdentifier, Function.identity(), (u, v) ->
				{
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));

		validateOwnIdAttribute(entityMeta, ownAllAttrMap);
		validateOwnLabelAttribute(entityMeta, ownAllAttrMap);
		validateOwnLookupAttributes(entityMeta, ownAllAttrMap);
		validateBackend(entityMeta);
	}

	/**
	 * Validate that the entity meta data backend exists
	 *
	 * @param entityMeta entity meta data
	 * @throws MolgenisValidationException if the entity meta data backend does not exist
	 */
	private void validateBackend(EntityMetaData entityMeta)
	{
		// Validate backend exists
		String backendName = entityMeta.getBackend();
		RepositoryCollection repoCollection = dataService.getMeta().getBackend(backendName);
		if (repoCollection == null)
		{
			throw new MolgenisValidationException(new ConstraintViolation(format("Unknown backend [%s]", backendName)));
		}
	}

	/**
	 * Validate that the lookup attributes owned by this entity are part of the owned attributes.
	 *
	 * @param entityMeta    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if one or more lookup attributes are not entity attributes
	 */
	private static void validateOwnLookupAttributes(EntityMetaData entityMeta,
			Map<String, AttributeMetaData> ownAllAttrMap)
	{
		// Validate lookup attributes
		entityMeta.getOwnLookupAttributes().forEach(ownLookupAttr ->
		{
			// Validate that lookup attribute is in the attributes list
			AttributeMetaData ownAttr = ownAllAttrMap.get(ownLookupAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Lookup attribute [%s] is not part of the entity attributes", ownLookupAttr.getName())));
			}
		});
	}

	/**
	 * Validate that the label attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityMeta    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if the label attribute is not an entity attribute
	 */
	private static void validateOwnLabelAttribute(EntityMetaData entityMeta,
			Map<String, AttributeMetaData> ownAllAttrMap)
	{
		// Validate label attribute
		AttributeMetaData ownLabelAttr = entityMeta.getOwnLabelAttribute();
		if (ownLabelAttr != null)
		{
			// Validate that label attribute is in the attributes list
			AttributeMetaData ownAttr = ownAllAttrMap.get(ownLabelAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Label attribute [%s] is not part of the entity attributes", ownLabelAttr.getName())));
			}
		}
	}

	/**
	 * Validate that the ID attribute owned by this entity is part of the owned attributes.
	 *
	 * @param entityMeta    entity meta data
	 * @param ownAllAttrMap attribute identifier to attribute map
	 * @throws MolgenisValidationException if the ID attribute is not an entity attribute
	 */
	private static void validateOwnIdAttribute(EntityMetaData entityMeta, Map<String, AttributeMetaData> ownAllAttrMap)
	{
		// Validate ID attribute
		AttributeMetaData ownIdAttr = entityMeta.getOwnIdAttribute();
		if (ownIdAttr != null)
		{
			// Validate that ID attribute is in the attributes list
			AttributeMetaData ownAttr = ownAllAttrMap.get(ownIdAttr.getIdentifier());
			if (ownAttr == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("ID attribute [%s] is not part of the entity attributes", ownIdAttr.getName())));
			}

			// Validate that ID attribute data type is allowed
			if (!AttributeMetaDataUtils.isIdAttributeTypeAllowed(ownIdAttr))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("ID attribute [%s] type [%s] is not allowed", ownIdAttr.getName(),
								ownIdAttr.getDataType().toString())));
			}

			// Validate that ID attribute is unique
			if (!ownIdAttr.isUnique())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("ID attribute [%s] is not a unique attribute", ownIdAttr.getName())));
			}

			// Validate that ID attribute is not nillable
			if (ownIdAttr.isNillable())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("ID attribute [%s] is not a non-nillable attribute", ownIdAttr.getName())));
			}
		}
		else
		{
			if (!entityMeta.isAbstract() && entityMeta.getIdAttribute() == null)
			{
				throw new MolgenisValidationException(new ConstraintViolation("Missing required ID attribute"));
			}
		}
	}

	/**
	 * Validates the attributes owned by this entity:
	 * 1) validates that attributes are not owned by another entity
	 * 2) validates that the parent entity doesn't have entities with the same name
	 *
	 * @param entityMeta entity meta data
	 * @throws MolgenisValidationException if an attribute is owned by another entity or a parent attribute has the same name
	 */
	private void validateOwnAttributes(EntityMetaData entityMeta)
	{
		// Validate that entity attributes are not owned by another entity
		entityMeta.getOwnAllAttributes().forEach(attr ->
		{
			EntityMetaData ownerEntityMeta = getAttributeOwner(attr);
			if (ownerEntityMeta != null && !ownerEntityMeta.getName().equals(entityMeta.getName()))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Attribute [%s] is owned by entity [%s]", attr.getName(), ownerEntityMeta.getName())));
			}
		});

		// Validate that entity attributes with same name do no exist in parent entity
		EntityMetaData extendsEntityMeta = entityMeta.getExtends();
		if (extendsEntityMeta != null)
		{
			Map<String, AttributeMetaData> extendsAllAttrMap = stream(
					extendsEntityMeta.getAllAttributes().spliterator(), false)
					.collect(toMap(AttributeMetaData::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));

			entityMeta.getOwnAllAttributes().forEach(attr ->
			{
				if (extendsAllAttrMap.containsKey(attr.getName()))
				{
					throw new MolgenisValidationException(new ConstraintViolation(
							format("An attribute with name [%s] already exists in entity [%s] or one of its parents",
									attr.getName(), extendsEntityMeta.getName())));
				}
			});
		}
	}

	/**
	 * Validates the entity fully qualified name and simple name:
	 * - Validates that the entity simple name does not contain illegal characters and validates the name length
	 * - Validates that the fully qualified name, simple name and package name are consistent with each other
	 *
	 * @param entityMeta entity meta data
	 * @throws MolgenisValidationException if the entity simple name content is invalid or the fully qualified name, simple name and package name are not consistent
	 */
	private static void validateEntityName(EntityMetaData entityMeta)
	{
		// validate entity name (e.g. illegal characters, length)
		String name = entityMeta.getName();
		if (!name.equals(ATTRIBUTE_META_DATA) && !name.equals(ENTITY_META_DATA) && !name.equals(PACKAGE))
		{
			try
			{
				validateName(entityMeta.getSimpleName());
			}
			catch (MolgenisDataException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(e.getMessage()));
			}
		}

		// Validate that entity name equals entity package name + underscore + entity simple name
		Package package_ = entityMeta.getPackage();
		if (package_ != null)
		{
			if (!(package_.getName() + '_' + entityMeta.getSimpleName()).equals(entityMeta.getName()))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Qualified entity name [%s] not equal to entity package name [%s] underscore entity name [%s]",
								entityMeta.getName(), package_.getName(), entityMeta.getSimpleName())));
			}
		}
		else
		{
			if (!entityMeta.getSimpleName().equals(entityMeta.getName()))
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Qualified entity name [%s] not equal to entity name [%s]", entityMeta.getName(),
								entityMeta.getSimpleName())));
			}
		}
	}

	/**
	 * Returns the entity that owns the given attribute.
	 *
	 * @param attr attribute
	 * @return attribute owner
	 */
	private EntityMetaData getAttributeOwner(AttributeMetaData attr)
	{
		EntityMetaData entityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(ATTRIBUTES, attr)
				.findOne();
		if (entityMeta == null)
		{
			AttributeMetaData parentAttr = dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)
					.eq(PARTS, attr).findOne();
			if (parentAttr != null)
			{
				entityMeta = getAttributeOwner(parentAttr);
			}
			else
			{
				entityMeta = null;
			}
		}
		return entityMeta;
	}
}
