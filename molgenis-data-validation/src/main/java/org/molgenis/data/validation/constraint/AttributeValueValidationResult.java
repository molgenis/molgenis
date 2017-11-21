package org.molgenis.data.validation.constraint;

import org.molgenis.data.AttributeValue;

import static java.util.Objects.requireNonNull;

// TODO refactor this class to be similar to PackageValidationResult, AttributeValidationResult, EntityTypeValidationResult
public final class AttributeValueValidationResult implements ValidationResult
{
	private final AttributeValueConstraint attributeValueConstraint;
	private final AttributeValue attributeValue;

	public AttributeValueValidationResult(AttributeValueConstraint attributeValueConstraint,
			AttributeValue attributeValue)
	{
		this.attributeValueConstraint = requireNonNull(attributeValueConstraint);
		this.attributeValue = requireNonNull(attributeValue);
	}

	public AttributeValueConstraint getConstraint()
	{
		return attributeValueConstraint;
	}

	public AttributeValue getAttributeValue()
	{
		return attributeValue;
	}

	@Override
	public boolean hasConstraintViolations()
	{
		return true;
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResultVisitor.visit(this);
	}
}
