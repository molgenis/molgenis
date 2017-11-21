package org.molgenis.data.validation;

import org.molgenis.data.CodedRuntimeException;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ValidationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "V99";

	private final Collection<ValidationMessage> validationMessages;

	public ValidationException(ValidationResult constraintViolation)
	{
		this(singletonList(constraintViolation));
	}

	public ValidationException(Collection<? extends ValidationResult> constraintViolations)
	{
		super(ERROR_CODE);
		this.validationMessages = createConstraintViolationMessages(constraintViolations);
	}

	@Override
	public String getMessage()
	{
		return validationMessages.stream().map(ValidationMessage::getMessage).collect(joining("\n"));
	}

	@Override
	public String getLocalizedMessage()
	{
		String localizedViolationsMessage = validationMessages.stream()
															  .map(ValidationMessage::getLocalizedMessage)
															  .collect(joining("\n"));
		return getLanguageService().map(
				languageService -> languageService.getString(ERROR_CODE) + ": " + localizedViolationsMessage)
								   .orElse(super.getLocalizedMessage());
	}

	public Stream<ValidationMessage> getValidationMessages()
	{
		return validationMessages.stream();
	}

	private Collection<ValidationMessage> createConstraintViolationMessages(
			Collection<? extends ValidationResult> constraintViolations)
	{
		ValidationMessageGenerator visitor = new ValidationMessageGenerator();
		constraintViolations.forEach(constraintViolation -> constraintViolation.accept(visitor));
		return visitor.getValidationMessages();
	}
}
