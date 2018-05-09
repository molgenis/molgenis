package org.molgenis.data.security.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class GroupMemberMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "GroupMember";
	public static final String GROUP_MEMBER = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String USER = "User";
	public static final String GROUP = "Role";

	private final SecurityPackage securityPackage;
	private final UserMetaData userMetaData;
	private final RoleMetadata roleMetadata;

	GroupMemberMetaData(SecurityPackage securityPackage, UserMetaData userMetaData, RoleMetadata roleMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.userMetaData = requireNonNull(userMetaData);
		this.roleMetadata = requireNonNull(roleMetadata);
	}

	@Override
	public void init()
	{
		setLabel("Role member");
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(USER).setDataType(XREF).setRefEntity(userMetaData).setAggregatable(true).setNillable(false);
		addAttribute(GROUP).setDataType(XREF).setRefEntity(roleMetadata).setAggregatable(true).setNillable(false);
	}
}
