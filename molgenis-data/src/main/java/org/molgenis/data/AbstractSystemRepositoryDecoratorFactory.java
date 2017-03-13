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
public abstract class AbstractSystemRepositoryDecoratorFactory<E extends StaticEntity, M extends SystemEntityType>
		implements SystemRepositoryDecoratorFactory<E, M>
{
	private final M entityType;

	public AbstractSystemRepositoryDecoratorFactory(M entityType)
	{
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public M getEntityType()
	{
		return entityType;
	}
}
