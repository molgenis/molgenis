package org.molgenis.data.security.meta;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class SecurityMetadataPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "md";
	public static final String PACKAGE_SECURITY_METADATA = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final SecurityPackage securityPackage;

	public SecurityMetadataPackage(PackageMetadata packageMetadata, SecurityPackage securityPackage)
	{
		super(PACKAGE_SECURITY_METADATA, packageMetadata);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	protected void init()
	{
		setParent(securityPackage);

		setLabel("Security Metadata");
		setDescription("System package containing metadata for security related entity types");
	}
}
