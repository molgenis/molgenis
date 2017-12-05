package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnknownSampleAttributeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF03";
	private final String sampleAttribute;

	public UnknownSampleAttributeException(String sampleAttribute)
	{
		super(ERROR_CODE);
		this.sampleAttribute = requireNonNull(sampleAttribute);
	}

	@Override
	public String getMessage()
	{
		return String.format("sampleAttribute:%s", sampleAttribute);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { sampleAttribute };
	}
}
