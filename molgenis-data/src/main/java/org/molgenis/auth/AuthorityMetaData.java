package org.molgenis.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class AuthorityMetaData extends SystemEntityType
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
		setLabel("Authority");
		setPackage(securityPackage);

		setAbstract(true);
		addAttribute(ROLE).setLabel("role").setNillable(true);
	}
}
