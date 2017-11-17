package org.molgenis.data.validation.message;

import org.molgenis.data.validation.constraint.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that creates {@link ConstraintViolationMessage} for each {@link ConstraintViolation} to use in {@link org.molgenis.data.validation.ValidationException}.
 */
public class ValidationExceptionMessageGenerator implements ConstraintViolationVisitor
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
	public void visit(PackageConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorPackage.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(EntityTypeConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorEntityType.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(AttributeConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorAttribute.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
	}

	@Override
	public void visit(TagConstraintViolation constraintViolation)
	{
		ConstraintViolationMessage violationMessage = MessageGeneratorTag.createMessage(constraintViolation);
		constraintViolationMessages.add(violationMessage);
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
