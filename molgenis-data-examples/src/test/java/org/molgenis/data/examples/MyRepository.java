package org.molgenis.data.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
	public Integer add(Stream<? extends Entity> newEntities)
	{
		return add(newEntities.iterator());
	}

	private Integer add(Iterator<? extends Entity> it)
	{
		int i = 0;
		while (it.hasNext())
		{
			entities.add(it.next());
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
