package org.molgenis.data.security.auth;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class GroupMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Group";
	public static final String GROUP = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ROLES = "roles";
	public static final String PUBLIC = "public";
	public static final String ROOT_PACKAGE = "rootPackage";

	private final SecurityPackage securityPackage;
	private final RoleMetadata roleMetadata;
	private final PackageMetadata packageMetadata;

	public GroupMetadata(SecurityPackage securityPackage, RoleMetadata roleMetadata, PackageMetadata packageMetadata)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.roleMetadata = requireNonNull(roleMetadata);
		this.packageMetadata = requireNonNull(packageMetadata);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setLabel("Group");
		setDescription("A number of people that work together or share certain beliefs.");

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(NAME, ROLE_LABEL).setLabel("Name").setNillable(false);
		addAttribute(DESCRIPTION).setLabel("Description");
		addAttribute(PUBLIC).setDataType(BOOL)
							.setLabel("Publicly visible")
							.setDescription("Indication if this group is publicly visible.")
							.setNillable(false)
							.setDefaultValue("true");
		addAttribute(ROLES).setDataType(MREF)
						   .setRefEntity(roleMetadata)
						   .setLabel("Roles")
						   .setDescription("Roles a User can have within this Group");
		addAttribute(ROOT_PACKAGE).setDataType(AttributeType.XREF)
								  .setRefEntity(packageMetadata)
								  .setLabel("Root package")
								  .setDescription("Package where this Group's resources reside.")
								  .setNillable(false);
	}
}