package org.molgenis.data.validation.constraint;

import org.molgenis.data.meta.model.Tag;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagConstraintViolation that = (TagConstraintViolation) o;
		return tagConstraint == that.tagConstraint && Objects.equals(tag.getId(), that.tag.getId());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(tagConstraint, tag.getId());
	}
}
