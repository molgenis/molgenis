package org.molgenis.data.security.model;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.model.SecurityPackage.PACKAGE_SECURITY;

@Component
public class GroupMembershipMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "GroupMembership";
	public static final String GROUP_MEMBERSHIP = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String USER = "user";
	public static final String GROUP = "group";
	public static final String START = "start";
	public static final String END = "end";

	private final SecurityPackage securityPackage;
	private final UserMetadata userMetaData;
	private final GroupMetadata groupMetaData;

	GroupMembershipMetadata(SecurityPackage securityPackage, UserMetadata userMetaData, GroupMetadata groupMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.userMetaData = requireNonNull(userMetaData);
		this.groupMetaData = requireNonNull(groupMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Group membership");
		setPackage(securityPackage);
		addAttribute(ID, ROLE_ID).setLabel("Id").setAuto(true).setVisible(false);
		addAttribute(USER).setLabel("User")
						  .setDataType(XREF)
						  .setRefEntity(userMetaData)
						  .setAggregatable(true)
						  .setNillable(false)
						  .setCascadeDelete(true);
		addAttribute(GROUP).setLabel("Group")
						   .setDataType(XREF)
						   .setRefEntity(groupMetaData)
						   .setAggregatable(true)
						   .setNillable(false)
						   .setCascadeDelete(true);
		addAttribute(START).setLabel("Start").setDataType(DATE_TIME).setNillable(false);
		addAttribute(END).setLabel("End").setDataType(DATE_TIME).setNillable(true);
	}
}
