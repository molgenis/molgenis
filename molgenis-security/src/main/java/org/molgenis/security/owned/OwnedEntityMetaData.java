package org.molgenis.security.owned;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Defines an abstract EntityMetaData for entities that have an 'owner'.
 * <p>
 * These entities can only be viewed/updated/deleted by it's creator.
 * <p>
 * Defines one attribute 'ownerUsername', that is the username of the owner. You can extend this EntityMetaData to
 * inherit this behavior.
 */
@Component
public class OwnedEntityMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "Owned";
	public static final String OWNED = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String OWNER_USERNAME = "ownerUsername";

	private final SecurityPackage securityPackage;

	@Autowired
	public OwnedEntityMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setAbstract(true);

		setLabel("Owned");

		addAttribute(OWNER_USERNAME).setDataType(STRING).setVisible(false);
	}
}
