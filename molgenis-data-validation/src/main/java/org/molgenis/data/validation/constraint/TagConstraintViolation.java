package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Tag;

import static java.util.Objects.requireNonNull;

public class TagConstraintViolation implements ConstraintViolation
{
	private final TagConstraint tagConstraint;
	private final Tag tag;

	public TagConstraintViolation(TagConstraint tagConstraint, Tag tag)
	{
		this.tagConstraint = requireNonNull(tagConstraint);
		this.tag = requireNonNull(tag);
	}

	@Override
	public TagConstraint getConstraint()
	{
		return tagConstraint;
	}

	public Tag getTag()
	{
		return tag;
	}

	@Override
	public void accept(ConstraintViolationVisitor constraintViolationVisitor)
	{
		constraintViolationVisitor.visit(this);
	}
}
