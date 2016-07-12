package org.molgenis.data.cache.l2;

import com.google.common.collect.Iterators;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

/**
 * Adds, removes and retrieves entities from the {@link L2Cache} when a {@link Repository} is {@link RepositoryCapability#CACHEABLE}.
 * Delegates to the underlying repository when an action is not supported by the cache or when the cache doesn't contain
 * the needed entity.
 */
public class L2CacheRepositoryDecorator extends AbstractRepositoryDecorator
{
	private static final int ID_BATCH_SIZE = 1000;

	private final L2Cache l2Cache;

	private final boolean cacheable;

	private final Repository<Entity> decoratedRepository;

	public L2CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L2Cache l2Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l2Cache = requireNonNull(l2Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE));
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public Entity findOneById(Object id)
	{
		if (!cacheable) //TODO and entity id not touched in current transaction
		{
			return delegate().findOneById(id);
		}
		return l2Cache.get(delegate(), id);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (cacheable) //TODO split on entities touched and untouched by current transaction
		{
			Iterator<List<Object>> idBatches = partition(ids.iterator(), ID_BATCH_SIZE);
			Iterator<List<Entity>> entityBatches = Iterators.transform(idBatches, this::findAllBatch);
			return stream(spliteratorUnknownSize(entityBatches, SORTED | ORDERED), false).flatMap(List::stream);
		}
		return decoratedRepository.findAll(ids);
	}

	/**
	 * Retrieves a batch of Entity IDs from the L2Cache or underlying repository.
	 *
	 * @param ids list of entity IDs to retrieve
	 * @return List of {@link Entity}s, missing ones excluded.
	 */
	private List<Entity> findAllBatch(List<Object> ids)
	{
		return l2Cache.getBatch(delegate(), ids);
	}
}
