package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.validation.constraint.AttributeConstraint;
import org.molgenis.data.validation.constraint.AttributeValidationResult;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorAttribute
{
	private MessageGeneratorAttribute()
	{
	}

	public static Collection<? extends ConstraintViolationMessage> createMessages(
			AttributeValidationResult attributeValidationResult)
	{
		Attribute attribute = attributeValidationResult.getAttribute();
		return attributeValidationResult.getConstraintViolations()
										.stream()
										.map(attributeConstraint -> createMessage(attribute, attributeConstraint))
										.collect(toList());
	}

	static ConstraintViolationMessage createMessage(Attribute attribute, AttributeConstraint attributeConstraint)
	{
		ConstraintViolationMessage constraintViolationMessage;

		switch (attributeConstraint)
		{
			case COMPOUND_PARENT:
				constraintViolationMessage = createMessageCompoundParent("V81", attribute);
				break;
			case DEFAULT_VALUE_NOT_UNIQUE:
				constraintViolationMessage = createMessageDefaultValueNotUnique("V95", attribute);
				break;
			case DEFAULT_VALUE_NOT_COMPUTED:
				constraintViolationMessage = createMessageDefaultValueNotComputed("V96", attribute);
				break;
			case DEFAULT_VALUE_EMAIL:
				constraintViolationMessage = createMessageDefaultValueEmail("V82", attribute);
				break;
			case DEFAULT_VALUE_ENTITY_REFERENCE:
				if (EntityTypeUtils.isSingleReferenceType(attribute))
				{
					constraintViolationMessage = createMessageDefaultValueEntityReferenceXref("V83a", attribute);
				}
				else
				{
					constraintViolationMessage = createMessageDefaultValueEntityReferenceMref("V83b", attribute);
				}
				break;
			case DEFAULT_VALUE_ENUM:
				constraintViolationMessage = createMessageDefaultValueEnum("V84", attribute);
				break;
			case DEFAULT_VALUE_HYPERLINK:
				constraintViolationMessage = createMessageDefaultValueHyperlink("V85", attribute);
				break;
			case DEFAULT_VALUE_MAX_LENGTH:
				constraintViolationMessage = createMessageDefaultValueMaxLength("V86", attribute);
				break;
			case DEFAULT_VALUE_TYPE:
				constraintViolationMessage = createMessageDefaultValueType("V87", attribute);
				break;
			case MAPPED_BY_REFERENCE:
				constraintViolationMessage = createMessageMappedByReference("V88", attribute);
				break;
			case MAPPED_BY_TYPE:
				constraintViolationMessage = createMessageMappedByType("V89", attribute);
				break;
			case NAME: // TODO mention which characters are not allowed
				constraintViolationMessage = createMessageName("V90", attribute);
				break;
			case NON_COMPOUND_CHILDREN:
				constraintViolationMessage = createMessageNonCompoundChildren("V91", attribute);
				break;
			case ORDER_BY_REFERENCE: // TODO mention which part of sort is invalid
				constraintViolationMessage = createMessageOrderByReference("V92", attribute);
				break;
			case TYPE_UPDATE_BIDIRECTIONAL: // TODO attribute update constraint instead of attribute constraint
				constraintViolationMessage = createMessageTypeUpdateBidirectional("V93", attribute);
				break;
			case TYPE_UPDATE: // TODO implement (something like: Attribute data type update from [%s] to [%s] not allowed, allowed types are %s)
				constraintViolationMessage = createMessageTypeUpdate("V94", attribute);
				break;
			default:
				throw new UnexpectedEnumException(attributeConstraint);
		}
		return constraintViolationMessage;
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageCompoundParent(String errorCode, Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.COMPOUND_PARENT);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEmail(String errorCode, Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_EMAIL);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueNotUnique(String errorCode, Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_NOT_UNIQUE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueNotComputed(String errorCode,
			Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_NOT_COMPUTED);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEntityReferenceXref(String errorCode,
			Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_ENTITY_REFERENCE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEntityReferenceMref(String errorCode,
			Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_ENTITY_REFERENCE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEnum(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.DEFAULT_VALUE_ENUM);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(),
				attribute.getEnumOptions().stream().collect(Collectors.joining(","))).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueHyperlink(String errorCode, Attribute attribute)
	{
		return createMessageDefaultValue(errorCode, attribute, AttributeConstraint.DEFAULT_VALUE_HYPERLINK);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueMaxLength(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.DEFAULT_VALUE_MAX_LENGTH);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getDataType().getMaxLength()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueType(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.DEFAULT_VALUE_TYPE);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageMappedByReference(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.MAPPED_BY_REFERENCE);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getMappedBy().getLabel(),
				attribute.getRefEntity().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageMappedByType(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.MAPPED_BY_TYPE);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getMappedBy().getLabel(),
				attribute.getMappedBy().getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageName(String errorCode, Attribute attribute)
	{
		return createMessage(errorCode, attribute, AttributeConstraint.NAME);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageNonCompoundChildren(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.NON_COMPOUND_CHILDREN);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageOrderByReference(String errorCode, Attribute attribute)
	{
		return createMessage(errorCode, attribute, AttributeConstraint.ORDER_BY_REFERENCE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageTypeUpdateBidirectional(String errorCode,
			Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.TYPE_UPDATE_BIDIRECTIONAL);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageTypeUpdate(String errorCode, Attribute attribute)
	{
		String message = getMessage(attribute, AttributeConstraint.TYPE_UPDATE);
		String localizedMessage = getLocalizedMessage(errorCode).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static ConstraintViolationMessage createMessage(String errorCode, Attribute attribute,
			AttributeConstraint attributeConstraint)
	{
		String message = getMessage(attribute, attributeConstraint);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel())
				.orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static ConstraintViolationMessage createMessageDefaultValue(String errorCode, Attribute attribute,
			AttributeConstraint attributeConstraint)
	{
		String message = getMessage(attribute, attributeConstraint);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static String getMessage(Attribute attribute, AttributeConstraint attributeConstraint)
	{
		return String.format("constraint:%s entityType:%s attribute:%s", attributeConstraint.name(),
				attribute.getEntity().getId(), attribute.getIdentifier());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}
}
