package org.molgenis.data.validation;

import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MolgenisValidationException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;
	private Set<ConstraintViolation> violations;

	public MolgenisValidationException(ConstraintViolation violation)
	{
		this(Collections.singleton(violation));
	}

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

		return StringUtils.join(Collections2.transform(violations, ConstraintViolation::getMessage), '.');
	}

	/**
	 * renumber the violation row indices with the actual row numbers
	 *
	 * @param actualIndices
	 */
	public void renumberViolationRowIndices(List<Integer> actualIndices)
	{
		violations.stream().forEach(v -> v.renumberRowIndex(actualIndices));
	}
}
