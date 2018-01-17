package org.molgenis.data.annotation.core.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnresolvedAnnotatorDependencyException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN07";
	private final String annotatorName;

	public UnresolvedAnnotatorDependencyException(String annotatorName)
	{
		super(ERROR_CODE);
		this.annotatorName = requireNonNull(annotatorName);
	}

	@Override
	public String getMessage()
	{
		return String.format("annotatorName:%s", annotatorName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { annotatorName };
	}
}
