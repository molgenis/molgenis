package org.molgenis.data.validation;

import java.util.Set;

import org.molgenis.data.MolgenisDataException;

public class MolgenisValidationException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;
	private final Set<ConstraintViolation> violations;

	public MolgenisValidationException(Set<ConstraintViolation> violations)
	{
		super();
		this.violations = violations;
	}

	public Set<ConstraintViolation> getViolations()
	{
		return violations;
	}

}
