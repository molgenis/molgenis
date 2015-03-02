package org.molgenis.data;

import java.util.Iterator;
import java.util.Map;

/**
 * Adds auto generated id to an entity if it does not have an id.
 * 
 * Keeps an index of id by rownr so multiple calls to iterator() returns the same id.
 */
public class AutoIdEntityIterableDecorator implements Iterable<Entity>
{
	private final EntityMetaData entityMetaData;
	private final Iterable<? extends Entity> entities;
	private final Map<Integer, Object> ids;
	private final IdGenerator idGenerator;

	public AutoIdEntityIterableDecorator(EntityMetaData entityMetaData, Iterable<? extends Entity> entities,
			IdGenerator idGenerator, Map<Integer, Object> idMap)
	{
		this.entityMetaData = entityMetaData;
		this.entities = entities;
		this.idGenerator = idGenerator;
		this.ids = idMap;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		Iterator<? extends Entity> it = entities.iterator();

		return new Iterator<Entity>()
		{
			int i = 0;

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity entity = it.next();
				Object id = entity.getIdValue();
				if (id == null)
				{
					id = ids.get(i);
					if (id == null)
					{
						id = idGenerator.generateId();
						ids.put(i, id);
					}

					entity.set(entityMetaData.getIdAttribute().getName(), id);
				}

				i++;
				return entity;
			}

		};
	}

}
