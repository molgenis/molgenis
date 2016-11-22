package org.molgenis.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class GroupMemberMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "GroupMember";
	public static final String GROUP_MEMBER = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String USER = "User";
	public static final String GROUP = "Group";

	private final SecurityPackage securityPackage;
	private final UserMetaData userMetaData;
	private final GroupMetaData groupMetaData;

	@Autowired
	GroupMemberMetaData(SecurityPackage securityPackage, UserMetaData userMetaData,
			GroupMetaData groupMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.userMetaData = requireNonNull(userMetaData);
		this.groupMetaData = requireNonNull(groupMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Group member");
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(USER).setDataType(XREF).setRefEntity(userMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
		addAttribute(GROUP).setDataType(XREF).setRefEntity(groupMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}
}
