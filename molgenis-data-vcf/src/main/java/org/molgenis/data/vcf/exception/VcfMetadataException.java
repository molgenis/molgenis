package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

public class VcfMetadataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF02";

	public VcfMetadataException(Throwable cause)
	{
		super(ERROR_CODE, cause);
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
