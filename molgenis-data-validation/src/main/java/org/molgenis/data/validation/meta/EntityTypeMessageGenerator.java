package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ValidationMessage;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Collection;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;
import static org.molgenis.data.validation.meta.EntityTypeConstraint.*;

public class EntityTypeMessageGenerator
{
	private EntityTypeMessageGenerator()
	{
	}

	public static Collection<? extends ValidationMessage> createMessages(
			EntityTypeValidationResult entityTypeValidationResult)
	{
		EntityType entityType = entityTypeValidationResult.getEntityType();
		return entityTypeValidationResult.getConstraintViolations()
										 .stream()
										 .map(entityTypeConstraint -> createMessage(entityType, entityTypeConstraint))
										 .collect(toList());
	}

	private static ValidationMessage createMessage(EntityType entityType,
			EntityTypeConstraint entityTypeConstraint)
	{
		ValidationMessage constraintViolationMessage;

		switch (entityTypeConstraint)
		{
			case LOOKUP_ATTRIBUTES_EXIST:
				constraintViolationMessage = createMessageLookupAttributesExist("V40", entityType);
				break;
			case LABEL_ATTRIBUTE_EXISTS:
				constraintViolationMessage = createMessageLabelAttributeExists("V41", entityType);
				break;
			case ID_ATTRIBUTE_EXISTS:
				constraintViolationMessage = createMessageIdAttributeExists("V42", entityType);
				break;
			case ID_ATTRIBUTE_TYPE:
				constraintViolationMessage = createMessageIdAttributeType("V43", entityType);
				break;
			case ID_ATTRIBUTE_UNIQUE:
				constraintViolationMessage = createMessageIdAttributeUnique("V44", entityType);
				break;
			case ID_ATTRIBUTE_NOT_NULL:
				constraintViolationMessage = createMessageIdAttributeNotNull("V45", entityType);
				break;
			case ID_ATTRIBUTE_REQUIRED:
				constraintViolationMessage = createMessageIdAttributeRequired("V46", entityType);
				break;
			case BACKEND_EXISTS:
				constraintViolationMessage = createMessageBackendExists("V47", entityType);
				break;
			case HAS_ATTRIBUTES:
				constraintViolationMessage = createMessageHasAttributes("V48", entityType);
				break;
			case ATTRIBUTES_UNIQUE:
				constraintViolationMessage = createMessageAttributesUnique("V49", entityType);
				break;
			case ATTRIBUTE_IN_PARENT:
				constraintViolationMessage = createMessageAttributeInParent("V50", entityType);
				break;
			case EXTENDS_NOT_ABSTRACT:
				constraintViolationMessage = createMessageExtendsNotAbstract("V51", entityType);
				break;
			case NAME:
				constraintViolationMessage = createMessageName("V52", entityType);
				break;
			case LABEL_NOT_EMPTY:
				constraintViolationMessage = createMessageLabelNotEmpty("V53", entityType);
				break;
			case LABEL_NOT_WHITESPACE_ONLY:
				constraintViolationMessage = createMessageLabelNotWhitespaceOnlyEmpty("V54", entityType);
				break;
			case PACKAGE_NOT_SYSTEM:
				constraintViolationMessage = createMessagePackageNotSystem("V55", entityType);
				break;
			default:
				throw new UnexpectedEnumException(entityTypeConstraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(EntityType entityType, EntityTypeConstraint entityTypeConstraint)
	{
		return String.format("constraint:%s entityType:%s", entityTypeConstraint.name(), entityType.getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageLookupAttributesExist(String errorCode,
			EntityType entityType)
	{
		return createMessageEntityTypeLabel(errorCode, entityType, EntityTypeConstraint.LOOKUP_ATTRIBUTES_EXIST);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageLabelAttributeExists(String errorCode, EntityType entityType)
	{
		String message = getMessage(entityType, EntityTypeConstraint.LABEL_ATTRIBUTE_EXISTS);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getOwnLabelAttribute().getLabel()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageIdAttributeExists(String errorCode, EntityType entityType)
	{
		return createMessageIdAttribute(errorCode, entityType, ID_ATTRIBUTE_EXISTS);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageIdAttributeType(String errorCode, EntityType entityType)
	{
		Attribute idAttribute = entityType.getOwnIdAttribute();

		String message = getMessage(entityType, ID_ATTRIBUTE_TYPE);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(), idAttribute.getLabel(),
				idAttribute.getDataType().toString()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageIdAttributeUnique(String errorCode, EntityType entityType)
	{
		return createMessageIdAttribute(errorCode, entityType, ID_ATTRIBUTE_UNIQUE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageIdAttributeNotNull(String errorCode, EntityType entityType)
	{
		return createMessageIdAttribute(errorCode, entityType, ID_ATTRIBUTE_NOT_NULL);
	}

	private static ValidationMessage createMessageIdAttribute(String errorCode, EntityType entityType,
			EntityTypeConstraint entityTypeConstraint)
	{
		String message = getMessage(entityType, entityTypeConstraint);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getOwnIdAttribute().getLabel()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageIdAttributeRequired(String errorCode, EntityType entityType)
	{
		return createMessageEntityTypeLabel(errorCode, entityType, ID_ATTRIBUTE_NOT_NULL);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageBackendExists(String errorCode, EntityType entityType)
	{
		String message = getMessage(entityType, BACKEND_EXISTS);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(), entityType.getBackend()).orElse(
				message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageHasAttributes(String errorCode, EntityType entityType)
	{
		return createMessageEntityTypeLabel(errorCode, entityType, HAS_ATTRIBUTES);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageAttributesUnique(String errorCode, EntityType entityType)
	{
		// TODO entity attribute constraint: Entity [%s] contains multiple attributes with name [%s]
		return createMessageEntityTypeLabel(errorCode, entityType, ATTRIBUTES_UNIQUE);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageAttributeInParent(String errorCode, EntityType entityType)
	{
		// TODO entity attribute constraint: An attribute with name [%s] already exists in entity [%s] or one of its parents
		return createMessageEntityTypeLabel(errorCode, entityType, ATTRIBUTE_IN_PARENT);
	}

	@SuppressWarnings({ "SameParameterValue", "ConstantConditions" })
	private static ValidationMessage createMessageExtendsNotAbstract(String errorCode, EntityType entityType)
	{
		String message = getMessage(entityType, EXTENDS_NOT_ABSTRACT);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getExtends().getLabel()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageName(String errorCode, EntityType entityType)
	{
		// TODO improve message such that NameValidator.validateEntityName messages are returned
		String message = getMessage(entityType, NAME);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(), entityType.getId()).orElse(
				message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageLabelNotEmpty(String errorCode, EntityType entityType)
	{
		return createMessageEntityTypeLabel(errorCode, entityType, LABEL_NOT_EMPTY);
	}

	@SuppressWarnings("SameParameterValue")
	private static ValidationMessage createMessageLabelNotWhitespaceOnlyEmpty(String errorCode,
			EntityType entityType)
	{
		return createMessageEntityTypeLabel(errorCode, entityType, LABEL_NOT_WHITESPACE_ONLY);
	}

	@SuppressWarnings({ "SameParameterValue", "ConstantConditions" })
	private static ValidationMessage createMessagePackageNotSystem(String errorCode, EntityType entityType)
	{
		String message = getMessage(entityType, PACKAGE_NOT_SYSTEM);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel(),
				entityType.getPackage().getLabel()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	private static ValidationMessage createMessageEntityTypeLabel(String errorCode, EntityType entityType,
			EntityTypeConstraint entityTypeConstraint)
	{
		String message = getMessage(entityType, entityTypeConstraint);
		String localizedMessage = getLocalizedMessage(errorCode, entityType.getLabel()).orElse(message);
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}
}
