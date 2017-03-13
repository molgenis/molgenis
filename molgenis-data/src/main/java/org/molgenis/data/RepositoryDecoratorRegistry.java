package org.molgenis.data;

public interface RepositoryDecoratorRegistry
{
	void addFactory(String entityName, RepositoryDecoratorFactory factory);

	Repository<Entity> decorate(Repository<Entity> repository);
}
