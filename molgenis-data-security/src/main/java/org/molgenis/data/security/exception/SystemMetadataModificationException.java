package org.molgenis.data.security.exception;

import org.molgenis.i18n.CodedRuntimeException;

/**
 * Thrown when users try to modify system metadata.
 */
public class SystemMetadataModificationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DS05";

	public SystemMetadataModificationException()
	{
		super(ERROR_CODE);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
