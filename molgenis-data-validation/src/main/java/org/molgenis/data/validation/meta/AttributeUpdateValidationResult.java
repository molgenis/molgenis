package org.molgenis.data.validation.meta;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.ValidationResult;
import org.molgenis.data.validation.ValidationResultVisitor;

import java.util.EnumSet;
import java.util.Set;

@AutoValue
public abstract class AttributeUpdateValidationResult implements ValidationResult
{
	public abstract Attribute getAttribute();

	public abstract Attribute getUpdatedAttribute();

	public abstract Set<AttributeUpdateConstraint> getConstraintViolations();

	public static AttributeUpdateValidationResult create(Attribute attribute, Attribute updatedAttribute)
	{
		return create(attribute, updatedAttribute, EnumSet.noneOf(AttributeUpdateConstraint.class));
	}

	public static AttributeUpdateValidationResult create(Attribute attribute, Attribute updatedAttribute,
			Set<AttributeUpdateConstraint> newConstraints)
	{
		return new AutoValue_AttributeUpdateValidationResult(attribute, updatedAttribute, newConstraints);
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
