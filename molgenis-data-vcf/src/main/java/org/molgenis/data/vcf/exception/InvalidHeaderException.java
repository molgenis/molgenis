package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

public class InvalidHeaderException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF07";

	public InvalidHeaderException()
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
