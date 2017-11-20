package org.molgenis.data.validation.constraint;

public interface ValidationResultVisitor
{
	void visit(EntityTypeConstraintViolation entityTypeConstraintViolation);

	void visit(AttributeConstraintViolation attributeConstraintViolation);

	void visit(TagValidationResult tagValidationResult);

	void visit(AttributeValueConstraintViolation attributeValueConstraintViolation);

	void visit(PackageValidationResult packageValidationResult);

	void visit(DefaultValueReferenceConstraintViolation entityConstraintViolation);
}
