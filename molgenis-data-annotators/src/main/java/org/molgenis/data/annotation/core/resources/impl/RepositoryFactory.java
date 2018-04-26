package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

import java.io.File;
import java.io.IOException;

public interface RepositoryFactory
{
	Repository<Entity> createRepository(File file) throws IOException;
}
