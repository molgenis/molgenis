package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Attribute;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO introduce AttributeConstraintViolations with Attribute + EnumSet<AttributeConstraint>
// TODO autovalue?
public final class AttributeConstraintViolation implements ConstraintViolation
{
	private final AttributeConstraint attributeConstraint;
	private final Attribute attribute;

	public AttributeConstraintViolation(AttributeConstraint attributeConstraint, Attribute attribute)
	{
		this.attributeConstraint = requireNonNull(attributeConstraint);
		this.attribute = requireNonNull(attribute);
	}

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttributeConstraintViolation that = (AttributeConstraintViolation) o;
		return attributeConstraint == that.attributeConstraint && Objects.equals(attribute.getIdentifier(),
				that.attribute.getIdentifier());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(attributeConstraint, attribute.getIdentifier());
	}

	@Override
	public String toString()
	{
		return "AttributeConstraintViolation{" + "attributeConstraint=" + attributeConstraint + ", attribute="
				+ attribute.getIdentifier() + '}';
	}
}
