package org.molgenis.data;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.support.StaticEntity;

import static java.util.Objects.requireNonNull;

/**
 * Repository decorator factory for a specific {@link SystemEntityType}.
 *
 * @param <E>
 * @param <M>
 */
public abstract class StaticEntityRepositoryDecoratorFactory<E extends StaticEntity, M extends SystemEntityType>
		implements EntityTypeRepositoryDecoratorFactory<E>
{
	private final M entityType;

	public StaticEntityRepositoryDecoratorFactory(M entityType)
	{
		this.entityType = requireNonNull(entityType);
	}

	public M getEntityType()
	{
		return entityType;
	}
}
