package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

public class MissingFormatException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF05";

	public MissingFormatException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
