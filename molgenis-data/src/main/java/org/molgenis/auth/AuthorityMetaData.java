package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorityMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "authority";
	public static final String AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ROLE = "role";

	private final SecurityPackage securityPackage;

	@Autowired
	AuthorityMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setAbstract(true);
		addAttribute(ROLE).setLabel("role").setNillable(true);
	}
}
