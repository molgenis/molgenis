package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class VcfReaderCreationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF01";
	private final String filename;

	public VcfReaderCreationException(String filename)
	{
		super(ERROR_CODE);
		this.filename = requireNonNull(filename);
	}

	@Override
	public String getMessage()
	{
		return String.format("filename:%s", filename);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { filename };
	}
}
