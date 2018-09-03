package org.molgenis.data.decorator;

import static com.google.common.collect.Iterators.partition;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator that checks if actions on this repository are allowed using a PermissionChecker.
 *
 * @param <E> the class of the entities in the repository
 */
public class PermissionCheckingDecorator<E extends Entity> extends AbstractRepositoryDecorator<E> {
  private static final int BATCH_SIZE = 1000;

  private final PermissionChecker<E> permissionChecker;

  public PermissionCheckingDecorator(
      Repository<E> delegateRepository, PermissionChecker<E> permissionChecker) {
    super(delegateRepository);
    this.permissionChecker = requireNonNull(permissionChecker);
  }

  @Override
  public @Nonnull Iterator<E> iterator() {
    return Streams.stream(delegate().iterator())
        .filter(permissionChecker::isReadAllowed)
        .iterator();
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize) {
    delegate()
        .forEachBatched(
            fetch,
            entities ->
                consumer.accept(
                    entities.stream().filter(permissionChecker::isReadAllowed).collect(toList())),
            batchSize);
  }

  @Override
  public long count() {
    return count(new QueryImpl<>());
  }

  @Override
  public long count(Query<E> q) {
    Query<E> qWithoutLimitOffset = new QueryImpl<>(q);
    qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
    Stream<E> permittedEntityStream =
        delegate().findAll(qWithoutLimitOffset).filter(permissionChecker::isCountAllowed);
    permittedEntityStream = skipAndLimitStream(permittedEntityStream, q);
    return permittedEntityStream.count();
  }

  @Override
  public Stream<E> findAll(Query<E> q) {
    Query<E> qWithoutLimitOffset = new QueryImpl<>(q);
    qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
    Stream<E> permittedEntityStream =
        delegate().findAll(qWithoutLimitOffset).filter(permissionChecker::isReadAllowed);
    permittedEntityStream = skipAndLimitStream(permittedEntityStream, q);
    return permittedEntityStream;
  }

  private Stream<E> skipAndLimitStream(Stream<E> entityStream, Query<E> q) {
    if (q.getOffset() > 0) {
      entityStream = entityStream.skip(q.getOffset());
    }
    if (q.getPageSize() > 0) {
      entityStream = entityStream.limit(q.getPageSize());
    }
    return entityStream;
  }

  @Override
  public E findOne(Query<E> q) {
    return findAll(q).findFirst().orElse(null);
  }

  @Override
  public E findOneById(Object id) {
    E entity = delegate().findOneById(id);
    return entity != null && permissionChecker.isReadAllowed(entity) ? entity : null;
  }

  @Override
  public E findOneById(Object id, Fetch fetch) {
    E entity = delegate().findOneById(id, fetch);
    return entity != null && permissionChecker.isReadAllowed(entity) ? entity : null;
  }

  @Override
  public Stream<E> findAll(Stream<Object> ids) {
    return delegate().findAll(ids).filter(permissionChecker::isReadAllowed);
  }

  @Override
  public Stream<E> findAll(Stream<Object> ids, Fetch fetch) {
    return delegate().findAll(ids, fetch).filter(permissionChecker::isReadAllowed);
  }

  @Override
  public void update(E entity) {
    if (permissionChecker.isUpdateAllowed(entity)) {
      delegate().update(entity);
    }
  }

  @Override
  public void update(Stream<E> entities) {
    delegate().update(entities.filter(permissionChecker::isUpdateAllowed));
  }

  @Override
  public void delete(E entity) {
    if (permissionChecker.isDeleteAllowed(entity)) {
      delegate().delete(entity);
    }
  }

  @Override
  public void delete(Stream<E> entities) {
    deleteStream(entities);
  }

  @Override
  public void deleteById(Object id) {
    E entity = delegate().findOneById(id);
    if (entity == null || permissionChecker.isDeleteAllowed(entity)) {
      delegate().deleteById(id);
    }
  }

  @Transactional
  @Override
  public void deleteAll(Stream<Object> ids) {
    // finding during deletion is tricky business: process in batch
    partition(ids.iterator(), BATCH_SIZE)
        .forEachRemaining(
            batchIds -> {
              List<E> entities = delegate().findAll(batchIds.stream()).collect(toList());
              delegate()
                  .deleteAll(
                      entities
                          .stream()
                          .filter(permissionChecker::isDeleteAllowed)
                          .map(Entity::getIdValue));
            });
  }

  @Transactional
  @Override
  public void deleteAll() {
    // finding during deletion is tricky business: can't use iterator or findAll here
    delegate().forEachBatched(entities -> deleteStream(entities.stream()), BATCH_SIZE);
  }

  @Override
  public void add(E entity) {
    if (permissionChecker.isAddAllowed(entity)) {
      delegate().add(entity);
    }
  }

  @Override
  public Integer add(Stream<E> entities) {
    return delegate().add(entities.filter(permissionChecker::isAddAllowed));
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    if (!currentUserIsSuOrSystem()) {
      throw new UnsupportedOperationException();
    }
    return delegate().aggregate(aggregateQuery);
  }

  private void deleteStream(Stream<E> entityStream) {
    delegate().delete(entityStream.filter(permissionChecker::isDeleteAllowed));
  }
}
