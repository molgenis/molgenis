package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

public final class AttributeConstraintViolation implements ConstraintViolation
{
	private final AttributeConstraint attributeConstraint;
	private final Attribute attribute;

	public AttributeConstraintViolation(AttributeConstraint attributeConstraint, Attribute attribute)
	{
		this.attributeConstraint = requireNonNull(attributeConstraint);
		this.attribute = requireNonNull(attribute);
	}

	@Override
	public AttributeConstraint getConstraint()
	{
		return attributeConstraint;
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	@Override
	public void accept(ConstraintViolationVisitor constraintViolationVisitor)
	{
		constraintViolationVisitor.visit(this);
	}
}
