package org.molgenis.data.security.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.meta.SecurityMetadataPackage.PACKAGE_SECURITY_METADATA;

@Component
public class RowLevelSecuredMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "RowLevelSecured";
	public static final String ROW_LEVEL_SECURED = PACKAGE_SECURITY_METADATA + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IS_ROW_LEVEL_SECURED = "rowLevelSecured";
	public static final String ENTITYTYPE_ID = "entityTypeId";

	public static final String ID = "id";

	private final Package securityMetadataPackage;

	public RowLevelSecuredMetadata(SecurityMetadataPackage securityMetadataPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY_METADATA);
		this.securityMetadataPackage = requireNonNull(securityMetadataPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityMetadataPackage);

		setLabel("Row Level Secured");
		setDescription("Administration of which entity types are row level secured");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
		addAttribute(ENTITYTYPE_ID, ROLE_LABEL).setLabel("EntityType Id")
											   .setDataType(AttributeType.STRING)
											   .setNillable(false);
		addAttribute(IS_ROW_LEVEL_SECURED).setDataType(AttributeType.BOOL)
										  .setLabel("Row Level Secured")
										  .setNillable(false);
	}
}