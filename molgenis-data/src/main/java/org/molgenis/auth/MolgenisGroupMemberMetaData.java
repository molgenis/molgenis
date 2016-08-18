package org.molgenis.auth;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class MolgenisGroupMemberMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "MolgenisGroupMember";
	public static final String MOLGENIS_GROUP_MEMBER = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String MOLGENIS_USER = "molgenisUser";
	public static final String MOLGENIS_GROUP = "molgenisGroup";

	private final SecurityPackage securityPackage;
	private final MolgenisUserMetaData molgenisUserMetaData;
	private final MolgenisGroupMetaData molgenisGroupMetaData;

	@Autowired
	MolgenisGroupMemberMetaData(SecurityPackage securityPackage, MolgenisUserMetaData molgenisUserMetaData,
			MolgenisGroupMetaData molgenisGroupMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
		this.molgenisGroupMetaData = requireNonNull(molgenisGroupMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Group member");
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MOLGENIS_USER).setDataType(XREF).setRefEntity(molgenisUserMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
		addAttribute(MOLGENIS_GROUP).setDataType(XREF).setRefEntity(molgenisGroupMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}
}
