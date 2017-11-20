package org.molgenis.data.validation.message;

import org.molgenis.data.AttributeValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.constraint.AttributeValueConstraint;
import org.molgenis.data.validation.constraint.AttributeValueConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorAttributeValue
{
	private MessageGeneratorAttributeValue()
	{
	}

	static ConstraintViolationMessage createMessage(AttributeValueConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage constraintViolationMessage;

		AttributeValueConstraint constraint = constraintViolation.getConstraint();
		switch (constraint)
		{
			case EMAIL:
				constraintViolationMessage = createMessageEmail("V70", constraintViolation);
				break;
			case ENTITY_REFERENCE:
				constraintViolationMessage = createMessageEntityReference("V71", constraintViolation);
				break;
			case ENUM:
				constraintViolationMessage = createMessageEnum("V72", constraintViolation);
				break;
			case EXPRESSION:
				constraintViolationMessage = createMessageExpression("V73", constraintViolation);
				break;
			case HYPERLINK:
				constraintViolationMessage = createMessageHyperlink("V74", constraintViolation);
				break;
			case MAX_LENGTH:
				constraintViolationMessage = createMessageMaxLength("V75", constraintViolation);
				break;
			case NOT_NULL:
				constraintViolationMessage = createMessageNotNull("V76", constraintViolation);
				break;
			case RANGE:
				constraintViolationMessage = createMessageRange("V77", constraintViolation);
				break;
			case READ_ONLY:
				constraintViolationMessage = createMessageReadOnly("V78", constraintViolation);
				break;
			case TYPE:
				constraintViolationMessage = createMessageType("V79", constraintViolation);
				break;
			case DATE:
				constraintViolationMessage = createMessageType("V79b", constraintViolation);
				break;
			case DATETIME:
				constraintViolationMessage = createMessageType("V79c", constraintViolation);
				break;
			case UNIQUE:
				constraintViolationMessage = createMessageUnique("V80", constraintViolation);
				break;
			default:
				throw new UnexpectedEnumException(constraint);
		}
		return constraintViolationMessage;
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageUnique(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageType(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageReadOnly(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(),
				attribute.getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageRange(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(),
				attribute.getLabel(), attributeValue.getValue().toString(),
				attribute.getRangeMin(), attribute.getRangeMax()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageNotNull(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageMaxLength(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(),
				attribute.getLabel(), attributeValue.getValue().toString(),
				attribute.getDataType().getMaxLength()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageHyperlink(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageExpression(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageEnum(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(),
				attribute.getLabel(), attributeValue.getValue().toString(),
				attribute.getEnumOptions().stream().collect(Collectors.joining(","))).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageEntityReference(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageEmail(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		return createMessage(errorCode, constraintViolation);
	}

	private static ConstraintViolationMessage createMessage(String errorCode,
			AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, attribute.getEntity().getLabel(),
				attribute.getLabel(), attributeValue.getValue().toString()).orElse(
				message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static String getMessage(AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();
		return String.format("constraint:%s entityType:%s attribute:%s value:%s",
				constraintViolation.getConstraint().name(), attribute.getEntity().getId(), attribute.getIdentifier(),
				attributeValue.getValue().toString());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}
}
