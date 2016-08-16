package org.molgenis.data.cache.l3;

import org.molgenis.data.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionInformation;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

/**
 * Retrieves identifiers from the {@link L3Cache} based on a {@link Query}
 * if {@link RepositoryCapability#CACHEABLE}.
 * <p>
 * Delegates to the underlying {@link Repository}
 */
public class L3CacheRepositoryDecorator extends AbstractRepositoryDecorator
{
	private final L3Cache l3Cache;
	private final boolean cacheable;
	private final Repository<Entity> decoratedRepository;

	private final TransactionInformation transactionInformation;

	private static final int MAX_PAGE_SIZE = 1000;

	public L3CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L3Cache l3Cache,
			TransactionInformation transactionInformation)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l3Cache = requireNonNull(l3Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE));
		this.transactionInformation = transactionInformation;
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	/**
	 * Retrieves a {@link List} of identifiers from the {@link L3Cache} if the
	 * {@link Repository} is cacheable and the {@link Query} is
	 * limited (i.e. contains a pageSize) between 0 and MAX_PAGE_SIZE
	 *
	 * @param query The {@link Query}
	 * @return A stream of {@link Entity}
	 */
	@Override
	public Stream<Entity> findAll(Query<Entity> query)
	{
		if (transactionInformation.isRepositoryCompletelyClean(getName()) && cacheable && query.getPageSize() > 0
				&& query.getPageSize() <= MAX_PAGE_SIZE)
		{
			List<Object> ids = l3Cache.get(delegate(), query);
			return delegate().findAll(ids.stream(), query.getFetch());
		}
		return delegate().findAll(query);
	}

	/**
	 * Retrieves a single identifier from the {@link L3Cache} if the
	 * {@link Repository} is cacheable
	 *
	 * @param query The {@link Query}
	 * @return A single {@link Entity} or null if not found
	 */
	@Override
	public Entity findOne(Query<Entity> query)
	{
		if (transactionInformation.isRepositoryCompletelyClean(getName()) && cacheable)
		{
			// Some query parameters are irrelevant for findOne
			QueryImpl<Entity> newQuery = new QueryImpl<>(query);
			newQuery.setPageSize(1);
			newQuery.setSort(new Sort());

			List<Object> ids = l3Cache.get(delegate(), newQuery);
			if (!ids.isEmpty())
			{
				return delegate().findOneById(ids.get(0), query.getFetch());
			}
			else
			{
				return null;
			}
		}
		return delegate().findOne(query);
	}
}
