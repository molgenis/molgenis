package org.molgenis.data.transaction;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Repository decorator that wraps CRUD operations in a (read-only) transaction. Classes that extend from
 * {@link AbstractRepositoryDecorator} might not be managed by Spring, so {@link TransactionTemplate} is used instead
 * of the {@link Transactional} annotation.
 *
 * @param <E> entity type
 */
public class TransactionalRepositoryDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private final Repository<E> decoratedRepo;
	private final PlatformTransactionManager transactionManager;

	public TransactionalRepositoryDecorator(Repository<E> decoratedRepo, PlatformTransactionManager transactionManager)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.transactionManager = requireNonNull(transactionManager);
	}

	@Override
	protected Repository<E> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void forEachBatched(Consumer<List<E>> consumer, int batchSize)
	{
		createReadonlyTransactionTemplate().execute((status) ->
		{
			decoratedRepo.forEachBatched(consumer, batchSize);
			return null;
		});
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize)
	{
		createReadonlyTransactionTemplate().execute((status) ->
		{
			decoratedRepo.forEachBatched(fetch, consumer, batchSize);
			return null;
		});
	}

	@Override
	public long count()
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.count());
	}

	@Override
	public long count(Query<E> q)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.count(q));
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findAll(q));
	}

	@Override
	public E findOne(Query<E> q)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findOne(q));
	}

	@Override
	public E findOneById(Object id)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findOneById(id));
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findOneById(id, fetch));
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findAll(ids));
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.findAll(ids, fetch));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.aggregate(aggregateQuery));
	}

	@Override
	public void update(E entity)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.update(entity);
			return null;
		});
	}

	@Override
	public void update(Stream<E> entities)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.update(entities);
			return null;
		});
	}

	@Override
	public void delete(E entity)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.delete(entity);
			return null;
		});
	}

	@Override
	public void delete(Stream<E> entities)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.delete(entities);
			return null;
		});
	}

	@Override
	public void deleteById(Object id)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.deleteById(id);
			return null;
		});
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.deleteAll(ids);
			return null;
		});
	}

	@Override
	public void deleteAll()
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.deleteAll();
			return null;
		});
	}

	@Override
	public void add(E entity)
	{
		createWriteTransactionTemplate().execute((status) ->
		{
			decoratedRepo.add(entity);
			return null;
		});
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return createWriteTransactionTemplate().execute((status) -> decoratedRepo.add(entities));
	}

	@Override
	public Iterator<E> iterator()
	{
		return createReadonlyTransactionTemplate().execute((status) -> decoratedRepo.iterator());
	}

	private TransactionTemplate createWriteTransactionTemplate()
	{
		return new TransactionTemplate(transactionManager);
	}

	private TransactionTemplate createReadonlyTransactionTemplate()
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setReadOnly(true);
		return transactionTemplate;
	}
}
