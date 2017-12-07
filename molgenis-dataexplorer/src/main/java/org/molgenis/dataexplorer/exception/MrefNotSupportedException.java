package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MrefNotSupportedException extends CodedRuntimeException
{
	private final String subject;
	private static final String ERROR_CODE = "DE04";

	public MrefNotSupportedException(String subject)
	{
		super(ERROR_CODE);
		this.subject = requireNonNull(subject);
	}

	public String getSubject()
	{
		return subject;
	}

	@Override
	public String getMessage()
	{
		return String.format("subject:%s", subject);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { subject };
	}
}
