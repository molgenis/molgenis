package org.molgenis.data.security;

import org.springframework.security.acls.domain.CumulativePermission;

public class PackagePermissionUtils
{
	private PackagePermissionUtils()
	{
	}

	public static CumulativePermission getCumulativePermission(PackagePermission entityTypePermission)
	{
		CumulativePermission cumulativePermission = new CumulativePermission();

		if (entityTypePermission.equals(PackagePermission.WRITEMETA))
		{
			cumulativePermission.set(PackagePermission.WRITEMETA)
								.set(PackagePermission.WRITE)
								.set(PackagePermission.READ)
								.set(PackagePermission.COUNT);
		}
		else if (entityTypePermission.equals(PackagePermission.WRITE))
		{
			cumulativePermission.set(PackagePermission.WRITE).set(PackagePermission.READ).set(PackagePermission.COUNT);
		}
		else if (entityTypePermission.equals(PackagePermission.READ))
		{
			cumulativePermission.set(PackagePermission.READ).set(PackagePermission.COUNT);
		}
		if (entityTypePermission.equals(PackagePermission.COUNT))
		{
			cumulativePermission.set(PackagePermission.COUNT);
		}

		return cumulativePermission;
	}
}
