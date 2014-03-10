package org.molgenis.data;

import java.io.Closeable;
import java.util.List;

public interface RepositorySource extends Closeable
{
	List<Repository> getRepositories();

	Repository getRepository(String name);
}
