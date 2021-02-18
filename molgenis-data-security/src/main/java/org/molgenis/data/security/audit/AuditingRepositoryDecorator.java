package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditingRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {

  private static final String ENTITY_READ = "ENTITY_READ";
  private static final String ENTITY_CREATED = "ENTITY_CREATED";
  private static final String ENTITY_UPDATED = "ENTITY_UPDATED";
  private static final String ENTITY_DELETED = "ENTITY_DELETED";
  private static final String ENTITIES_READ = "ENTITIES_READ";
  private static final String ENTITIES_COUNTED = "ENTITIES_COUNTED";
  private static final String ENTITIES_AGGREGATED = "ENTITIES_AGGREGATED";
  private static final String ALL_ENTITIES_DELETED = "ALL_ENTITIES_DELETED";

  private final AuditEventPublisher auditEventPublisher;

  public AuditingRepositoryDecorator(
      Repository<Entity> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public @Nonnull Iterator<Entity> iterator() {
    if (isUser()) {
      return new AuditingIterator(delegate().iterator());
    } else {
      return delegate().iterator();
    }
  }

  @Override
  public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize) {
    if (isUser()) {
      delegate()
          .forEachBatched(
              fetch,
              entities -> {
                consumer.accept(entities);
                var ids = entities.stream().map(Entity::getIdValue).collect(toList());
                audit(ENTITIES_READ, "entities", ids);
              },
              batchSize);
    } else {
      delegate().forEachBatched(fetch, consumer, batchSize);
    }
  }

  @Override
  public long count() {
    long count = delegate().count();

    if (isUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public long count(Query<Entity> q) {
    long count = delegate().count(q);

    if (isUser() && isNonSystemEntityType()) {
      audit(ENTITIES_COUNTED);
    }
    return count;
  }

  @Override
  public Stream<Entity> findAll(Query<Entity> q) {
    if (isUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(q)
          .filter(
              entity -> {
                audit(ENTITY_READ, "entity", entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(q);
    }
  }

  @Override
  public Entity findOne(Query<Entity> q) {
    Entity entity = delegate().findOne(q);
    if (entity != null && isUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public AggregateResult aggregate(AggregateQuery aggregateQuery) {
    var aggregate = delegate().aggregate(aggregateQuery);
    if (isUser()) {
      audit(ENTITIES_AGGREGATED);
    }
    return aggregate;
  }

  @Override
  public Entity findOneById(Object id) {
    Entity entity = delegate().findOneById(id);
    if (entity != null && isUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    Entity entity = delegate().findOneById(id, fetch);
    if (entity != null && isUser() && isNonSystemEntityType()) {
      audit(ENTITY_READ, "entity", entity.getIdValue());
    }
    return entity;
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    if (isUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(ids)
          .filter(
              entity -> {
                audit(ENTITY_READ, "entity", entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(ids);
    }
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    if (isUser() && isNonSystemEntityType()) {
      return delegate()
          .findAll(ids, fetch)
          .filter(
              entity -> {
                audit(ENTITY_READ, "entity", entity.getIdValue());
                return true;
              });
    } else {
      return delegate().findAll(ids, fetch);
    }
  }

  @Override
  public void deleteAll() {
    delegate().deleteAll();

    if (isUser()) {
      audit(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void add(Entity entity) {
    delegate().add(entity);

    if (isUser()) {
      audit(ENTITY_CREATED, "entity", entity.getIdValue());
    }
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    if (isUser()) {
      var entityStream =
          entities.filter(
              entity -> {
                audit(ENTITY_CREATED, "entity", entity.getIdValue());
                return true;
              });
      return delegate().add(entityStream);
    } else {
      return delegate().add(entities);
    }
  }

  @Override
  public void update(Entity entity) {
    delegate().update(entity);

    if (isUser()) {
      audit(ENTITY_UPDATED, "entity", entity.getIdValue());
    }
  }

  @Override
  public void update(Stream<Entity> entities) {
    if (isUser()) {
      var entityStream =
          entities.filter(
              entity -> {
                audit(ENTITY_UPDATED, "entity", entity.getIdValue());
                return true;
              });

      delegate().update(entityStream);
    } else {
      delegate().update(entities);
    }
  }

  @Override
  public void delete(Entity entity) {
    delegate().delete(entity);

    if (isUser()) {
      audit(ENTITY_DELETED, "entity", entity.getIdValue());
    }
  }

  @Override
  public void delete(Stream<Entity> entities) {
    if (isUser()) {
      var entityStream =
          entities.filter(
              entity -> {
                audit(ENTITY_DELETED, "entity", entity.getIdValue());
                return true;
              });

      delegate().delete(entityStream);
    } else {
      delegate().update(entities);
    }
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    delegate().deleteAll(ids);

    if (isUser()) {
      audit(ALL_ENTITIES_DELETED);
    }
  }

  @Override
  public void deleteById(Object id) {
    delegate().deleteById(id);

    if (isUser()) {
      audit(ENTITY_DELETED, "entity", id);
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
    data.put("entity_type_id", delegate().getEntityType().getId());
    return data;
  }

  private static String getUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private boolean isNonSystemEntityType() {
    return !EntityTypeUtils.isSystemEntity(delegate().getEntityType());
  }

  private static boolean isUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal instanceof UserDetails;
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
      audit(ENTITY_READ, "entity", entity.getIdValue());
      return entity;
    }
  }
}
