package org.molgenis.auth;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class UserAuthorityMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "UserAuthority";
	public static final String USER_AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MOLGENIS_USER = "molgenisUser";
	public static final String ID = "id";

	private final SecurityPackage securityPackage;
	private final MolgenisUserMetaData molgenisUserMetaData;
	private final AuthorityMetaData authorityMetaData;

	@Autowired
	UserAuthorityMetaData(SecurityPackage securityPackage, MolgenisUserMetaData molgenisUserMetaData,
			AuthorityMetaData authorityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setExtends(authorityMetaData);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MOLGENIS_USER).setDataType(XREF).setRefEntity(molgenisUserMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}

	@Override
	public Set<SystemEntityMetaData> getDependencies()
	{
		return singleton(authorityMetaData);
	}
}
