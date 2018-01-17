package org.molgenis.data.vcf.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidHeaderException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF08";

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
