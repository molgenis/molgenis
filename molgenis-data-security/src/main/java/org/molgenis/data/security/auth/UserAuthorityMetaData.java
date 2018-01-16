package org.molgenis.data.security.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class UserAuthorityMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "UserAuthority";
	public static final String USER_AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String USER = "User";
	public static final String ID = "id";

	private final SecurityPackage securityPackage;
	private final UserMetaData userMetaData;
	private final AuthorityMetaData authorityMetaData;

	UserAuthorityMetaData(SecurityPackage securityPackage, UserMetaData userMetaData,
			AuthorityMetaData authorityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.userMetaData = requireNonNull(userMetaData);
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}

	@Override
	public void init()
	{
		setLabel("User authority");
		setPackage(securityPackage);

		setExtends(authorityMetaData);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(USER).setDataType(XREF).setRefEntity(userMetaData).setAggregatable(true).setNillable(false);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(authorityMetaData);
	}
}
