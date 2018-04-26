package org.molgenis.data;

import org.molgenis.data.meta.SystemEntityType;

/**
 * Repository decorator factory that creates decorated {@link Repository repositories} for specific {@link SystemEntityType system entity types}.
 *
 * @see RepositoryDecoratorFactory
 */
public interface SystemRepositoryDecoratorFactory<E extends Entity, M extends SystemEntityType>
{
	/**
	 * Returns system entity type for which repository decorators can be created.
	 *
	 * @return system entity type
	 */
	M getEntityType();

	/**
	 * Creates a decorated repository based on the given {@link Repository}
	 *
	 * @param repository undecorated repository
	 * @return decorated repository
	 */
	Repository<E> createDecoratedRepository(Repository<E> repository);
}
