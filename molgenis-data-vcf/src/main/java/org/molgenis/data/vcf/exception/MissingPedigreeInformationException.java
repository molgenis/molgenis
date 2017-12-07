package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MissingPedigreeInformationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF04";
	private final String line;

	public MissingPedigreeInformationException(String line)
	{
		super(ERROR_CODE);
		this.line = requireNonNull(line);
	}

	@Override
	public String getMessage()
	{
		return String.format("line:%s", line);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { line };
	}
}
