package org.molgenis.data.validation.constraint;

public interface ConstraintViolationVisitor
{
	void visit(EntityTypeConstraintViolation entityTypeConstraintViolation);

	void visit(AttributeConstraintViolation attributeConstraintViolation);

	void visit(TagConstraintViolation tagConstraintViolation);

	void visit(AttributeValueConstraintViolation attributeValueConstraintViolation);

	void visit(PackageConstraintViolation packageConstraintViolation);

	void visit(DefaultValueReferenceConstraintViolation entityConstraintViolation);
}
