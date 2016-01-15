package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.AggregateAnonymizerImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;

public class RepositorySecurityDecorator implements Repository
{
	private final Repository decoratedRepository;
	private final AppSettings appSettings;
	private final AggregateAnonymizer aggregateAnonymizer;

	public RepositorySecurityDecorator(Repository decoratedRepository, AppSettings appSettings)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.appSettings = requireNonNull(appSettings);
		this.aggregateAnonymizer = new AggregateAnonymizerImpl();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public long count()
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		return decoratedRepository.add(entities);
	}

	@Override
	public void flush()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.clearCache();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);

		Integer threshold = appSettings.getAggregateThreshold();

		AggregateResult result = decoratedRepository.aggregate(aggregateQuery);
		if (threshold != null && threshold > 0)
		{
			result = aggregateAnonymizer.anonymize(result, threshold);
		}
		return result;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public void rebuildIndex()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void create()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.drop();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		decoratedRepository.removeEntityListener(entityListener);
	}
}
