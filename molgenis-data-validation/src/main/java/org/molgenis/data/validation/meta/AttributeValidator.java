package org.molgenis.data.validation.meta;

import com.google.common.collect.Iterables;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.support.AttributeUtils.getValidIdAttributeTypes;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.UPDATE;

/**
 * Attribute metadata validator
 */
@Component
public class AttributeValidator
{
	public enum ValidationMode
	{
		ADD, ADD_SKIP_ENTITY_VALIDATION, UPDATE, UPDATE_SKIP_ENTITY_VALIDATION
	}

	private final DataService dataService;
	private final EntityManager entityManager;
	private final EmailValidator emailValidator;

	public AttributeValidator(DataService dataService, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.emailValidator = new EmailValidator();
	}

	public void validate(Attribute attr, ValidationMode validationMode)
	{
		validateName(attr);
		validateDefaultValue(attr, validationMode == ADD || validationMode == UPDATE);
		validateParent(attr);
		validateChildren(attr);

		switch (validationMode)
		{
			case ADD:
			case ADD_SKIP_ENTITY_VALIDATION:
				validateAdd(attr);
				break;
			case UPDATE:
			case UPDATE_SKIP_ENTITY_VALIDATION:
				Attribute currentAttr = dataService.findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(),
						Attribute.class);
				validateUpdate(attr, currentAttr);
				break;
			default:
				throw new UnexpectedEnumException(validationMode);
		}
	}

	private static void validateParent(Attribute attr)
	{
		if (attr.getParent() != null)
		{
			if (attr.getParent().getDataType() != COMPOUND)
			{
				throw new MolgenisDataException(
						format("Parent attribute [%s] of attribute [%s] is not of type compound",
								attr.getParent().getName(), attr.getName()));
			}
		}
	}

	private static void validateChildren(Attribute attr)
	{
		boolean childrenIsNullOrEmpty = attr.getChildren() == null || Iterables.isEmpty(attr.getChildren());

		if (!childrenIsNullOrEmpty && attr.getDataType() != COMPOUND)
		{
			throw new MolgenisDataException(
					format("Attribute [%s] is not of type COMPOUND and can therefor not have children",
							attr.getName()));
		}

	}

	private static void validateAdd(Attribute newAttr)
	{
		// mappedBy
		validateMappedBy(newAttr, newAttr.getMappedBy());

		// orderBy
		validateOrderBy(newAttr, newAttr.getOrderBy());
	}

	private static void validateUpdate(Attribute newAttr, Attribute currentAttr)
	{
		// data type
		AttributeType currentDataType = currentAttr.getDataType();
		AttributeType newDataType = newAttr.getDataType();
		if (!Objects.equals(currentDataType, newDataType))
		{
			validateUpdateDataType(currentDataType, newDataType);

			if (newAttr.isInversedBy())
			{
				throw new MolgenisDataException(
						format("Attribute data type change not allowed for bidirectional attribute [%s]",
								newAttr.getName()));
			}
		}

		// orderBy
		Sort currentOrderBy = currentAttr.getOrderBy();
		Sort newOrderBy = newAttr.getOrderBy();
		if (!Objects.equals(currentOrderBy, newOrderBy))
		{
			validateOrderBy(newAttr, newOrderBy);
		}

		// note: mappedBy is a readOnly attribute, no need to verify for updates
	}

	void validateDefaultValue(Attribute attr, boolean validateEntityReferences)
	{
		String value = attr.getDefaultValue();
		if (value != null)
		{
			if (attr.isUnique())
			{
				throw new MolgenisDataException("Unique attribute " + attr.getName() + " cannot have default value");
			}

			if (attr.getExpression() != null)
			{
				throw new MolgenisDataException("Computed attribute " + attr.getName() + " cannot have default value");
			}

			AttributeType fieldType = attr.getDataType();
			if (fieldType.getMaxLength() != null && value.length() > fieldType.getMaxLength())
			{
				throw new MolgenisDataException(
						"Default value for attribute [" + attr.getName() + "] exceeds the maximum length for datatype "
								+ attr.getDataType().name());
			}

			if (fieldType == AttributeType.EMAIL)
			{
				checkEmail(value);
			}

			if (fieldType == AttributeType.HYPERLINK)
			{
				checkHyperlink(value);
			}

			if (fieldType == AttributeType.ENUM)
			{
				checkEnum(attr, value);
			}

			// Get typed value to check if the value is of the right type.
			Object typedValue;
			try
			{
				typedValue = EntityUtils.getTypedValue(value, attr, entityManager);
			}
			catch (NumberFormatException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Invalid default value [%s] for data type [%s]", value, attr.getDataType())));
			}

			if (validateEntityReferences)
			{
				if (isSingleReferenceType(attr))
				{
					Entity refEntity = (Entity) typedValue;
					EntityType refEntityType = attr.getRefEntity();
					if (dataService.query(refEntityType.getId())
								   .eq(refEntityType.getIdAttribute().getName(), refEntity.getIdValue())
								   .count() == 0)
					{
						throw new MolgenisValidationException(new ConstraintViolation(
								format("Default value [%s] refers to an unknown entity", value)));
					}
				}
				else if (isMultipleReferenceType(attr))
				{
					Iterable<Entity> refEntitiesValue = (Iterable<Entity>) typedValue;
					EntityType refEntityType = attr.getRefEntity();
					if (dataService.query(refEntityType.getId())
								   .in(refEntityType.getIdAttribute().getName(),
										   StreamSupport.stream(refEntitiesValue.spliterator(), false)
														.map(Entity::getIdValue)
														.collect(toList()))
								   .count() < Iterables.size(refEntitiesValue))
					{
						throw new MolgenisValidationException(new ConstraintViolation(
								format("Default value [%s] refers to one or more unknown entities", value)));
					}
				}
			}
		}
	}

	private void checkEmail(String value)
	{
		if (!emailValidator.isValid(value, null))
		{
			throw new MolgenisDataException("Default value [" + value + "] is not a valid email address");
		}
	}

	private static void checkEnum(Attribute attr, String value)
	{
		if (value != null)
		{
			List<String> enumOptions = attr.getEnumOptions();

			if (!enumOptions.contains(value))
			{
				throw new MolgenisDataException(
						"Invalid default value [" + value + "] for enum [" + attr.getName() + "] value must be one of "
								+ enumOptions.toString());
			}
		}
	}

	private static void checkHyperlink(String value)
	{
		try
		{
			new URI(value);
		}
		catch (URISyntaxException e)
		{
			throw new MolgenisDataException("Default value [" + value + "] is not a valid hyperlink.");
		}
	}

	private static void validateName(Attribute attr)
	{
		// validate entity name (e.g. illegal characters, length)
		String name = attr.getName();
		if (!name.equals(ATTRIBUTE_META_DATA) && !name.equals(ENTITY_TYPE_META_DATA) && !name.equals(PACKAGE))
		{
			try
			{
				NameValidator.validateAttributeName(attr.getName());
			}
			catch (MolgenisDataException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(e.getMessage()));
			}
		}
	}

	/**
	 * Validate whether the mappedBy attribute is part of the referenced entity.
	 *
	 * @param attr         attribute
	 * @param mappedByAttr mappedBy attribute
	 * @throws MolgenisDataException if mappedBy is an attribute that is not part of the referenced entity
	 */
	private static void validateMappedBy(Attribute attr, Attribute mappedByAttr)
	{
		if (mappedByAttr != null)
		{
			if (!isSingleReferenceType(mappedByAttr))
			{
				throw new MolgenisDataException(
						format("Invalid mappedBy attribute [%s] data type [%s].", mappedByAttr.getName(),
								mappedByAttr.getDataType()));
			}

			Attribute refAttr = attr.getRefEntity().getAttribute(mappedByAttr.getName());
			if (refAttr == null)
			{
				throw new MolgenisDataException(
						format("mappedBy attribute [%s] is not part of entity [%s].", mappedByAttr.getName(),
								attr.getRefEntity().getId()));
			}
		}
	}

	/**
	 * Validate whether the attribute names defined by the orderBy attribute point to existing attributes in the
	 * referenced entity.
	 *
	 * @param attr    attribute
	 * @param orderBy orderBy of attribute
	 * @throws MolgenisDataException if orderBy contains attribute names that do not exist in the referenced entity.
	 */
	private static void validateOrderBy(Attribute attr, Sort orderBy)
	{
		if (orderBy != null)
		{
			EntityType refEntity = attr.getRefEntity();
			if (refEntity != null)
			{
				for (Sort.Order orderClause : orderBy)
				{
					String refAttrName = orderClause.getAttr();
					if (refEntity.getAttribute(refAttrName) == null)
					{
						throw new MolgenisDataException(
								format("Unknown entity [%s] attribute [%s] referred to by entity [%s] attribute [%s] sortBy [%s]",
										refEntity.getId(), refAttrName, attr.getEntityType().getId(), attr.getName(),
										orderBy.toSortString()));
					}
				}
			}
		}
	}

	private static void validateUpdateDataType(AttributeType currentDataType, AttributeType newDataType)
	{
		EnumSet<AttributeType> allowedDatatypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		if (!allowedDatatypes.contains(newDataType))
		{
			throw new MolgenisDataException(
					format("Attribute data type update from [%s] to [%s] not allowed, allowed types are %s",
							currentDataType.toString(), newDataType.toString(), allowedDatatypes.toString()));
		}
	}

	private static EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_ALLOWED_TRANSITIONS;

	static
	{
		// transitions to EMAIL and HYPERLINK not allowed because existing values can not be validated
		// transitions to CATEGORICAL_MREF and MREF not allowed because junction tables updated not implemented
		// transitions to FILE not allowed because associated file in FileStore not created/removed, see github issue https://github.com/molgenis/molgenis/issues/3217
		DATA_TYPE_ALLOWED_TRANSITIONS = new EnumMap<>(AttributeType.class);
		Set<AttributeType> allowedIdAttributeTypes = getValidIdAttributeTypes();

		// TRUE and FALSE can either be expressed in string or 0 and 1
		// Postgres does not support boolean to bigint or double precision (LONG and DECIMAL)
		DATA_TYPE_ALLOWED_TRANSITIONS.put(BOOL, EnumSet.of(STRING, TEXT, INT));

		// DATE and DATE_TIME can only be converted to STRING and TEXT types
		DATA_TYPE_ALLOWED_TRANSITIONS.put(DATE, EnumSet.of(STRING, TEXT, DATE_TIME));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(DATE_TIME, EnumSet.of(STRING, TEXT, DATE));

		DATA_TYPE_ALLOWED_TRANSITIONS.put(DECIMAL, EnumSet.of(STRING, TEXT, INT, LONG, ENUM));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(INT, EnumSet.of(STRING, TEXT, DECIMAL, LONG, BOOL, ENUM));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(LONG, EnumSet.of(STRING, TEXT, INT, DECIMAL, ENUM));

		// EMAIL and HYPERLINK can never be anything else then STRING or TEXT compatible
		DATA_TYPE_ALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(STRING, TEXT));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(STRING, TEXT));

		// If you have JS only in your HTML attribute, you can also change it to SCRIPT
		DATA_TYPE_ALLOWED_TRANSITIONS.put(HTML, EnumSet.of(STRING, TEXT, SCRIPT));

		// CATEGORICAL and XREF can be converted to all the allowed ID attribute types, and to eachother
		// EMAIL and HYPERLINK are excluded, we are unable to validate the format
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL, EnumSet.of(STRING, INT, LONG, XREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(XREF, EnumSet.of(STRING, INT, LONG, CATEGORICAL));

		// Allow transition between types that already have a junction table
		DATA_TYPE_ALLOWED_TRANSITIONS.put(MREF, EnumSet.of(CATEGORICAL_MREF));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.of(MREF));

		// SCRIPT is an algorithm, which can not be anything else then STRING or TEXT
		DATA_TYPE_ALLOWED_TRANSITIONS.put(SCRIPT, EnumSet.of(STRING, TEXT));

		DATA_TYPE_ALLOWED_TRANSITIONS.put(STRING,
				EnumSet.of(BOOL, DATE, DATE_TIME, DECIMAL, INT, LONG, HTML, SCRIPT, TEXT, ENUM, COMPOUND));

		DATA_TYPE_ALLOWED_TRANSITIONS.put(TEXT,
				EnumSet.of(BOOL, DATE, DATE_TIME, DECIMAL, INT, LONG, HTML, SCRIPT, STRING, ENUM, COMPOUND));

		DATA_TYPE_ALLOWED_TRANSITIONS.put(ENUM, EnumSet.of(STRING, INT, LONG, TEXT));

		// STRING only, because STRING can be converted to almost everything else
		DATA_TYPE_ALLOWED_TRANSITIONS.put(COMPOUND, EnumSet.of(STRING));

		// ONE_TO_MANY and FILE can never be anything else
		DATA_TYPE_ALLOWED_TRANSITIONS.put(ONE_TO_MANY, EnumSet.noneOf(AttributeType.class));
		DATA_TYPE_ALLOWED_TRANSITIONS.put(FILE, EnumSet.noneOf(AttributeType.class));

		// Excluded MREF and CATEGORICAL_MREF because transition to a type with a junction table is not possible at the moment
		EnumSet<AttributeType> referenceTypes = EnumSet.of(XREF, CATEGORICAL);

		// Every type that is listed as a valid ID attribute type is allowed to be converted to an XREF and CATEGORICAL
		DATA_TYPE_ALLOWED_TRANSITIONS.keySet()
									 .stream()
									 .filter(allowedIdAttributeTypes::contains)
									 .forEach(type -> DATA_TYPE_ALLOWED_TRANSITIONS.get(type).addAll(referenceTypes));
	}
}
