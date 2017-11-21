package org.molgenis.data.validation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * {@link ValidationResult} that displays multiple validation results as a single result.
 */
public class CompositeValidationResult implements ValidationResult
{
	private final Collection<ValidationResult> validationResults;

	public CompositeValidationResult()
	{
		this.validationResults = new ArrayList<>();
	}

	public void addValidationResult(ValidationResult validationResult)
	{
		validationResults.add(validationResult);
	}

	@Override
	public boolean hasConstraintViolations()
	{
		return validationResults.stream().anyMatch(ValidationResult::hasConstraintViolations);
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResults.forEach(validationResult -> validationResult.accept(validationResultVisitor));
	}
}
