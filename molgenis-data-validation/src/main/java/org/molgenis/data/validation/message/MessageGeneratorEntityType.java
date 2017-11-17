package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.EntityTypeConstraint;
import org.molgenis.data.validation.constraint.EntityTypeConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorEntityType
{
	private MessageGeneratorEntityType()
	{
	}

	static ConstraintViolationMessage createMessage(EntityTypeConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage constraintViolationMessage;

		EntityTypeConstraint constraint = constraintViolation.getConstraint();
		switch (constraint)
		{
			case LOOKUP_ATTRIBUTES_EXIST:
				constraintViolationMessage = createMessageLookupAttributesExist("V40", constraintViolation);
				break;
			case LABEL_ATTRIBUTE_EXISTS:
				constraintViolationMessage = createMessageLabelAttributeExists("V41", constraintViolation);
				break;
			case ID_ATTRIBUTE_EXISTS:
				constraintViolationMessage = createMessageIdAttributeExists("V42", constraintViolation);
				break;
			case ID_ATTRIBUTE_TYPE:
				constraintViolationMessage = createMessageIdAttributeType("V43", constraintViolation);
				break;
			case ID_ATTRIBUTE_UNIQUE:
				constraintViolationMessage = createMessageIdAttributeUnique("V44", constraintViolation);
				break;
			case ID_ATTRIBUTE_NOT_NULL:
				constraintViolationMessage = createMessageIdAttributeNotNull("V45", constraintViolation);
				break;
			case ID_ATTRIBUTE_REQUIRED:
				constraintViolationMessage = createMessageIdAttributeRequired("V46", constraintViolation);
				break;
			case BACKEND_EXISTS:
				constraintViolationMessage = createMessageBackendExists("V47", constraintViolation);
				break;
			case HAS_ATTRIBUTES:
				constraintViolationMessage = createMessageHasAttributes("V48", constraintViolation);
				break;
			case ATTRIBUTES_UNIQUE: // TODO entity attribute constraint: Entity [%s] contains multiple attributes with name [%s]
				constraintViolationMessage = createMessageAttributesUnique("V49", constraintViolation);
				break;
			case ATTRIBUTE_IN_PARENT: // TODO entity attribute constraint: An attribute with name [%s] already exists in entity [%s] or one of its parents
				constraintViolationMessage = createMessageAttributeInParent("V50", constraintViolation);
				break;
			case EXTENDS_NOT_ABSTRACT:
				constraintViolationMessage = createMessageExtendsNotAbstract("V51", constraintViolation);
				break;
			case NAME: // TODO improve message such that NameValidator.validateEntityName messages are returned
				constraintViolationMessage = createMessageName("V52", constraintViolation);
				break;
			case LABEL_NOT_EMPTY:
				constraintViolationMessage = createMessageLabelNotEmpty("V53", constraintViolation);
				break;
			case LABEL_NOT_WHITESPACE_ONLY:
				constraintViolationMessage = createMessageLabelNotWhitespaceOnlyEmpty("V54", constraintViolation);
				break;
			case PACKAGE_NOT_SYSTEM:
				constraintViolationMessage = createMessagePackageNotSystem("V55", constraintViolation);
				break;
			default:
				throw new UnexpectedEnumException(constraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(EntityTypeConstraintViolation constraintViolation)
	{
		return String.format("constraint:%s entityType:%s", constraintViolation.getConstraint().name(),
				constraintViolation.getEntityType().getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageLookupAttributesExist(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageLabelAttributeExists(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode,
				constraintViolation.getEntityType().getLabelAttribute().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageIdAttributeExists(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageIdAttribute(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageIdAttributeType(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();
		Attribute idAttribute = entityType.getIdAttribute();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(), idAttribute.getLabel(),
				idAttribute.getDataType().toString()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageIdAttributeUnique(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageIdAttribute(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageIdAttributeNotNull(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageIdAttribute(errorCode, constraintViolation);
	}

	private static ConstraintViolationMessage createMessageIdAttribute(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getIdAttribute().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageIdAttributeRequired(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageBackendExists(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getBackend()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageHasAttributes(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageAttributesUnique(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageAttributeInParent(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings({ "SameParameterValue", "ConstantConditions" })
	private static ConstraintViolationMessage createMessageExtendsNotAbstract(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getExtends().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageName(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageLabelNotEmpty(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageLabelNotWhitespaceOnlyEmpty(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		return createMessageEntityTypeLabel(errorCode, constraintViolation);
	}

	@SuppressWarnings({ "SameParameterValue", "ConstantConditions" })
	private static ConstraintViolationMessage createMessagePackageNotSystem(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getPackage().getLabel()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}

	private static ConstraintViolationMessage createMessageEntityTypeLabel(String errorCode,
			EntityTypeConstraintViolation constraintViolation)
	{
		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, constraintViolation.getEntityType().getLabel()).orElse(
				message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
