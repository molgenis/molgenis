package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public final class EntityTypeConstraintViolation implements ConstraintViolation
{
	private final EntityTypeConstraint entityTypeConstraint;
	private final EntityType entityType;

	public EntityTypeConstraintViolation(EntityTypeConstraint entityTypeConstraint, EntityType entityType)
	{
		this.entityTypeConstraint = requireNonNull(entityTypeConstraint);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public EntityTypeConstraint getConstraint()
	{
		return entityTypeConstraint;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public void accept(ConstraintViolationVisitor constraintViolationVisitor)
	{
		constraintViolationVisitor.visit(this);
	}
}
