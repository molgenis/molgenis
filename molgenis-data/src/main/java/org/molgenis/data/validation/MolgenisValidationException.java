package org.molgenis.data.validation;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class MolgenisValidationException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;
	private final Set<ConstraintViolation> violations;

	public MolgenisValidationException(Set<ConstraintViolation> violations)
	{
		this.violations = violations;
	}

	public Set<ConstraintViolation> getViolations()
	{
		return violations;
	}

	@Override
	public String getMessage()
	{
		if ((violations == null) || (violations.isEmpty())) return "Unknown validation exception.";

		return StringUtils.join(Collections2.transform(violations, new Function<ConstraintViolation, String>()
		{
			@Override
			public String apply(ConstraintViolation violation)
			{
				return violation.getMessage();
			}

		}), '.');
	}

}
