package org.molgenis.security.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class ActionPermissionMappingMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ActionPermissionMapping";
	public static final String ACTION_PERMISSION_MAPPING = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String ACTION = "action";
	public static final String PERMISSIONS = "permissions";
	public static final String NAME = "name";

	private final Package securityPackage;
	private final ActionMetadata actionMetadata;
	private final PermissionMetadata permissionMetadata;

	public ActionPermissionMappingMetadata(Package securityPackage, ActionMetadata actionMetadata,
			PermissionMetadata permissionMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.actionMetadata = requireNonNull(actionMetadata);
		this.permissionMetadata = requireNonNull(permissionMetadata);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setLabel("Action Permission Mapping");
		setDescription("Action Permission Mapping");

		addAttribute(ID, ROLE_ID).setLabel("Identifier").setAuto(true).setVisible(false);
		addAttribute(NAME, ROLE_LABEL).setLabel("Name").setDataType(AttributeType.STRING);
		addAttribute(ACTION).setLabel("Action").setDataType(AttributeType.XREF).setRefEntity(actionMetadata);
		addAttribute(PERMISSIONS).setLabel("Permissions")
								 .setDataType(AttributeType.MREF)
								 .setRefEntity(permissionMetadata);
	}
}