package org.molgenis.data.security;

import org.molgenis.data.meta.model.Package;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class PackageIdentity extends ObjectIdentityImpl
{
	public static final String TYPE = "package";

	public PackageIdentity(Package package_)
	{
		this(package_.getId());
	}

	public PackageIdentity(String package_)
	{
		super(TYPE, package_);
	}
}