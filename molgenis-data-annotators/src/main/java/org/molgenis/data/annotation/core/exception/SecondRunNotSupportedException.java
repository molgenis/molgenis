package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;

public class SecondRunNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN10";
	private RepositoryAnnotator annotator;

	public SecondRunNotSupportedException(RepositoryAnnotator annotator)
	{
		super(ERROR_CODE);
		this.annotator = annotator;
	}
}
