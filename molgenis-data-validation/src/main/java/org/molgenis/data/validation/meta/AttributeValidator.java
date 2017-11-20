package org.molgenis.data.validation.meta;

import com.google.common.collect.Iterables;
import org.hibernate.validator.constraints.impl.EmailValidator;
import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.AttributeConstraintViolation;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.support.AttributeUtils.getValidIdAttributeTypes;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.data.validation.constraint.AttributeConstraint.*;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.UPDATE;

/**
 * {@link Attribute} validator.
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

	AttributeValidator(DataService dataService, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.emailValidator = new EmailValidator();
	}

	public Collection<AttributeConstraintViolation> validate(Attribute attr, ValidationMode validationMode)
	{
		Set<AttributeConstraintViolation> constraintViolations = new HashSet<>();

		boolean validateDefaultValueEntityReferences = validationMode == ADD || validationMode == UPDATE;

		validateName(attr).forEach(constraintViolations::add);
		validateDefaultValue(attr, validateDefaultValueEntityReferences).forEach(constraintViolations::add);
		validateParent(attr).ifPresent(constraintViolations::add);
		validateChildren(attr).ifPresent(constraintViolations::add);

		switch (validationMode)
		{
			case ADD:
			case ADD_SKIP_ENTITY_VALIDATION:
				validateAdd(attr).forEach(constraintViolations::add);
				break;
			case UPDATE:
			case UPDATE_SKIP_ENTITY_VALIDATION:
				Attribute currentAttr = dataService.findOneById(ATTRIBUTE_META_DATA, attr.getIdentifier(),
						Attribute.class);
				validateUpdate(attr, currentAttr).forEach(constraintViolations::add);
				break;
			default:
				throw new UnexpectedEnumException(validationMode);
		}

		return constraintViolations;
	}

	private static Optional<AttributeConstraintViolation> validateParent(Attribute attr)
	{
		AttributeConstraintViolation constraintViolation;
		if (attr.getParent() != null && attr.getParent().getDataType() != COMPOUND)
		{
			constraintViolation = new AttributeConstraintViolation(COMPOUND_PARENT, attr);
		}
		else
		{
			constraintViolation = null;
		}
		return Optional.ofNullable(constraintViolation);
	}

	private static Optional<AttributeConstraintViolation> validateChildren(Attribute attr)
	{
		AttributeConstraintViolation constraintViolation;

		boolean childrenIsNullOrEmpty = attr.getChildren() == null || Iterables.isEmpty(attr.getChildren());
		if (!childrenIsNullOrEmpty && attr.getDataType() != COMPOUND)
		{
			constraintViolation = new AttributeConstraintViolation(NON_COMPOUND_CHILDREN, attr);
		}
		else
		{
			constraintViolation = null;
		}
		return Optional.ofNullable(constraintViolation);
	}

	private static Stream<AttributeConstraintViolation> validateAdd(Attribute newAttr)
	{
		List<AttributeConstraintViolation> constraintViolations = new ArrayList<>();
		validateMappedBy(newAttr, newAttr.getMappedBy()).ifPresent(constraintViolations::add);
		validateOrderBy(newAttr, newAttr.getOrderBy()).forEach(constraintViolations::add);
		return constraintViolations.stream();
	}

	private static Stream<AttributeConstraintViolation> validateUpdate(Attribute newAttr, Attribute currentAttr)
	{
		List<AttributeConstraintViolation> constraintViolations = new ArrayList<>();

		// data type
		AttributeType currentDataType = currentAttr.getDataType();
		AttributeType newDataType = newAttr.getDataType();
		if (!Objects.equals(currentDataType, newDataType))
		{
			validateUpdateDataType(currentAttr, newAttr).ifPresent(constraintViolations::add);

			if (newAttr.isInversedBy())
			{
				constraintViolations.add(new AttributeConstraintViolation(TYPE_UPDATE_BIDIRECTIONAL, newAttr));
			}
		}

		// orderBy
		Sort currentOrderBy = currentAttr.getOrderBy();
		Sort newOrderBy = newAttr.getOrderBy();
		if (!Objects.equals(currentOrderBy, newOrderBy))
		{
			validateOrderBy(newAttr, newOrderBy).forEach(constraintViolations::add);
		}

		// note: mappedBy is a readOnly attribute, no need to verify for updates
		return constraintViolations.stream();
	}

	Stream<AttributeConstraintViolation> validateDefaultValue(Attribute attr, boolean validateEntityReferences)
	{

		String value = attr.getDefaultValue();
		if (value != null)
		{
			List<AttributeConstraintViolation> constraintViolations = new ArrayList<>();

			if (attr.isUnique())
			{
				constraintViolations.add(new AttributeConstraintViolation(DEFAULT_VALUE_NOT_UNIQUE, attr));
			}

			if (attr.getExpression() != null)
			{
				constraintViolations.add(new AttributeConstraintViolation(DEFAULT_VALUE_NOT_COMPUTED, attr));
			}

			AttributeType fieldType = attr.getDataType();
			if (fieldType.getMaxLength() != null && value.length() > fieldType.getMaxLength())
			{
				constraintViolations.add(new AttributeConstraintViolation(DEFAULT_VALUE_MAX_LENGTH, attr));
			}

			if (fieldType == AttributeType.EMAIL)
			{
				validateEmail(attr).ifPresent(constraintViolations::add);
			}

			if (fieldType == AttributeType.HYPERLINK)
			{
				validateHyperlink(attr).ifPresent(constraintViolations::add);
			}

			if (fieldType == AttributeType.ENUM)
			{
				validateEnum(attr, value).ifPresent(constraintViolations::add);
			}

			// Get typed value to check if the value is of the right type.
			Object typedValue;
			boolean typeValid;
			try
			{
				typedValue = EntityUtils.getTypedValue(value, attr, entityManager);
				typeValid = true;

			}
			catch (Exception e)
			{
				constraintViolations.add(new AttributeConstraintViolation(DEFAULT_VALUE_TYPE, attr));
				typedValue = null;
				typeValid = false;
			}

			if (typeValid && validateEntityReferences)
			{
				if (isSingleReferenceType(attr))
				{
					Entity refEntity = (Entity) typedValue;
					EntityType refEntityType = attr.getRefEntity();
					if (dataService.query(refEntityType.getId())
								   .eq(refEntityType.getIdAttribute().getName(), refEntity.getIdValue())
								   .count() == 0)
					{
						constraintViolations.add(
								new AttributeConstraintViolation(DEFAULT_VALUE_ENTITY_REFERENCE, attr));
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
						constraintViolations.add(
								new AttributeConstraintViolation(DEFAULT_VALUE_ENTITY_REFERENCE, attr));
					}
				}
			}

			return constraintViolations.stream();
		}
		return Stream.empty();
	}

	private Optional<AttributeConstraintViolation> validateEmail(Attribute attribute)
	{
		if (!emailValidator.isValid(attribute.getDefaultValue(), null))
		{
			return Optional.of(new AttributeConstraintViolation(DEFAULT_VALUE_EMAIL, attribute));
		}
		return Optional.empty();
	}

	private static Optional<AttributeConstraintViolation> validateEnum(Attribute attr, String value)
	{
		if (value != null)
		{
			List<String> enumOptions = attr.getEnumOptions();

			if (!enumOptions.contains(value))
			{
				return Optional.of(new AttributeConstraintViolation(DEFAULT_VALUE_ENUM, attr));
			}
		}
		return Optional.empty();
	}

	private static Optional<AttributeConstraintViolation> validateHyperlink(Attribute attr)
	{
		try
		{
			new URI(attr.getDefaultValue());
		}
		catch (URISyntaxException e)
		{

			return Optional.of(new AttributeConstraintViolation(DEFAULT_VALUE_HYPERLINK, attr));
		}
		return Optional.empty();
	}

	private static Stream<AttributeConstraintViolation> validateName(Attribute attr)
	{
		// validate entity name (e.g. illegal characters, length)
		String name = attr.getName();
		if (!name.equals(ATTRIBUTE_META_DATA) && !name.equals(ENTITY_TYPE_META_DATA) && !name.equals(PACKAGE))
		{
			try
			{
				NameValidator.validateAttributeName(
						attr.getName()); // TODO name validator should return constraint violation
			}
			catch (MolgenisDataException e)
			{
				return Stream.of(new AttributeConstraintViolation(NAME, attr));
			}
		}
		return Stream.empty();
	}

	/**
	 * Validate whether the mappedBy attribute is part of the referenced entity.
	 *
	 * @param attr         attribute
	 * @param mappedByAttr mappedBy attribute
	 * @throws MolgenisDataException if mappedBy is an attribute that is not part of the referenced entity
	 */
	private static Optional<AttributeConstraintViolation> validateMappedBy(Attribute attr, Attribute mappedByAttr)
	{
		if (mappedByAttr != null)
		{
			if (!isSingleReferenceType(mappedByAttr))
			{
				return Optional.of(new AttributeConstraintViolation(MAPPED_BY_TYPE, attr));
			}
			else
			{
				Attribute refAttr = attr.getRefEntity().getAttribute(mappedByAttr.getName());
				if (refAttr == null)
				{
					return Optional.of(new AttributeConstraintViolation(MAPPED_BY_REFERENCE, attr));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Validate whether the attribute names defined by the orderBy attribute point to existing attributes in the
	 * referenced entity.
	 *
	 * @param attr    attribute
	 * @param orderBy orderBy of attribute
	 */
	private static Stream<AttributeConstraintViolation> validateOrderBy(Attribute attr, Sort orderBy)
	{
		if (orderBy != null)
		{
			EntityType refEntity = attr.getRefEntity();
			if (refEntity != null)
			{
				List<AttributeConstraintViolation> constraintViolations = new ArrayList<>();
				for (Sort.Order orderClause : orderBy)
				{
					String refAttrName = orderClause.getAttr();
					if (refEntity.getAttribute(refAttrName) == null)
					{
						constraintViolations.add(new AttributeConstraintViolation(ORDER_BY_REFERENCE, attr));
					}
				}
				return constraintViolations.stream();
			}
		}
		return Stream.empty();
	}

	private static Optional<AttributeConstraintViolation> validateUpdateDataType(Attribute currentAttribute,
			Attribute newAttribute)
	{
		AttributeType currentDataType = currentAttribute.getDataType();
		AttributeType newDataType = newAttribute.getDataType();

		EnumSet<AttributeType> allowedDatatypes = DATA_TYPE_ALLOWED_TRANSITIONS.get(currentDataType);
		if (!allowedDatatypes.contains(newDataType))
		{
			return Optional.of(new AttributeConstraintViolation(TYPE_UPDATE, newAttribute));
		}
		else
		{
			return Optional.empty();
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
