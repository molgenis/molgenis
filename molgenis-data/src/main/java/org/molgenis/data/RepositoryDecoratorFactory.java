package org.molgenis.data;

/**
 * Repository decorator factory that creates decorated {@link Repository repositories}.
 *
 * @see SystemRepositoryDecoratorFactory
 */
public interface RepositoryDecoratorFactory
{
	/**
	 * Creates a decorated repository based on the given {@link Repository}
	 *
	 * @param repository undecorated repository
	 * @return decorated repository
	 */
	Repository<Entity> createDecoratedRepository(Repository<Entity> repository);
}
