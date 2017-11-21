package org.molgenis.data.validation;

import org.molgenis.data.validation.data.AttributeValueMessageGenerator;
import org.molgenis.data.validation.data.AttributeValueValidationResult;
import org.molgenis.data.validation.data.DefaultValueReferenceMessageGenerator;
import org.molgenis.data.validation.data.DefaultValueReferenceValidationResult;
import org.molgenis.data.validation.meta.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that creates {@link ValidationMessage validation messages} for each {@link ValidationResult} to use in {@link org.molgenis.data.validation.ValidationException}.
 */
public class ValidationMessageGenerator implements ValidationResultVisitor
{
	private List<ValidationMessage> validationMessages;

	public ValidationMessageGenerator()
	{
		validationMessages = new ArrayList<>();
	}

	public List<ValidationMessage> getValidationMessages()
	{
		return validationMessages;
	}

	@Override
	public void visit(PackageValidationResult packageValidationResult)
	{
		validationMessages.addAll(PackageMessageGenerator.createMessages(packageValidationResult));
	}

	@Override
	public void visit(EntityTypeValidationResult entityTypeValidationResult)
	{
		validationMessages.addAll(EntityTypeMessageGenerator.createMessages(entityTypeValidationResult));
	}

	@Override
	public void visit(AttributeValidationResult attributeValidationResult)
	{
		validationMessages.addAll(AttributeMessageGenerator.createMessages(attributeValidationResult));
	}

	@Override
	public void visit(TagValidationResult tagValidationResult)
	{
		validationMessages.addAll(TagMessageGenerator.createMessages(tagValidationResult));
	}

	@Override
	public void visit(AttributeValueValidationResult attributeValueValidationResult)
	{
		ValidationMessage violationMessage = AttributeValueMessageGenerator.createMessage(
				attributeValueValidationResult);
		validationMessages.add(violationMessage);
	}

	@Override
	public void visit(DefaultValueReferenceValidationResult defaultValueReferenceValidationResult)
	{
		ValidationMessage violationMessage = DefaultValueReferenceMessageGenerator.createMessage(
				defaultValueReferenceValidationResult);
		validationMessages.add(violationMessage);
	}
}
