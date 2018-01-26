package org.molgenis.data.decorator;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;

/**
 * Repository decorator factory that creates decorated {@link Repository repositories} for specific entity types.
 * Used to decorate (system) entity types dynamically.
 *
 * @see RepositoryDecoratorFactory
 */
public interface DynamicRepositoryDecoratorFactory<E extends Entity, M extends EntityType>
{
	String getId();

	String getLabel();

	String getDescription();

	/**
	 * Creates a decorated repository based on the given {@link Repository}
	 *
	 * @param repository undecorated repository
	 * @return decorated repository
	 */
	Repository<E> createDecoratedRepository(Repository<E> repository);
}