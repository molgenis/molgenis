package org.molgenis.data.security;

import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;

import static org.molgenis.data.security.PackagePermission.ADD_ENTITY_TYPE;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;

public class PackagePermissionUtils
{
	private PackagePermissionUtils()
	{
	}

	/**
	 * @return whether the current user can add entity types or add packages to this package
	 */
	public static boolean isWritablePackage(Package aPackage, UserPermissionEvaluator userPermissionEvaluator)
	{
		PackageIdentity packageIdentity = new PackageIdentity(aPackage);
		return !MetaUtils.isSystemPackage(aPackage) && (
				userPermissionEvaluator.hasPermission(packageIdentity, ADD_ENTITY_TYPE)
						|| userPermissionEvaluator.hasPermission(packageIdentity, ADD_PACKAGE));
	}
}
