package org.molgenis.data.security;

import org.springframework.security.acls.domain.CumulativePermission;

public class EntityTypePermissionUtils
{
	private EntityTypePermissionUtils()
	{
	}

	public static CumulativePermission getCumulativePermission(EntityTypePermission entityTypePermission)
	{
		CumulativePermission cumulativePermission = new CumulativePermission();

		if (entityTypePermission.equals(EntityTypePermission.WRITEMETA))
		{
			cumulativePermission.set(EntityTypePermission.WRITEMETA)
								.set(EntityTypePermission.WRITE)
								.set(EntityTypePermission.READ)
								.set(EntityTypePermission.COUNT);
		}
		else if (entityTypePermission.equals(EntityTypePermission.WRITE))
		{
			cumulativePermission.set(EntityTypePermission.WRITE)
								.set(EntityTypePermission.READ)
								.set(EntityTypePermission.COUNT);
		}
		else if (entityTypePermission.equals(EntityTypePermission.READ))
		{
			cumulativePermission.set(EntityTypePermission.READ).set(EntityTypePermission.COUNT);
		}
		if (entityTypePermission.equals(EntityTypePermission.COUNT))
		{
			cumulativePermission.set(EntityTypePermission.COUNT);
		}

		return cumulativePermission;
	}
}
