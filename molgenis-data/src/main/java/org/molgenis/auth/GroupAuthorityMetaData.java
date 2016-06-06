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
public class GroupAuthorityMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "GroupAuthority";
	public static final String GROUP_AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MOLGENIS_GROUP = "molgenisGroup";
	public static final String ROLE = "role";
	public static final String ID = "id";

	private final SecurityPackage securityPackage;

	private MolgenisGroupMetaData molgenisGroupMetaData;
	private AuthorityMetaData authorityMetaData;

	@Autowired
	GroupAuthorityMetaData(SecurityPackage securityPackage)
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
		addAttribute(MOLGENIS_GROUP).setDataType(XREF).setRefEntity(molgenisGroupMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisGroupMetaData(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		this.molgenisGroupMetaData = requireNonNull(molgenisGroupMetaData);
	}

	@Autowired
	public void setAuthorityMetaData(AuthorityMetaData authorityMetaData)
	{
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}
}
