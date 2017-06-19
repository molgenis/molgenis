package org.molgenis.data.cache.l2;

import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.molgenis.data.*;
import org.molgenis.data.transaction.TransactionInformation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

/**
 * Adds, removes and retrieves entities from the {@link L2Cache} when a {@link Repository} is
 * {@link RepositoryCapability#CACHEABLE}.
 * <p>
 * Delegates to the underlying repository when an action is not supported by the cache or when the cache doesn't contain
 * the needed entity.
 */
public class L2CacheRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final int ID_BATCH_SIZE = 1000;

	private final L2Cache l2Cache;

	private final boolean cacheable;

	private final Repository<Entity> decoratedRepository;

	private final TransactionInformation transactionInformation;

	public L2CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L2Cache l2Cache,
			TransactionInformation transactionInformation)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l2Cache = requireNonNull(l2Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE));
		this.transactionInformation = transactionInformation;
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	/**
	 * Retrieves a single entity by id.
	 *
	 * @param id the entity's ID value
	 * @return the retrieved Entity, or null if not present.
	 */
	@Override
	public Entity findOneById(Object id)
	{
		if (cacheable && !transactionInformation.isEntireRepositoryDirty(getEntityType())
				&& !transactionInformation.isEntityDirty(EntityKey.create(getEntityType(), id)))
		{
			return l2Cache.get(delegate(), id);
		}
		return delegate().findOneById(id);
	}

	/**
	 * Retrieves multiple entities by id.
	 * <p>
	 * If the repository is cacheable and the current transaction hasn't completely dirtied it, will split the stream
	 * into batches and load the batches through {@link #findAllBatch(List)}.
	 * <p>
	 * Otherwise, will delegate this call to the decorated repository.
	 *
	 * @param ids {@link Stream} of ids to retrieve
	 * @return {@link Stream} of retrieved {@link Entity}s, missing ones excluded
	 */
	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (cacheable && !transactionInformation.isEntireRepositoryDirty(getEntityType()))
		{
			Iterator<List<Object>> idBatches = partition(ids.iterator(), ID_BATCH_SIZE);
			Iterator<List<Entity>> entityBatches = Iterators.transform(idBatches, this::findAllBatch);
			return stream(spliteratorUnknownSize(entityBatches, SORTED | ORDERED), false).flatMap(List::stream);
		}
		return delegate().findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return findAll(ids);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return findOneById(id);
	}

	/**
	 * Retrieves a batch of Entity IDs.
	 * <p>
	 * If currently in transaction, splits the ids into those that have been dirtied in the current transaction
	 * and those that have been left untouched. The untouched ids are loaded through the cache, the dirtied ids
	 * are loaded from the decorated repository directly.
	 *
	 * @param ids list of entity IDs to retrieve
	 * @return List of {@link Entity}s, missing ones excluded.
	 */
	private List<Entity> findAllBatch(List<Object> ids)
	{
		String entityTypeId = getEntityType().getId();
		Multimap<Boolean, Object> partitionedIds = Multimaps.index(ids,
				id -> transactionInformation.isEntityDirty(EntityKey.create(entityTypeId, id)));
		Collection<Object> cleanIds = partitionedIds.get(false);
		Collection<Object> dirtyIds = partitionedIds.get(true);

		Map<Object, Entity> result = newHashMap(
				uniqueIndex(l2Cache.getBatch(delegate(), cleanIds), Entity::getIdValue));
		result.putAll(delegate().findAll(dirtyIds.stream()).collect(toMap(Entity::getIdValue, e -> e)));

		return ids.stream().filter(result::containsKey).map(result::get).collect(toList());
	}
}
