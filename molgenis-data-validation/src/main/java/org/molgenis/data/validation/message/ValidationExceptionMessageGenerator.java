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
	public void visit(EntityTypeValidationResult constraintViolation)
	{
		constraintViolationMessages.addAll(MessageGeneratorEntityType.createMessages(constraintViolation));
	}

	@Override
	public void visit(AttributeConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorAttribute.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(TagValidationResult tagValidationResult)
	{
		constraintViolationMessages.addAll(MessageGeneratorTag.createMessages(tagValidationResult));
	}

	@Override
	public void visit(AttributeValueConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorAttributeValue.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(DefaultValueReferenceConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorDefaultValueReference.createMessage(
				constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}
}
