package org.molgenis.data.validation.meta;

import org.molgenis.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.meta.NameValidator.validateName;
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
	private final DataService dataService;

	@Autowired
	public AttributeValidator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public void validate(Attribute attr)
	{
		validateAttributeName(attr);
		validateAttributeDefaultValue(attr);

		Attribute currentAttr = dataService.findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(), Attribute.class);
		if (currentAttr == null)
		{
			validateAdd(attr);
		}
		else
		{
			validateUpdate(attr, currentAttr);
		}
	}

	private static void validateAttributeDefaultValue(Attribute attr)
	{
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
		}
	}

	private void validateAdd(Attribute newAttr)
	{
		// mappedBy
		validateMappedBy(newAttr, newAttr.getMappedBy());

		// orderBy
		validateOrderBy(newAttr, newAttr.getOrderBy());
	}

	private void validateUpdate(Attribute newAttr, Attribute currentAttr)
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

		// expression
		String currentExpression = currentAttr.getExpression();
		String newExpression = newAttr.getExpression();
		if (!Objects.equals(currentExpression, newExpression))
		{
			validateUpdateExpression(currentExpression, newExpression);
		}

		// validation expression
		String currentValidationExpression = currentAttr.getValidationExpression();
		String newValidationExpression = newAttr.getValidationExpression();
		if (!Objects.equals(currentValidationExpression, newValidationExpression))
		{
			validateUpdateExpression(currentValidationExpression, newValidationExpression);
		}

		// visible expression
		String currentVisibleExpression = currentAttr.getVisibleExpression();
		String newVisibleExpression = newAttr.getVisibleExpression();
		if (!Objects.equals(currentVisibleExpression, newVisibleExpression))
		{
			validateUpdateExpression(currentVisibleExpression, newVisibleExpression);
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

	private static void validateAttributeName(Attribute attr)
	{
		// validate entity name (e.g. illegal characters, length)
		String name = attr.getName();
		if (!name.equals(ATTRIBUTE_META_DATA) && !name.equals(ENTITY_TYPE_META_DATA) && !name.equals(PACKAGE))
		{
			try
			{
				validateName(attr.getName());
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

	private void validateUpdateExpression(String currentExpression, String newExpression)
	{
		// TODO validate with script evaluator

		// how to get access to expression validator here since it is located in molgenis-data-validation?
	}
}
