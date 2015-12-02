package org.molgenis.data.i18n;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;

public class LanguageRepositoryDecorator implements Repository
{
	private final Repository decorated;
	private final DataService dataService;

	public LanguageRepositoryDecorator(Repository decorated, DataService dataService)
	{
		this.decorated = decorated;
		this.dataService = dataService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decorated.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query q)
	{
		return decorated.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decorated.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decorated.findOne(id, fetch);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decorated.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		decorated.delete(entity);

		String languageCode = entity.getString(LanguageMetaData.CODE);

		AttributeMetaData existing = AttributeMetaDataMetaData.INSTANCE.getAttribute(AttributeMetaDataMetaData.LABEL
				+ '.' + languageCode);

		if (existing != null)
		{
			AttributeMetaDataMetaData.INSTANCE.removeAttributeMetaData(existing);
			dataService.getMeta().getDefaultBackend()
					.deleteAttribute(AttributeMetaDataMetaData.ENTITY_NAME, existing.getName());
		}
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Entity entity = findOne(id);
		if (entity != null) delete(entity);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		delete(this);
	}

	@Override
	public void add(Entity entity)
	{
		// Add language
		decorated.add(entity);

		// Create new label and description attributes for the added language
		String languageCode = entity.getString(LanguageMetaData.CODE);

		AttributeMetaData attr = new DefaultAttributeMetaData(AttributeMetaDataMetaData.LABEL + '.' + languageCode)
				.setNillable(true);

		// Add the attribute to the attributes table
		dataService.getMeta().getDefaultBackend().addAttribute(AttributeMetaDataMetaData.ENTITY_NAME, attr);

		// Update AttributeMetaDataMetaData
		AttributeMetaDataMetaData.INSTANCE.addAttributeMetaData(attr);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		int count = 0;
		for (Entity e : entities)
		{
			add(e);
			count++;
		}

		return count;
	}

	@Override
	public void flush()
	{
		decorated.flush();
	}

	@Override
	public void clearCache()
	{
		decorated.clearCache();
	}

	@Override
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decorated.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decorated.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decorated.removeEntityListener(entityListener);
	}

}
