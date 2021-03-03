package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.audit.AuditTransactionListener.TRANSACTION_ID;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunAsSystem;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunByUser;
import static org.molgenis.data.transaction.TransactionConstants.TRANSACTION_ID_RESOURCE_NAME;

import com.google.common.collect.ForwardingIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes audit events for CRUD actions. In the case of system entity types, does not publish
 * events for read actions. If the current user is SYSTEM (excluding elevated users running as
 * system), does not publish anything.
 */
public class AuditingRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {

  static final String ENTITY_READ = "ENTITY_READ";
  static final String ENTITY_CREATED = "ENTITY_CREATED";
  static final String ENTITY_UPDATED = "ENTITY_UPDATED";
  static final String ENTITY_DELETED = "ENTITY_DELETED";
  static final String ENTITIES_READ = "ENTITIES_READ";
  static final String ENTITIES_COUNTED = "ENTITIES_COUNTED";
  static final String ENTITIES_AGGREGATED = "ENTITIES_AGGREGATED";
  static final String ALL_ENTITIES_DELETED = "ALL_ENTITIES_DELETED";
  private static final String ENTITY_TYPE_ID = "entityTypeId";
  private static final String ENTITY_ID = "entityId";
  private static final String ENTITY_IDS = "entityIds";
  private static final String RUN_AS = "runAs";

  private final AuditEventPublisher auditEventPublisher;

  public AuditingRepositoryDecorator(
      Repository<Entity> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public @Nonnull
  Iterator<Entity> iterator() {
    if (isRunByUser() && isNonSystemEntityType()) {
      return new AuditingIterator(delegate().iterator());
    } else {
      return delegate().iterator();
    }
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize) {
    if (isRunByUser() && isNonSystemEntityType()) {
      delegate()
          .forEachBatched(
              fetch,
              entities -> {
                consumer.accept(entities);
                var ids = entities.stream().map(Entity::getIdValue).collect(toList());
                audit(ENTITIES_READ, ENTITY_IDS, ids);
              },
              batchSize);
    } else {
      delegate().forEachBatched(fetch, consumer, batchSize);
    }
  }

  @Override
  public long count() {
    long count = delegate().count();

    if (isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public long count(Query<Entity> q) {
    long count = delegate().count(q);

    if (isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public Stream<Entity> findAll(Query<Entity> q) {
    if (isRunByUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(q)
          .filter(
              entity -> {
                audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(q);
    }
  }

  @Override
  public Entity findOne(Query<Entity> q) {
    Entity entity = delegate().findOne(q);
    if (entity != null && isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    var aggregate = delegate().aggregate(aggregateQuery);
    if (isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITIES_AGGREGATED);
    }
    return aggregate;
  }

  @Override
  public Entity findOneById(Object id) {
    Entity entity = delegate().findOneById(id);
    if (entity != null && isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    Entity entity = delegate().findOneById(id, fetch);
    if (entity != null && isRunByUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    if (isRunByUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(ids)
          .filter(
              entity -> {
                audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(ids);
    }
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    if (isRunByUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(ids, fetch)
          .filter(
              entity -> {
                audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(ids, fetch);
    }
  }

  @Override
  public void deleteAll() {
    delegate().deleteAll();

    if (isRunByUser()) {
      audit(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void add(Entity entity) {
    delegate().add(entity);

    if (isRunByUser()) {
      audit(ENTITY_CREATED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    if (isRunByUser()) {
      entities =
          entities.filter(
              entity -> {
                audit(ENTITY_CREATED, ENTITY_ID, entity.getIdValue());
                return true;
              });
    }

    return delegate().add(entities);
  }

  @Override
  public void update(Entity entity) {
    delegate().update(entity);

    if (isRunByUser()) {
      audit(ENTITY_UPDATED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public void update(Stream<Entity> entities) {
    if (isRunByUser()) {
      entities =
          entities.filter(
              entity -> {
                audit(ENTITY_UPDATED, ENTITY_ID, entity.getIdValue());
                return true;
              });
    }

    delegate().update(entities);
  }

  @Override
  public void delete(Entity entity) {
    delegate().delete(entity);

    if (isRunByUser()) {
      audit(ENTITY_DELETED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public void delete(Stream<Entity> entities) {
    if (isRunByUser()) {
      entities =
          entities.filter(
              entity -> {
                audit(ENTITY_DELETED, ENTITY_ID, entity.getIdValue());
                return true;
              });
    }

    delegate().delete(entities);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    if (isRunByUser()) {
      ids =
          ids.filter(
              id -> {
                audit(ENTITY_DELETED, ENTITY_ID, id);
                return true;
              });
    }

    delegate().deleteAll(ids);
  }

  @Override
  public void deleteById(Object id) {
    delegate().deleteById(id);

    if (isRunByUser()) {
      audit(ENTITY_DELETED, ENTITY_ID, id);
    }
  }

  private void audit(String type) {
    var data = createDataMap();
    auditEventPublisher.publish(getUsername(), type, data);
  }

  private void audit(String type, String k1, Object v1) {
    var data = createDataMap();
    data.put(k1, v1);
    auditEventPublisher.publish(getUsername(), type, data);
  }

  private Map<String, Object> createDataMap() {
    var data = new HashMap<String, Object>();

    data.put(ENTITY_TYPE_ID, delegate().getEntityType().getId());
    if (isRunAsSystem()) {
      data.put(RUN_AS, "SYSTEM");
    }
    getTransactionId().ifPresent(transactionId -> data.put(TRANSACTION_ID, transactionId));

    return data;
  }

  private static Optional<String> getTransactionId() {
    return Optional.ofNullable(
        (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME));
  }

  private boolean isNonSystemEntityType() {
    return !EntityTypeUtils.isSystemEntity(delegate().getEntityType());
  }

  class AuditingIterator extends ForwardingIterator<Entity> {

    private final Iterator<Entity> delegate;

    AuditingIterator(Iterator<Entity> delegate) {
      this.delegate = delegate;
    }

    @Override
    protected Iterator<Entity> delegate() {
      return delegate;
    }

    @Override
    public Entity next() {
      var entity = super.next();
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
      return entity;
    }
  }
}
