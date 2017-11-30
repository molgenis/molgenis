package org.molgenis.data.validation;

import org.molgenis.i18n.CodedRuntimeException;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public class ValidationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "V99";

	private final Collection<ValidationMessage> validationMessages;

	public ValidationException(ValidationResult constraintViolation)
	{
		this(singletonList(constraintViolation));
	}

	/**
	 * @deprecated use {@link #ValidationException(ValidationResult)} with {@link CompositeValidationResult}
	 */
	@Deprecated
	public ValidationException(Collection<? extends ValidationResult> constraintViolations)
	{
		super(ERROR_CODE);
		this.validationMessages = createConstraintViolationMessages(constraintViolations);
	}

	@Override
	public String getMessage()
	{
		return validationMessages.stream().map(ValidationMessage::getMessage).collect(joining(","));
	}

	@Override
	public String getLocalizedMessage()
	{
		//TODO: put the validation messages as argument in the message format
		return super.getLocalizedMessage() + ": " + validationMessages.stream()
																	  .map(ValidationMessage::getLocalizedMessage)
																	  .collect(joining("\n"));
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] {};
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
