package org.molgenis.data.validation.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ValidationMessage;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class DefaultValueReferenceMessageGenerator
{
	private DefaultValueReferenceMessageGenerator()
	{
	}

	public static ValidationMessage createMessage(DefaultValueReferenceValidationResult constraintViolation)
	{
		ValidationMessage constraintViolationMessage;

		DefaultValueReferenceConstraint defaultValueReferenceConstraint = constraintViolation.getConstraint();
		switch (defaultValueReferenceConstraint)
		{
			case REFERENCE_EXISTS:
				if (constraintViolation.getEntityId().isPresent() && constraintViolation.getAttributes().isPresent())
				{
					constraintViolationMessage = createMessageReferenceExists("V60a", constraintViolation);
				}
				else
				{
					constraintViolationMessage = createMessageReferenceExists("V60b", constraintViolation);
				}
				break;
			default:
				throw new UnexpectedEnumException(defaultValueReferenceConstraint);
		}
		return constraintViolationMessage;
	}

	private static ValidationMessage createMessageReferenceExists(String errorCode,
			DefaultValueReferenceValidationResult constraintViolation)
	{

		String message = getMessage(constraintViolation);

		String localizedMessage;
		if (constraintViolation.getEntityId().isPresent() && constraintViolation.getAttributes().isPresent())
		{
			localizedMessage = getLocalizedMessage(errorCode, constraintViolation.getEntityType().getLabel(),
					constraintViolation.getEntityId().get(), constraintViolation.getAttributes()
																				.get()
																				.stream()
																				.map(Attribute::getLabel)
																				.collect(Collectors.joining(
																						","))).orElse(message);
		}
		else
		{
			localizedMessage = getLocalizedMessage(errorCode, constraintViolation.getEntityType().getLabel()).orElse(
					message);
		}
		return ValidationMessage.create(errorCode, message, localizedMessage);
	}

	private static String getMessage(DefaultValueReferenceValidationResult constraintViolation)
	{
		DefaultValueReferenceConstraint constraint = constraintViolation.getConstraint();
		EntityType entityType = constraintViolation.getEntityType();
		Optional<Object> entityId = constraintViolation.getEntityId();
		Optional<Collection<Attribute>> attributes = constraintViolation.getAttributes();

		String message;
		if (entityId.isPresent() && attributes.isPresent())
		{
			message = String.format("constraint:%s entityType:%s entity:%s attributes:[%s]", constraint.name(),
					entityType.getId(), entityId.get().toString(),
					attributes.get().stream().map(Attribute::getName).collect(joining(",")));
		}
		else
		{
			message = String.format("constraint:%s entityType:%s", constraint.name(), entityType.getId());
		}
		return message;
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}
}
