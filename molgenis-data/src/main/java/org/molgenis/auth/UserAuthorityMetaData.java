package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "UserAuthority";
	public static final String USER_AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MOLGENISUSER = "molgenisUser";
	public static final String ID = "id";

	private final SecurityPackage securityPackage;

	private MolgenisUserMetaData molgenisUserMetaData;
	private AuthorityMetaData authorityMetaData;

	@Autowired
	UserAuthorityMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setExtends(authorityMetaData);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(molgenisUserMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}

	@Autowired
	public void setAuthorityMetaData(AuthorityMetaData authorityMetaData)
	{
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}
}
