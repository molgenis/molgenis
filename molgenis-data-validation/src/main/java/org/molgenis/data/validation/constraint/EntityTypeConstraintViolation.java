package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.EntityType;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class EntityTypeConstraintViolation implements ConstraintViolation
{
	private final EntityTypeConstraint entityTypeConstraint;
	private final EntityType entityType;

	public EntityTypeConstraintViolation(EntityTypeConstraint entityTypeConstraint, EntityType entityType)
	{
		this.entityTypeConstraint = requireNonNull(entityTypeConstraint);
		this.entityType = requireNonNull(entityType);
	}

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EntityTypeConstraintViolation that = (EntityTypeConstraintViolation) o;
		return entityTypeConstraint == that.entityTypeConstraint && Objects.equals(entityType.getId(),
				that.entityType.getId());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(entityTypeConstraint, entityType.getId());
	}
}
