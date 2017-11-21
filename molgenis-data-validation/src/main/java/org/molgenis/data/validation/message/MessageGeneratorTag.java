package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.constraint.TagConstraint;
import org.molgenis.data.validation.constraint.TagValidationResult;
import org.molgenis.util.UnexpectedEnumException;

import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;
import static org.molgenis.data.validation.constraint.TagConstraint.UNKNOWN_RELATION_IRI;

class MessageGeneratorTag
{
	private MessageGeneratorTag()
	{
	}

	static List<ConstraintViolationMessage> createMessages(TagValidationResult tagValidationResult)
	{
		Tag tag = tagValidationResult.getTag();
		return tagValidationResult.getConstraintViolations()
								  .stream()
								  .map(tagConstraint -> createMessage(tag, tagConstraint))
								  .collect(toList());
	}

	static ConstraintViolationMessage createMessage(Tag tag, TagConstraint tagConstraint)
	{
		ConstraintViolationMessage constraintViolationMessage;

		switch (tagConstraint)
		{
			case UNKNOWN_RELATION_IRI:
				constraintViolationMessage = createMessageUnknownRelationIri("V20", tag);
				break;
			default:
				throw new UnexpectedEnumException(tagConstraint);
		}
		return constraintViolationMessage;
	}

	private static String getMessage(Tag tag, TagConstraint tagConstraint)
	{
		return String.format("constraint:%s tag:%s", tagConstraint.name(), tag.getId());
	}

	private static Optional<String> getLocalizedMessage(String errorCode, Object... arguments)
	{
		return getLanguageService().map(languageService -> format(languageService.getString(errorCode), arguments));
	}

	@SuppressWarnings("SameParameterValue")
	private static ConstraintViolationMessage createMessageUnknownRelationIri(String errorCode, Tag tag)
	{
		String message = getMessage(tag, UNKNOWN_RELATION_IRI);
		String localizedMessage = getLocalizedMessage(errorCode, tag.getLabel(), tag.getRelationIri()).orElse(message);
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
