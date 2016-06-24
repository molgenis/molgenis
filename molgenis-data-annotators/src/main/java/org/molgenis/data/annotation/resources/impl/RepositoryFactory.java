package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Repository;

public interface RepositoryFactory
{
	Repository createRepository(File file) throws IOException;
}
