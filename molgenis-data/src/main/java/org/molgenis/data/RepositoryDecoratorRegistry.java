package org.molgenis.data;

public interface RepositoryDecoratorRegistry
{
	void addFactory(StaticEntityRepositoryDecoratorFactory factory);

	Repository<Entity> decorate(Repository<Entity> repository);
}
