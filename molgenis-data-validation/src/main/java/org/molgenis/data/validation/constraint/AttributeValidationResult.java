package org.molgenis.data.validation.constraint;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

import java.util.EnumSet;
import java.util.Set;

@AutoValue
public abstract class AttributeValidationResult implements ValidationResult
{
	public abstract Attribute getAttribute();

	public abstract Set<AttributeConstraint> getConstraintViolations();

	public static AttributeValidationResult create(Attribute attribute)
	{
		return create(attribute, EnumSet.noneOf(AttributeConstraint.class));
	}

	public static AttributeValidationResult create(Attribute attribute, Set<AttributeConstraint> newConstraints)
	{
		return new AutoValue_AttributeValidationResult(attribute, newConstraints);
	}

	@Override
	public boolean hasConstraintViolations()
	{
		return !getConstraintViolations().isEmpty();
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResultVisitor.visit(this);
	}
}
