package org.molgenis.data.security;

import org.springframework.security.acls.domain.CumulativePermission;

public class EntityPermissionUtils
{
	private EntityPermissionUtils()
	{
	}

	public static CumulativePermission getCumulativePermission(EntityPermission entityPermission)
	{
		CumulativePermission cumulativePermission = new CumulativePermission();

		if (entityPermission.equals(EntityPermission.WRITE))
		{
			cumulativePermission.set(EntityPermission.WRITE).set(EntityPermission.READ).set(EntityPermission.COUNT);
		}
		else if (entityPermission.equals(EntityPermission.READ))
		{
			cumulativePermission.set(EntityPermission.READ).set(EntityPermission.COUNT);
		}
		if (entityPermission.equals(EntityPermission.COUNT))
		{
			cumulativePermission.set(EntityPermission.COUNT);
		}

		return cumulativePermission;
	}
}
