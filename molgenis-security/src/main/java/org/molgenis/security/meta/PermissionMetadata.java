package org.molgenis.security.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class PermissionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Permission";
	public static final String PERMISSION = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String MASK = "mask";
	public static final String CODE = "code";

	private final Package securityPackage;

	public PermissionMetadata(Package securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setLabel("Permission");
		setDescription("Permission");

		addAttribute(NAME, ROLE_ID).setLabel("Name");
		addAttribute(MASK).setLabel("Mask").setDataType(AttributeType.INT);
		addAttribute(CODE).setLabel("Code").setDataType(AttributeType.STRING);
	}
}

