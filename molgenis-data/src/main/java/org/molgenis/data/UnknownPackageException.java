package org.molgenis.data;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownPackageException extends UnknownDataException
{
	private static final String ERROR_CODE = "D07";

	private final String packageId;

	public UnknownPackageException(String packageId)
	{
		super(ERROR_CODE);
		this.packageId = requireNonNull(packageId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", packageId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { packageId };
	}
}

