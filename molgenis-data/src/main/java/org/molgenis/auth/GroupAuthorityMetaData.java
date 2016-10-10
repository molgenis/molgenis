package org.molgenis.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class GroupAuthorityMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "GroupAuthority";
	public static final String GROUP_AUTHORITY = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String GROUP = "Group";
	public static final String ID = "id";

	private final SecurityPackage securityPackage;
	private final GroupMetaData groupMetaData;
	private final AuthorityMetaData authorityMetaData;

	@Autowired
	GroupAuthorityMetaData(SecurityPackage securityPackage, GroupMetaData groupMetaData,
			AuthorityMetaData authorityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.groupMetaData = requireNonNull(groupMetaData);
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Group authority");
		setPackage(securityPackage);

		setExtends(authorityMetaData);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(GROUP).setDataType(XREF).setRefEntity(groupMetaData).setAggregatable(true)
				.setDescription("").setNillable(false);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(authorityMetaData);
	}
}
