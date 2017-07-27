package org.molgenis.data.transaction;

import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TransactionalRepositoryDecoratorTest
{
	private TransactionalRepositoryDecorator<Entity> transactionalRepo;
	private Repository<Entity> delegateRepository;
	private PlatformTransactionManager transactionManager;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		transactionManager = mock(PlatformTransactionManager.class);
		transactionalRepo = new TransactionalRepositoryDecorator<>(delegateRepository, transactionManager);
	}

	@Test
	public void forEachBatched() throws Exception
	{
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 1000;
		transactionalRepo.forEachBatched(consumer, batchSize);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).forEachBatched(consumer, batchSize);
	}

	@Test
	public void forEachBatchedFetch() throws Exception
	{
		Fetch fetch = mock(Fetch.class);
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		int batchSize = 1000;
		transactionalRepo.forEachBatched(fetch, consumer, batchSize);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
	}

	@Test
	public void count() throws Exception
	{
		transactionalRepo.count();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).count();
	}

	@Test
	public void countQuery() throws Exception
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		transactionalRepo.count(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).count(query);
	}

	@Test
	public void findAllQuery() throws Exception
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		transactionalRepo.findAll(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findAll(query);
	}

	@Test
	public void findAllStream() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Object> entityIds = mock(Stream.class);
		transactionalRepo.findAll(entityIds);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findAll(entityIds);
	}

	@Test
	public void findAllStreamFetch() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Object> entityIds = mock(Stream.class);
		Fetch fetch = mock(Fetch.class);
		transactionalRepo.findAll(entityIds, fetch);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findAll(entityIds, fetch);
	}

	@Test
	public void findOne() throws Exception
	{
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		transactionalRepo.findOne(query);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findOne(query);
	}

	@Test
	public void findOneById() throws Exception
	{
		Object id = mock(Object.class);
		transactionalRepo.findOneById(id);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findOneById(id);
	}

	@Test
	public void findOneByIdFetch() throws Exception
	{
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		transactionalRepo.findOneById(id, fetch);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).findOneById(id, fetch);
	}

	@Test
	public void aggregate() throws Exception
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		transactionalRepo.aggregate(aggregateQuery);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).aggregate(aggregateQuery);
	}

	@Test
	public void update() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.update(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).update(entity);
	}

	@Test
	public void updateStream() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.update(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).update(entityStream);
	}

	@Test
	public void delete() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.delete(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).delete(entity);
	}

	@Test
	public void deleteStream() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.delete(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).delete(entityStream);
	}

	@Test
	public void deleteById() throws Exception
	{
		Object id = mock(Object.class);
		transactionalRepo.deleteById(id);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).deleteById(id);
	}

	@Test
	public void deleteAll() throws Exception
	{
		transactionalRepo.deleteAll();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).deleteAll();
	}

	@Test
	public void deleteAllStream() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Object> entityIds = mock(Stream.class);
		transactionalRepo.deleteAll(entityIds);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).deleteAll(entityIds);
	}

	@Test
	public void add() throws Exception
	{
		Entity entity = mock(Entity.class);
		transactionalRepo.add(entity);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).add(entity);
	}

	@Test
	public void addStream() throws Exception
	{
		@SuppressWarnings("unchecked")
		Stream<Entity> entityStream = mock(Stream.class);
		transactionalRepo.add(entityStream);
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).add(entityStream);
	}

	@Test
	public void iterator() throws Exception
	{
		transactionalRepo.iterator();
		verify(transactionManager).getTransaction(any(TransactionDefinition.class));
		verify(delegateRepository).iterator();
	}
}