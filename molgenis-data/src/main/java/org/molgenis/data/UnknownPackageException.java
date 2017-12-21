package org.molgenis.data;

import org.molgenis.data.meta.model.PackageMetadata;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownPackageException extends UnknownEntityException
{
	public UnknownPackageException(PackageMetadata packageMetadata, String packageId)
	{
		super(packageMetadata, packageId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", getEntityId().toString());
	}
}

