package org.molgenis.data.mem;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultEntityMetaData;

public class InMemoryRepository implements CrudRepository
{
	private DefaultEntityMetaData metadata;
	private Map<Object, Entity> entities = new LinkedHashMap<Object, Entity>();

	public InMemoryRepository(EntityMetaData entityMetaData)
	{
		this.metadata = new DefaultEntityMetaData(entityMetaData);
	}

	@Override
	public String getName()
	{
		return metadata.getName();
	}

	@Override
	public DefaultEntityMetaData getEntityMetaData()
	{
		return metadata;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.values().iterator();
	}

	@Override
	public String getUrl()
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return entities.values().stream().map(entity -> (E) entity).collect(Collectors.toList());
	}

	@Override
	public Query query()
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public long count(Query q)
	{
		return entities.size();
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Entity findOne(Query q)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Entity findOne(Object id)
	{
		return entities.get(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public long count()
	{
		return entities.size();
	}

	@Override
	public void update(Entity entity)
	{
		Object id = getId(entity);
		if (!entities.containsKey(id))
		{
			throw new IllegalStateException("No entity with id " + id);
		}
		entities.put(id, entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		records.forEach(this::update);
	}

	private Object getId(Entity entity)
	{
		return entity.get(metadata.getIdAttribute().getName());
	}

	@Override
	public void delete(Entity entity)
	{
		deleteById(getId(entity));
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		entities.remove(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		entities.clear();
	}

	@Override
	public void add(Entity entity)
	{
		Object id = getId(entity);
		if (entities.containsKey(id))
		{
			throw new IllegalStateException("Entity with id " + id + " already exists");
		}
		entities.put(id, entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		int i = 0;
		for (Entity entity : entities)
		{
			add(entity);
			i++;
		}
		return i;
	}

	@Override
	public void flush()
	{

	}

	@Override
	public void clearCache()
	{

	}

}
