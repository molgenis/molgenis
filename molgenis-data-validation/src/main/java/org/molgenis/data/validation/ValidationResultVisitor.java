package org.molgenis.data.validation;

import org.molgenis.data.validation.data.AttributeValueValidationResult;
import org.molgenis.data.validation.data.DefaultValueReferenceValidationResult;
import org.molgenis.data.validation.meta.*;

public interface ValidationResultVisitor
{
	void visit(EntityTypeValidationResult entityTypeConstraintViolation);

	void visit(AttributeValidationResult attributeConstraintViolation);

	void visit(TagValidationResult tagValidationResult);

	void visit(AttributeValueValidationResult attributeValueConstraintViolation);

	void visit(PackageValidationResult packageValidationResult);

	void visit(DefaultValueReferenceValidationResult entityConstraintViolation);

	void visit(AttributeUpdateValidationResult attributeUpdateValidationResult);
}
