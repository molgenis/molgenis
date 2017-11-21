package org.molgenis.data.validation.message;

import org.molgenis.data.validation.constraint.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that creates {@link ConstraintViolationMessage} for each {@link ValidationResult} to use in {@link org.molgenis.data.validation.ValidationException}.
 */
public class ValidationExceptionMessageGenerator implements ValidationResultVisitor
{
	private List<ConstraintViolationMessage> constraintViolationMessages;

	public ValidationExceptionMessageGenerator()
	{
		constraintViolationMessages = new ArrayList<>();
	}

	public List<ConstraintViolationMessage> getConstraintViolationMessages()
	{
		return constraintViolationMessages;
	}

	@Override
	public void visit(PackageValidationResult packageValidationResult)
	{
		constraintViolationMessages.addAll(MessageGeneratorPackage.createMessages(packageValidationResult));
	}

	@Override
	public void visit(EntityTypeValidationResult entityTypeValidationResult)
	{
		constraintViolationMessages.addAll(MessageGeneratorEntityType.createMessages(entityTypeValidationResult));
	}

	@Override
	public void visit(AttributeValidationResult attributeValidationResult)
	{
		constraintViolationMessages.addAll(MessageGeneratorAttribute.createMessages(attributeValidationResult));
	}

	@Override
	public void visit(TagValidationResult tagValidationResult)
	{
		constraintViolationMessages.addAll(MessageGeneratorTag.createMessages(tagValidationResult));
	}

	@Override
	public void visit(AttributeValueValidationResult attributeValueValidationResult)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorAttributeValue.createMessage(
				attributeValueValidationResult);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(DefaultValueReferenceValidationResult defaultValueReferenceValidationResult)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorDefaultValueReference.createMessage(
				defaultValueReferenceValidationResult);
		constraintViolationMessages.add(violationMessage);
	}
}
