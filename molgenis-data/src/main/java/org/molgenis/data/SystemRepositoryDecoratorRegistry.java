package org.molgenis.data;

public interface SystemRepositoryDecoratorRegistry
{
	void addFactory(SystemRepositoryDecoratorFactory factory);

	Repository<Entity> decorate(Repository<Entity> repository);
}
