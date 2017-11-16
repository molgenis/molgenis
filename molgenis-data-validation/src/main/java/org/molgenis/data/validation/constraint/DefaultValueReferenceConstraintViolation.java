package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DefaultValueReferenceConstraintViolation implements ConstraintViolation
{
	private final DefaultValueReferenceConstraint entityConstraint;
	private final EntityType entityType;
	private final Object entityId;
	private final Collection<Attribute> attributes;

	public DefaultValueReferenceConstraintViolation(DefaultValueReferenceConstraint entityConstraint,
			EntityType entityType)
	{
		this(entityConstraint, entityType, null, null);
	}

	public DefaultValueReferenceConstraintViolation(DefaultValueReferenceConstraint entityConstraint,
			EntityType entityType, @Nullable Object entityId, @Nullable Collection<Attribute> attributes)
	{
		this.entityConstraint = requireNonNull(entityConstraint);
		this.entityType = requireNonNull(entityType);
		this.entityId = entityId;
		this.attributes = attributes;
	}

	@Override
	public DefaultValueReferenceConstraint getConstraint()
	{
		return entityConstraint;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public Optional<Object> getEntityId()
	{
		return Optional.ofNullable(entityId);
	}

	public Optional<Collection<Attribute>> getAttributes()
	{
		return Optional.ofNullable(attributes);
	}

	@Override
	public void accept(ConstraintViolationVisitor constraintViolationVisitor)
	{
		constraintViolationVisitor.visit(this);
	}
}
