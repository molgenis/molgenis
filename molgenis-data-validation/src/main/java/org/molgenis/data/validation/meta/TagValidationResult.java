package org.molgenis.data.validation.meta;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.ValidationResult;
import org.molgenis.data.validation.ValidationResultVisitor;

import java.util.EnumSet;
import java.util.Set;

@AutoValue
public abstract class TagValidationResult implements ValidationResult
{
	public abstract Tag getTag();

	public abstract Set<TagConstraint> getConstraintViolations();

	@Override
	public boolean hasConstraintViolations()
	{
		return !getConstraintViolations().isEmpty();
	}

	public static TagValidationResult create(Tag newTag)
	{
		return create(newTag, EnumSet.noneOf(TagConstraint.class));
	}

	public static TagValidationResult create(Tag newTag, Set<TagConstraint> newConstraintViolations)
	{
		return new AutoValue_TagValidationResult(newTag, newConstraintViolations);
	}

	@Override
	public void accept(ValidationResultVisitor validationResultVisitor)
	{
		validationResultVisitor.visit(this);
	}
}
