package org.molgenis.data.validation;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.validation.constraint.ConstraintViolation;
import org.molgenis.data.validation.message.ConstraintViolationMessage;
import org.molgenis.data.validation.message.ValidationExceptionMessageGenerator;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ValidationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "Vxx"; // TODO error code

	private final List<ConstraintViolationMessage> constraintViolationMessages;

	public ValidationException(ConstraintViolation constraintViolation)
	{
		this(singletonList(constraintViolation));
	}

	public ValidationException(List<ConstraintViolation> constraintViolations)
	{
		super(ERROR_CODE);
		this.constraintViolationMessages = createConstraintViolationMessages(constraintViolations);
	}

	@Override
	public String getMessage()
	{
		return constraintViolationMessages.stream().map(ConstraintViolationMessage::getMessage).collect(joining("\n"));
	}

	@Override
	public String getLocalizedMessage()
	{
		String localizedViolationsMessage = constraintViolationMessages.stream()
																	   .map(ConstraintViolationMessage::getLocalizedMessage)
																	   .collect(joining("\n"));
		return getLanguageService().map(
				languageService -> languageService.getString(ERROR_CODE) + "\n" + localizedViolationsMessage)
								   .orElse(super.getLocalizedMessage());
	}

	private List<ConstraintViolationMessage> createConstraintViolationMessages(
			List<org.molgenis.data.validation.constraint.ConstraintViolation> constraintViolations)
	{
		ValidationExceptionMessageGenerator visitor = new ValidationExceptionMessageGenerator();
		constraintViolations.forEach(constraintViolation -> constraintViolation.accept(visitor));
		return visitor.getConstraintViolationMessages();
	}
}
