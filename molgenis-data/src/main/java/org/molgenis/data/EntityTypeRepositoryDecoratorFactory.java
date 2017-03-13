package org.molgenis.data;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.support.StaticEntity;

/**
 * Repository decorator factory that creates decorated {@link Repository repositories} for specific {@link SystemEntityType system entity types}.
 *
 * @see RepositoryDecoratorFactory
 */
public interface EntityTypeRepositoryDecoratorFactory<E extends StaticEntity>
{
	/**
	 * Creates a decorated repository based on the given {@link Repository}
	 *
	 * @param repository undecorated repository
	 * @return decorated repository
	 */
	Repository<E> createDecoratedRepository(Repository<E> repository);
}
