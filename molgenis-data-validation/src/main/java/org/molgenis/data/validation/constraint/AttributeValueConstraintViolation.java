package org.molgenis.data.validation.constraint;

import org.molgenis.data.AttributeValue;

import static java.util.Objects.requireNonNull;

public final class AttributeValueConstraintViolation implements ValidationResult
{
	private final AttributeValueConstraint attributeValueConstraint;
	private final AttributeValue attributeValue;

	public AttributeValueConstraintViolation(AttributeValueConstraint attributeValueConstraint,
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
		return true; // FIXME
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResultVisitor.visit(this);
	}
}
