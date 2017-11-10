package org.molgenis.data.decorator;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public interface DynamicRepositoryDecoratorRegistry
{
	void addFactory(DynamicRepositoryDecoratorFactory factory);

	Repository<Entity> decorate(Repository<Entity> repository);
}
