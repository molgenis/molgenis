package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.validation.constraint.AttributeConstraint;
import org.molgenis.data.validation.constraint.AttributeConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorAttribute
{
	private MessageGeneratorAttribute()
	{
	}

	static ConstraintViolationMessage createMessage(AttributeConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage constraintViolationMessage;

		AttributeConstraint constraint = constraintViolation.getConstraint();
		switch (constraint)
		{
			case COMPOUND_PARENT:
				constraintViolationMessage = createMessageCompoundParent("V81", constraintViolation);
				break;
			case DEFAULT_VALUE_NOT_UNIQUE:
				constraintViolationMessage = createMessageDefaultValueNotUnique("V95", constraintViolation);
				break;
			case DEFAULT_VALUE_NOT_COMPUTED:
				constraintViolationMessage = createMessageDefaultValueNotComputed("V96", constraintViolation);
				break;
			case DEFAULT_VALUE_EMAIL:
				constraintViolationMessage = createMessageDefaultValueEmail("V82", constraintViolation);
				break;
			case DEFAULT_VALUE_ENTITY_REFERENCE:
				if (EntityTypeUtils.isSingleReferenceType(constraintViolation.getAttribute()))
				{
					constraintViolationMessage = createMessageDefaultValueEntityReferenceXref("V83a",
							constraintViolation);
				}
				else
				{
					constraintViolationMessage = createMessageDefaultValueEntityReferenceMref("V83b",
							constraintViolation);
				}
				break;
			case DEFAULT_VALUE_ENUM:
				constraintViolationMessage = createMessageDefaultValueEnum("V84", constraintViolation);
				break;
			case DEFAULT_VALUE_HYPERLINK:
				constraintViolationMessage = createMessageDefaultValueHyperlink("V85", constraintViolation);
				break;
			case DEFAULT_VALUE_MAX_LENGTH:
				constraintViolationMessage = createMessageDefaultValueMaxLength("V86", constraintViolation);
				break;
			case DEFAULT_VALUE_TYPE:
				constraintViolationMessage = createMessageDefaultValueType("V87", constraintViolation);
				break;
			case MAPPED_BY_REFERENCE:
				constraintViolationMessage = createMessageMappedByReference("V88", constraintViolation);
				break;
			case MAPPED_BY_TYPE:
				constraintViolationMessage = createMessageMappedByType("V89", constraintViolation);
				break;
			case NAME: // TODO mention which characters are not allowed
				constraintViolationMessage = createMessageName("V90", constraintViolation);
				break;
			case NON_COMPOUND_CHILDREN:
				constraintViolationMessage = createMessageNonCompoundChildren("V91", constraintViolation);
				break;
			case ORDER_BY_REFERENCE: // TODO mention which part of sort is invalid
				constraintViolationMessage = createMessageOrderByReference("V92", constraintViolation);
				break;
			case TYPE_UPDATE_BIDIRECTIONAL: // TODO attribute update constraint instead of attribute constraint
				constraintViolationMessage = createMessageTypeUpdateBidirectional("V93", constraintViolation);
				break;
			case TYPE_UPDATE: // TODO implement (something like: Attribute data type update from [%s] to [%s] not allowed, allowed types are %s)
				constraintViolationMessage = createMessageTypeUpdate("V94", constraintViolation);
				break;
			default:
				throw new UnexpectedEnumException(constraint);
		}
		return constraintViolationMessage;
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageCompoundParent(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEmail(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessageDefaultValue(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueNotUnique(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueNotComputed(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEntityReferenceXref(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessageDefaultValue(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEntityReferenceMref(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessageDefaultValue(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueEnum(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(),
				attribute.getEnumOptions().stream().collect(Collectors.joining(","))).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueHyperlink(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessageDefaultValue(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueMaxLength(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getDataType().getMaxLength()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageDefaultValueType(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageMappedByReference(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getMappedBy().getLabel(),
				attribute.getRefEntity().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageMappedByType(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue(), attribute.getMappedBy().getLabel(),
				attribute.getMappedBy().getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageName(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageNonCompoundChildren(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageOrderByReference(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageTypeUpdateBidirectional(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, constraintViolation.getAttribute().getLabel()).orElse(
				message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageTypeUpdate(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static ConstraintViolationMessage createMessage(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel())
				.orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static ConstraintViolationMessage createMessageDefaultValue(String errorCode,
			AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(), attribute.getLabel(),
				attribute.getDefaultValue()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static String getMessage(AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();
		return String.format("constraint:%s entityType:%s attribute:%s", constraintViolation.getConstraint().name(),
				attribute.getEntity().getId(), attribute.getIdentifier());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}
}
