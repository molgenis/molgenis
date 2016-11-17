package org.molgenis.data.validation.meta;

import com.google.common.collect.Iterables;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;

/**
 * Attribute metadata validator
 */
@Component
public class AttributeValidator
{
	public enum ValidationMode
	{
		ADD, UPDATE
	}

	private final DataService dataService;
	private final EntityManager entityManager;
	private final EmailValidator emailValidator;

	@Autowired
	public AttributeValidator(DataService dataService, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.emailValidator = new EmailValidator();
	}

	public void validate(Attribute attr, ValidationMode validationMode)
	{
		validateName(attr);
		validateDefaultValue(attr);
		validateParent(attr);
		validateChildren(attr);

		switch (validationMode)
		{
			case ADD:
				validateAdd(attr);
				break;
			case UPDATE:
				Attribute currentAttr = dataService
						.findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
				validateUpdate(attr, currentAttr);
				break;
			default:
				throw new RuntimeException(format("Unknown attribute validation mode [%s]", validationMode.toString()));
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

	void validateDefaultValue(Attribute attr)
	{
		String value = attr.getDefaultValue();
		if (attr.getDefaultValue() != null)
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
			if (fieldType == AttributeType.XREF || fieldType == AttributeType.MREF)
			{
				throw new MolgenisDataException("Attribute " + attr.getName()
						+ " cannot have default value since specifying a default value for XREF and MREF data types is not yet supported.");
			}

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
			try
			{
				EntityUtils.getTypedValue(value, attr, entityManager);
			}
			catch (NumberFormatException e)
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						format("Invalid default value [%s] for data type [%s]", value, attr.getDataType())
				));
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
				NameValidator.validateName(attr.getName());
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
								attr.getRefEntity().getName()));
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
										refEntity.getName(), refAttrName, attr.getEntityType().getName(),
										attr.getName(), orderBy.toSortString()));
					}
				}
			}
		}
	}

	private static void validateUpdateDataType(AttributeType currentDataType, AttributeType newDataType)
	{
		EnumSet<AttributeType> disallowedDataTypes = DATA_TYPE_DISALLOWED_TRANSITIONS.get(currentDataType);
		if (disallowedDataTypes.contains(newDataType))
		{
			throw new MolgenisDataException(
					format("Attribute data type update from [%s] to [%s] not allowed, allowed types are %s",
							currentDataType.toString(), newDataType.toString(),
							EnumSet.complementOf(disallowedDataTypes).toString()));
		}
	}

	private static EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_DISALLOWED_TRANSITIONS;

	static
	{
		// transitions to EMAIL and HYPERLINK not allowed because existing values not checked by PostgreSQL
		// transitions to CATEGORICAL_MREF and MREF not allowed because junction tables updated not implemented
		// transitions to FILE not allowed because associated file in FileStore not created/removed
		DATA_TYPE_DISALLOWED_TRANSITIONS = new EnumMap<>(AttributeType.class);
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(BOOL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(CATEGORICAL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(CATEGORICAL_MREF, EnumSet.complementOf(EnumSet.of(MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(COMPOUND, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DATE, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DATE_TIME, EnumSet.of(CATEGORICAL_MREF, ONE_TO_MANY, MREF, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(DECIMAL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(EMAIL, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(ENUM, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(FILE, EnumSet.allOf(AttributeType.class));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(HTML, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(HYPERLINK, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(INT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(LONG, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(MREF, EnumSet.complementOf(EnumSet.of(CATEGORICAL_MREF)));
		DATA_TYPE_DISALLOWED_TRANSITIONS.put(ONE_TO_MANY, EnumSet.allOf(AttributeType.class));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(SCRIPT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(STRING, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(TEXT, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
		DATA_TYPE_DISALLOWED_TRANSITIONS
				.put(XREF, EnumSet.of(CATEGORICAL_MREF, MREF, ONE_TO_MANY, EMAIL, HYPERLINK, FILE));
	}
}
