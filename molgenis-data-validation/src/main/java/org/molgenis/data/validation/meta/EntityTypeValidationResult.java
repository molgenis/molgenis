package org.molgenis.data.validation.meta;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.ValidationResult;
import org.molgenis.data.validation.ValidationResultVisitor;

import java.util.EnumSet;
import java.util.Set;

@AutoValue
public abstract class EntityTypeValidationResult implements ValidationResult
{
	public abstract EntityType getEntityType();

	public abstract Set<EntityTypeConstraint> getConstraintViolations();

	public static EntityTypeValidationResult create(EntityType newEntityType)
	{
		return create(newEntityType, EnumSet.noneOf(EntityTypeConstraint.class));
	}

	public static EntityTypeValidationResult create(EntityType newEntityType,
			Set<EntityTypeConstraint> newConstraintViolations)
	{
		return new AutoValue_EntityTypeValidationResult(newEntityType, newConstraintViolations);
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
