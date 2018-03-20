package org.molgenis.data.security;

import org.springframework.security.acls.domain.CumulativePermission;

public class RepositoryPermissionUtils
{
	private RepositoryPermissionUtils()
	{
	}

	public static CumulativePermission getCumulativePermission(RepositoryPermission repositoryPermission)
	{
		CumulativePermission cumulativePermission = new CumulativePermission();

		if (repositoryPermission.equals(RepositoryPermission.WRITEMETA))
		{
			cumulativePermission.set(RepositoryPermission.WRITEMETA)
								.set(RepositoryPermission.WRITE)
								.set(RepositoryPermission.READ)
								.set(RepositoryPermission.COUNT);
		}
		else if (repositoryPermission.equals(RepositoryPermission.WRITE))
		{
			cumulativePermission.set(RepositoryPermission.WRITE)
								.set(RepositoryPermission.READ)
								.set(RepositoryPermission.COUNT);
		}
		else if (repositoryPermission.equals(RepositoryPermission.READ))
		{
			cumulativePermission.set(RepositoryPermission.READ).set(RepositoryPermission.COUNT);
		}
		if (repositoryPermission.equals(RepositoryPermission.COUNT))
		{
			cumulativePermission.set(RepositoryPermission.COUNT);
		}

		return cumulativePermission;
	}
}
