package org.molgenis.data.validation.constraint;

public interface ValidationResultVisitor
{
	void visit(EntityTypeValidationResult entityTypeConstraintViolation);

	void visit(AttributeValidationResult attributeConstraintViolation);

	void visit(TagValidationResult tagValidationResult);

	void visit(AttributeValueValidationResult attributeValueConstraintViolation);

	void visit(PackageValidationResult packageValidationResult);

	void visit(DefaultValueReferenceValidationResult entityConstraintViolation);
}
