package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;

import static java.util.Objects.requireNonNull;

public class SecondRunNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN10";
	private final transient RepositoryAnnotator annotator;

	public SecondRunNotSupportedException(RepositoryAnnotator annotator)
	{
		super(ERROR_CODE);
		this.annotator = requireNonNull(annotator);
	}

	@Override
	public String getMessage()
	{
		return String.format("annotator:%s", annotator.getFullName());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { annotator.getFullName() };
	}
}
