package org.molgenis.data.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;

import com.google.common.collect.Sets;

public class MyRepository extends AbstractRepository
{
	private final EntityMetaData entityMetaData;
	private final List<Entity> entities = new ArrayList<>();

	public MyRepository(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(RepositoryCapability.WRITABLE);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public void add(Entity entity)
	{
		entities.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> newEntities)
	{
		int i = 0;
		for (Entity e : newEntities)
		{
			entities.add(e);
			i++;
		}

		return i;
	}

	@Override
	public long count()
	{
		return entities.size();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.iterator();
	}

}
