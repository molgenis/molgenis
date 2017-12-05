package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnexpectedPedigreeInformationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF06";
	private final String value;
	private final String line;

	public UnexpectedPedigreeInformationException(String value, String line)
	{
		super(ERROR_CODE);
		this.value = requireNonNull(value);
		this.line = requireNonNull(line);
	}

	@Override
	public String getMessage()
	{
		return String.format("value:%s line:%s", value, line);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { value, line };
	}
}
