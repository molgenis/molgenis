package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsRunningAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsUser;
import static org.molgenis.security.core.utils.SecurityUtils.getActualUsername;

import com.google.common.collect.ForwardingIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  public @Nonnull Iterator<Entity> iterator() {
    if (currentUserIsUser() && isNonSystemEntityType()) {
      return new AuditingIterator(delegate().iterator());
    } else {
      return delegate().iterator();
    }
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize) {
    if (currentUserIsUser() && isNonSystemEntityType()) {
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

    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public long count(Query<Entity> q) {
    long count = delegate().count(q);

    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public Stream<Entity> findAll(Query<Entity> q) {
    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_READ);
    }
    return delegate().findAll(q);
  }

  @Override
  public Entity findOne(Query<Entity> q) {
    Entity entity = delegate().findOne(q);
    if (entity != null && currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    var aggregate = delegate().aggregate(aggregateQuery);
    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_AGGREGATED);
    }
    return aggregate;
  }

  @Override
  public Entity findOneById(Object id) {
    Entity entity = delegate().findOneById(id);
    if (entity != null && currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    Entity entity = delegate().findOneById(id, fetch);
    if (entity != null && currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, ENTITY_ID, entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_READ);
    }
    return delegate().findAll(ids);
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    if (currentUserIsUser() && isNonSystemEntityType()) {
      audit(ENTITIES_READ);
    }
    return delegate().findAll(ids, fetch);
  }

  @Override
  public void deleteAll() {
    delegate().deleteAll();

    if (currentUserIsUser()) {
      audit(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void add(Entity entity) {
    delegate().add(entity);

    if (currentUserIsUser()) {
      audit(ENTITY_CREATED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    if (currentUserIsUser()) {
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

    if (currentUserIsUser()) {
      audit(ENTITY_UPDATED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public void update(Stream<Entity> entities) {
    if (currentUserIsUser()) {
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

    if (currentUserIsUser()) {
      audit(ENTITY_DELETED, ENTITY_ID, entity.getIdValue());
    }
  }

  @Override
  public void delete(Stream<Entity> entities) {
    if (currentUserIsUser()) {
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
    if (currentUserIsUser()) {
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

    if (currentUserIsUser()) {
      audit(ENTITY_DELETED, ENTITY_ID, id);
    }
  }

  private void audit(String type) {
    var data = createDataMap();
    auditEventPublisher.publish(getActualUsername(), type, data);
  }

  private void audit(String type, String key, Object value) {
    var data = createDataMap();
    data.put(key, value);
    auditEventPublisher.publish(getActualUsername(), type, data);
  }

  private Map<String, Object> createDataMap() {
    var data = new HashMap<String, Object>();

    data.put(ENTITY_TYPE_ID, delegate().getEntityType().getId());
    if (currentUserIsRunningAsSystem()) {
      data.put(RUN_AS, "SYSTEM");
    }

    return data;
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
