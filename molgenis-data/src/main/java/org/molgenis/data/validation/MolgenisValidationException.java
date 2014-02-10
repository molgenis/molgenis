package org.molgenis.data.validation;

import java.util.List;

import org.molgenis.data.MolgenisDataException;

public class MolgenisValidationException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;
	private final List<ConstraintViolation> violations;

	public MolgenisValidationException(List<ConstraintViolation> violations)
	{
		super();
		this.violations = violations;
	}

	public List<ConstraintViolation> getViolations()
	{
		return violations;
	}

}
