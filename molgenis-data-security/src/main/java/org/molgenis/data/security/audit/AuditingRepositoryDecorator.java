package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditingRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {

  private static final String ENTITY_READ = "ENTITY_READ";
  private static final String ENTITY_CREATED = "ENTITY_CREATED";
  private static final String ENTITY_UPDATED = "ENTITY_UPDATED";
  private static final String ENTITY_DELETED = "ENTITY_DELETED";
  private static final String ENTITIES_READ = "ENTITIES_READ";
  private static final String ENTITIES_CREATED = "ENTITIES_CREATED";
  private static final String ENTITIES_UPDATED = "ENTITIES_UPDATED";
  private static final String ENTITIES_DELETED = "ENTITIES_DELETED";
  private static final String ENTITIES_COUNTED = "ENTITIES_COUNTED";
  private static final String ALL_ENTITIES_DELETED = "ALL_ENTITIES_DELETED";


  private final AuditEventPublisher auditEventPublisher;

  public AuditingRepositoryDecorator(
      Repository<Entity> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public @Nonnull
  Iterator<Entity> iterator() {
    return super.iterator();
    //TODO
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize) {
    //TODO
    super.forEachBatched(fetch, consumer, batchSize);
  }

  @Override
  public long count() {
    long count = delegate().count();

    if (isUser()) {
      publishAuditEvent(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public Query<Entity> query() {
    // TODO
    return super.query();
  }

  @Override
  public long count(Query<Entity> q) {
    long count = delegate().count(q);

    if (isUser()) {
      publishAuditEvent(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public Stream<Entity> findAll(Query<Entity> q) {
    //TODO
    return delegate().findAll(q);
  }

  @Override
  public Entity findOne(Query<Entity> q) {
    Entity entity = delegate().findOne(q);
    if (entity != null && isUser())
    {
      publishAuditEvent(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    // TODO
    return super.aggregate(aggregateQuery);
  }

  @Override
  public Entity findOneById(Object id) {
    Entity entity = delegate().findOneById(id);
    if (entity != null && isUser())
    {
      publishAuditEvent(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    Entity entity = delegate().findOneById(id, fetch);
    if (entity != null && isUser())
    {
      publishAuditEvent(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    // TODO
    return super.findAll(ids);
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    // TODO
    return super.findAll(ids, fetch);
  }

  @Override
  public void deleteAll() {
    delegate().deleteAll();

    if (isUser()) {
      publishAuditEvent(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void add(Entity entity) {
    delegate().add(entity);

    if (isUser()) {
      publishAuditEvent(ENTITY_CREATED, "entity", entity.getIdValue());
    }
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    Integer count = delegate().add(entities);

    if (isUser()) {
      publishAuditEvent(ENTITIES_CREATED, "count", count);
    }

    return count;
  }

  @Override
  public void update(Entity entity) {
    delegate().update(entity);

    if (isUser()) {
      publishAuditEvent(ENTITY_UPDATED, "entity", entity.getIdValue());
    }
  }

  @Override
  public void update(Stream<Entity> entities) {
    if (isUser()) {
      var count = new AtomicInteger();
      var entityStream = addCounterToStream(entities, count);

      delegate().update(entityStream);

      publishAuditEvent(ENTITIES_UPDATED, "count", count);
    } else {
      delegate().update(entities);
    }
  }

  @Override
  public void delete(Entity entity) {
    delegate().delete(entity);

    if (isUser()) {
      publishAuditEvent(ENTITY_DELETED, "entity", entity.getIdValue());
    }
  }

  @Override
  public void delete(Stream<Entity> entities) {
    if (isUser()) {
      var count = new AtomicInteger();
      var entityStream = addCounterToStream(entities, count);

      delegate().delete(entityStream);

      publishAuditEvent(ENTITIES_DELETED, "count", count);
    } else {
      delegate().update(entities);
    }
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    delegate().deleteAll(ids);

    if (isUser()) {
      publishAuditEvent(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void deleteById(Object id) {
    delegate().deleteById(id);

    if (isUser()) {
      publishAuditEvent(ENTITY_DELETED, "entity", id);
    }
  }

  private void publishAuditEvent(String type){
    var data = createDataMap();
    auditEventPublisher.publish(getUsername(), type, data);
  }

  private void publishAuditEvent(String type, String k1, Object v1) {
    var data = createDataMap();
    data.put(k1, v1);
    auditEventPublisher.publish(getUsername(), type, data);
  }

  private Map<String, Object> createDataMap() {
    var data = new HashMap<String, Object>();
    data.put("entity_type_id", delegate().getEntityType().getId());
    return data;
  }

  private static String getUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private static boolean isUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal instanceof UserDetails;
  }

  private static Stream<Entity> addCounterToStream(Stream<Entity> entities, AtomicInteger counter) {
    return entities.filter(entity -> {
      counter.incrementAndGet();
      return true;
    });
  }

}
