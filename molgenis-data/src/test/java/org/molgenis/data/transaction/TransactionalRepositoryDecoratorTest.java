package org.molgenis.data.transaction;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class TransactionalRepositoryDecoratorTest
{
	private TransactionalRepositoryDecorator<Entity> transactionalRepo;
	private Repository<Entity> decoratedRepo;
	private PlatformTransactionManager transactionManager;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		//noinspection unchecked
		decoratedRepo = mock(Repository.class);
		transactionManager = mock(PlatformTransactionManager.class);
		transactionalRepo = new TransactionalRepositoryDecorator<>(decoratedRepo, transactionManager);
	}

	@Test
	public void delegate() throws Exception
	{
		assertEquals(transactionalRepo.delegate(), decoratedRepo);
	}

	@Test
	public void forEachBatched() throws Exception
	{
		//noinspection unchecked
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 1000;
		transactionalRepo.forEachBatched(consumer, batchSize);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).forEachBatched(consumer, batchSize);
	}

	@Test
	public void forEachBatchedFetch() throws Exception
	{
		Fetch fetch = mock(Fetch.class);
		//noinspection unchecked
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 1000;
		transactionalRepo.forEachBatched(fetch, consumer, batchSize);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).forEachBatched(fetch, consumer, batchSize);
	}

	@Test
	public void count() throws Exception
	{
		transactionalRepo.count();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).count();
	}

	@Test
	public void countQuery() throws Exception
	{
		//noinspection unchecked
		Query<Entity> query = mock(Query.class);
		transactionalRepo.count(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).count(query);
	}

	@Test
	public void findAllQuery() throws Exception
	{
		//noinspection unchecked
		Query<Entity> query = mock(Query.class);
		transactionalRepo.findAll(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findAll(query);
	}

	@Test
	public void findAllStream() throws Exception
	{
		//noinspection unchecked
		Stream<Object> entityIds = mock(Stream.class);
		transactionalRepo.findAll(entityIds);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findAll(entityIds);
	}

	@Test
	public void findAllStreamFetch() throws Exception
	{
		//noinspection unchecked
		Stream<Object> entityIds = mock(Stream.class);
		Fetch fetch = mock(Fetch.class);
		transactionalRepo.findAll(entityIds, fetch);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findAll(entityIds, fetch);
	}

	@Test
	public void findOne() throws Exception
	{
		//noinspection unchecked
		Query<Entity> query = mock(Query.class);
		transactionalRepo.findOne(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findOne(query);
	}

	@Test
	public void findOneById() throws Exception
	{
		Object id = mock(Object.class);
		transactionalRepo.findOneById(id);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findOneById(id);
	}

	@Test
	public void findOneByIdFetch() throws Exception
	{
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		transactionalRepo.findOneById(id, fetch);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).findOneById(id, fetch);
	}

	@Test
	public void aggregate() throws Exception
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		transactionalRepo.aggregate(aggregateQuery);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).aggregate(aggregateQuery);
	}

	@Test
	public void update() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.update(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).update(entity);
	}

	@Test
	public void updateStream() throws Exception
	{
		//noinspection unchecked
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.update(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).update(entityStream);
	}

	@Test
	public void delete() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.delete(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).delete(entity);
	}

	@Test
	public void deleteStream() throws Exception
	{
		//noinspection unchecked
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.delete(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).delete(entityStream);
	}

	@Test
	public void deleteById() throws Exception
	{
		Object id = mock(Object.class);
		transactionalRepo.deleteById(id);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).deleteById(id);
	}

	@Test
	public void deleteAll() throws Exception
	{
		transactionalRepo.deleteAll();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).deleteAll();
	}

	@Test
	public void deleteAllStream() throws Exception
	{
		//noinspection unchecked
		Stream<Object> entityIds = mock(Stream.class);
		transactionalRepo.deleteAll(entityIds);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).deleteAll(entityIds);
	}

	@Test
	public void add() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.add(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).add(entity);
	}

	@Test
	public void addStream() throws Exception
	{
		//noinspection unchecked
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.add(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).add(entityStream);
	}

	@Test
	public void iterator() throws Exception
	{
		transactionalRepo.iterator();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(decoratedRepo).iterator();
	}
}