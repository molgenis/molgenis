package org.molgenis.data.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

class TransactionalRepositoryDecoratorTest {
  private TransactionalRepositoryDecorator<Entity> transactionalRepo;
  private Repository<Entity> delegateRepository;
  private PlatformTransactionManager transactionManager;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    transactionManager = mock(PlatformTransactionManager.class);
    transactionalRepo =
        new TransactionalRepositoryDecorator<>(delegateRepository, transactionManager);
  }

  @Test
  void forEachBatched() {
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = mock(Consumer.class);
    int batchSize = 1000;
    transactionalRepo.forEachBatched(consumer, batchSize);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).forEachBatched(consumer, batchSize);
  }

  @Test
  void forEachBatchedFetch() {
    Fetch fetch = mock(Fetch.class);
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = mock(Consumer.class);
    int batchSize = 1000;
    transactionalRepo.forEachBatched(fetch, consumer, batchSize);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).forEachBatched(fetch, consumer, batchSize);
  }

  @Test
  void count() {
    transactionalRepo.count();
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).count();
  }

  @Test
  void countQuery() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    transactionalRepo.count(query);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).count(query);
  }

  @Test
  void findAllQuery() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    transactionalRepo.findAll(query);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findAll(query);
  }

  @Test
  void findAllStream() {
    @SuppressWarnings("unchecked")
    Stream<Object> entityIds = mock(Stream.class);
    transactionalRepo.findAll(entityIds);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findAll(entityIds);
  }

  @Test
  void findAllStreamFetch() {
    @SuppressWarnings("unchecked")
    Stream<Object> entityIds = mock(Stream.class);
    Fetch fetch = mock(Fetch.class);
    transactionalRepo.findAll(entityIds, fetch);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findAll(entityIds, fetch);
  }

  @Test
  void findOne() {
    @SuppressWarnings("unchecked")
    Query<Entity> query = mock(Query.class);
    transactionalRepo.findOne(query);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findOne(query);
  }

  @Test
  void findOneById() {
    Object id = mock(Object.class);
    transactionalRepo.findOneById(id);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findOneById(id);
  }

  @Test
  void findOneByIdFetch() {
    Object id = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    transactionalRepo.findOneById(id, fetch);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).findOneById(id, fetch);
  }

  @Test
  void aggregate() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    transactionalRepo.aggregate(aggregateQuery);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).aggregate(aggregateQuery);
  }

  @Test
  void update() {
    Entity entity = mock(Entity.class);
    transactionalRepo.update(entity);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).update(entity);
  }

  @Test
  void updateStream() {
    @SuppressWarnings("unchecked")
    Stream<Entity> entityStream = mock(Stream.class);
    transactionalRepo.update(entityStream);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).update(entityStream);
  }

  @Test
  void delete() {
    Entity entity = mock(Entity.class);
    transactionalRepo.delete(entity);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).delete(entity);
  }

  @Test
  void deleteStream() {
    @SuppressWarnings("unchecked")
    Stream<Entity> entityStream = mock(Stream.class);
    transactionalRepo.delete(entityStream);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).delete(entityStream);
  }

  @Test
  void deleteById() {
    Object id = mock(Object.class);
    transactionalRepo.deleteById(id);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).deleteById(id);
  }

  @Test
  void deleteAll() {
    transactionalRepo.deleteAll();
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).deleteAll();
  }

  @Test
  void deleteAllStream() {
    @SuppressWarnings("unchecked")
    Stream<Object> entityIds = mock(Stream.class);
    transactionalRepo.deleteAll(entityIds);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).deleteAll(entityIds);
  }

  @Test
  void add() {
    Entity entity = mock(Entity.class);
    transactionalRepo.add(entity);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).add(entity);
  }

  @Test
  void addStream() {
    @SuppressWarnings("unchecked")
    Stream<Entity> entityStream = mock(Stream.class);
    transactionalRepo.add(entityStream);
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).add(entityStream);
  }

  @Test
  void iterator() {
    transactionalRepo.iterator();
    verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    verify(delegateRepository).iterator();
  }
}
