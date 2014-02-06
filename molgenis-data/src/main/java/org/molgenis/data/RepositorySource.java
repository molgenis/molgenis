package org.molgenis.data;

import java.util.List;

public interface RepositorySource
{
	List<Repository> getRepositories();

	Repository getRepository(String name);
}
