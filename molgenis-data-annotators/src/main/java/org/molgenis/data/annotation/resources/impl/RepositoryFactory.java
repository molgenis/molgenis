package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public interface RepositoryFactory
{
	Repository<Entity> createRepository(File file) throws IOException;
}
