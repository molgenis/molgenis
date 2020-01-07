package org.molgenis.data.transaction;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Repository decorator that wraps CRUD operations in a (read-only) transaction. Classes that extend
 * from {@link AbstractRepositoryDecorator} might not be managed by Spring, so {@link
 * TransactionTemplate} is used instead of the {@link Transactional} annotation.
 *
 * @param <E> entity type
 */
public class TransactionalRepositoryDecorator<E extends Entity>
    extends AbstractRepositoryDecorator<E> {
  private final PlatformTransactionManager transactionManager;

  public TransactionalRepositoryDecorator(
      Repository<E> delegateRepository, PlatformTransactionManager transactionManager) {
    super(delegateRepository);
    this.transactionManager = requireNonNull(transactionManager);
  }

  @Override
  public void forEachBatched(Consumer<List<E>> consumer, int batchSize) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().forEachBatched(consumer, batchSize);
    } else {
      createReadonlyTransactionTemplate()
          .execute(
              status -> {
                delegate().forEachBatched(consumer, batchSize);
                return null;
              });
    }
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().forEachBatched(fetch, consumer, batchSize);
    } else {
      createReadonlyTransactionTemplate()
          .execute(
              status -> {
                delegate().forEachBatched(fetch, consumer, batchSize);
                return null;
              });
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public long count() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().count();
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().count());
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public long count(Query<E> q) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().count(q);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().count(q));
    }
  }

  @Override
  public Stream<E> findAll(Query<E> q) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findAll(q);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().findAll(q));
    }
  }

  @Override
  public E findOne(Query<E> q) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findOne(q);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().findOne(q));
    }
  }

  @Override
  public E findOneById(Object id) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findOneById(id);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().findOneById(id));
    }
  }

  @Override
  public E findOneById(Object id, Fetch fetch) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findOneById(id, fetch);
    } else {
      return createReadonlyTransactionTemplate()
          .execute(status -> delegate().findOneById(id, fetch));
    }
  }

  @Override
  public Stream<E> findAll(Stream<Object> ids) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findAll(ids);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().findAll(ids));
    }
  }

  @Override
  public Stream<E> findAll(Stream<Object> ids, Fetch fetch) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().findAll(ids, fetch);
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().findAll(ids, fetch));
    }
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().aggregate(aggregateQuery);
    } else {
      return createReadonlyTransactionTemplate()
          .execute(status -> delegate().aggregate(aggregateQuery));
    }
  }

  @Override
  public void update(E entity) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().update(entity);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().update(entity);
                return null;
              });
    }
  }

  @Override
  public void update(Stream<E> entities) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().update(entities);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().update(entities);
                return null;
              });
    }
  }

  @Override
  public void delete(E entity) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().delete(entity);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().delete(entity);
                return null;
              });
    }
  }

  @Override
  public void delete(Stream<E> entities) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().delete(entities);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().delete(entities);
                return null;
              });
    }
  }

  @Override
  public void deleteById(Object id) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().deleteById(id);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().deleteById(id);
                return null;
              });
    }
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().deleteAll(ids);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().deleteAll(ids);
                return null;
              });
    }
  }

  @Override
  public void deleteAll() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().deleteAll();
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().deleteAll();
                return null;
              });
    }
  }

  @Override
  public void add(E entity) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      delegate().add(entity);
    } else {
      createWriteTransactionTemplate()
          .execute(
              status -> {
                delegate().add(entity);
                return null;
              });
    }
  }

  @Override
  public Integer add(Stream<E> entities) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().add(entities);
    } else {
      return createWriteTransactionTemplate().execute(status -> delegate().add(entities));
    }
  }

  @SuppressWarnings({"ConstantConditions", "NullableProblems"})
  @Override
  public Iterator<E> iterator() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      return delegate().iterator();
    } else {
      return createReadonlyTransactionTemplate().execute(status -> delegate().iterator());
    }
  }

  private TransactionTemplate createWriteTransactionTemplate() {
    return new TransactionTemplate(transactionManager);
  }

  private TransactionTemplate createReadonlyTransactionTemplate() {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setReadOnly(true);
    return transactionTemplate;
  }
}
