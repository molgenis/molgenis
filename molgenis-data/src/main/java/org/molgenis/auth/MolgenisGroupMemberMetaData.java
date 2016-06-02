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
public class MolgenisGroupMemberMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "MolgenisGroupMember";
	public static final String MOLGENIS_GROUP_MEMBER = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MOLGENISUSER = "molgenisUser";
	public static final String MOLGENISGROUP = "molgenisGroup";
	public static final String ID = "id";

	private MolgenisUserMetaData molgenisUserMetaData;
	private MolgenisGroupMetaData MolgenisGroupMetaData;
	private SecurityPackage securityPackage;

	@Autowired
	MolgenisGroupMemberMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(molgenisUserMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(MolgenisGroupMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}

	@Autowired
	public void setMolgenisGroupMetaData(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		this.MolgenisGroupMetaData = requireNonNull(molgenisGroupMetaData);
	}
}
