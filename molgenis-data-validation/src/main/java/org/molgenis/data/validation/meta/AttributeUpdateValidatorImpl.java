package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.support.AttributeUtils.getValidIdAttributeTypes;

public class AttributeUpdateValidatorImpl implements AttributeUpdateValidator
{
	@Override
	public AttributeUpdateValidationResult validate(Attribute attribute, Attribute updatedAttribute)
	{
		EnumSet<AttributeUpdateConstraint> violatedConstraints = EnumSet.noneOf(AttributeUpdateConstraint.class);
		if (attribute.getDataType() != updatedAttribute.getDataType())
		{
			if (!isValidDataTypeUpdate(attribute, updatedAttribute))
			{
				violatedConstraints.add(AttributeUpdateConstraint.TYPE_UPDATE);
			}
			if (updatedAttribute.isInversedBy())
			{
				violatedConstraints.add(AttributeUpdateConstraint.TYPE_UPDATE_BIDIRECTIONAL);
			}
		}
		return AttributeUpdateValidationResult.create(attribute, updatedAttribute, violatedConstraints);
	}

	private boolean isValidDataTypeUpdate(Attribute attribute, Attribute updatedAttribute)
	{
		AttributeType currentDataType = attribute.getDataType();
		AttributeType newDataType = updatedAttribute.getDataType();

		EnumSet<AttributeType> allowedDatatypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		return allowedDatatypes.contains(newDataType);
	}

	private static final EnumMap<AttributeType, EnumSet<AttributeType>> DATA_TYPE_ALLOWED_TRANSITIONS;

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
