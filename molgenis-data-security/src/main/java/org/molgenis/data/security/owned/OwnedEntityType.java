package org.molgenis.data.security.owned;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

/**
 * Defines an abstract EntityType for entities that have an 'owner'.
 * <p>
 * These entities can only be viewed/updated/deleted by it's creator.
 * <p>
 * Defines one attribute 'ownerUsername', that is the username of the owner. You can extend this EntityType to
 * inherit this behavior.
 */
@Component
public class OwnedEntityType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Owned";
	public static final String OWNED = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String OWNER_USERNAME = "ownerUsername";

	private final SecurityPackage securityPackage;

	public OwnedEntityType(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setLabel("Owned");
		setPackage(securityPackage);

		setAbstract(true);
		addAttribute(OWNER_USERNAME).setDataType(STRING).setVisible(false);
	}
}
