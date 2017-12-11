package org.molgenis.data.mapper.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class IllegalTargetPackageException extends MappingServiceException
{
	private static final String ERROR_CODE = "M02";

	private final String packageId;

	public IllegalTargetPackageException(String packageId)
	{
		super(ERROR_CODE);
		this.packageId = requireNonNull(packageId);
	}

	public String getPackageId()
	{
		return packageId;
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", packageId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { packageId };
	}
}
