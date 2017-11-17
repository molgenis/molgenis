package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.constraint.TagConstraint;
import org.molgenis.data.validation.constraint.TagConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Optional;

import static java.text.MessageFormat.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

class MessageGeneratorTag
{
	private MessageGeneratorTag()
	{
	}

	static ConstraintViolationMessage createMessage(TagConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage constraintViolationMessage;

		TagConstraint tagConstraint = constraintViolation.getConstraint();
		switch (tagConstraint)
		{
			case UNKNOWN_RELATION_IRI:
				constraintViolationMessage = createMessageUnknownRelationIri("V20", constraintViolation);
				break;
			default:
				throw new UnexpectedEnumException(tagConstraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(TagConstraintViolation constraintViolation)
	{
		return String.format("constraint:%s tag:%s", constraintViolation.getConstraint().name(),
				constraintViolation.getTag().getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	private static ConstraintViolationMessage createMessageUnknownRelationIri(String errorCode,
			TagConstraintViolation constraintViolation)
	{
		Tag tag = constraintViolation.getTag();

		String message = getMessage(constraintViolation);
		String localizedMessage = getLocalizedMessage(errorCode, tag.getLabel(), tag.getRelationIri()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
