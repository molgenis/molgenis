package org.molgenis.data.decorator;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

import java.util.stream.Stream;

public interface DynamicRepositoryDecoratorRegistry
{
	void addFactory(DynamicRepositoryDecoratorFactory factory);

	Stream<String> getFactoryIds();

	DynamicRepositoryDecoratorFactory getFactory(String id);

	Repository<Entity> decorate(Repository<Entity> repository);
}
