package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.i18n.CodedRuntimeException;

import java.io.File;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class UnsupportedFileTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI06";

	private final File file;
	private final Set<String> supportedFileExtensions;

	public UnsupportedFileTypeException(File file, Set<String> supportedFileExtensions)
	{
		super(ERROR_CODE);
		this.file = requireNonNull(file);
		this.supportedFileExtensions = supportedFileExtensions;
	}

	@Override
	public String getMessage()
	{
		return "file:" + file.getName() + " supported:" + supportedFileExtensions.stream().collect(joining(","));
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { file.getName(), supportedFileExtensions.stream().collect(joining(",")) };
	}
}
