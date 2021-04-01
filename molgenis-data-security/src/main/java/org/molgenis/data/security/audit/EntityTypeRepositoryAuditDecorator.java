package org.molgenis.data.security.audit;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;
import static org.molgenis.security.core.utils.SecurityUtils.getActualUsername;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.audit.AuditEvent;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;

/** Publishes {@link AuditEvent}s when an {@link EntityType}'s auditing is enabled or disabled. */
public class EntityTypeRepositoryAuditDecorator extends AbstractRepositoryDecorator<EntityType> {

  static final String ENTITY_TYPE_AUDIT_ENABLED = "ENTITY_TYPE_AUDIT_ENABLED";
  static final String ENTITY_TYPE_AUDIT_DISABLED = "ENTITY_TYPE_AUDIT_DISABLED";

  private final AuditEventPublisher auditEventPublisher;

  public EntityTypeRepositoryAuditDecorator(
      Repository<EntityType> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public void update(EntityType entityType) {
    auditAuditTagChanges(entityType);
    delegate().update(entityType);
  }

  @Override
  public void update(Stream<EntityType> entities) {
    delegate().update(entities.filter(this::auditAuditTagChanges));
  }

  @Override
  public void add(EntityType entityType) {
    auditAddAuditTag(entityType);
    delegate().add(entityType);
  }

  @Override
  public Integer add(Stream<EntityType> entities) {
    return delegate().add(entities.filter(this::auditAddAuditTag));
  }

  @Override
  public void delete(EntityType entity) {
    auditDeleteAuditTag(entity);
    delegate().delete(entity);
  }

  @Override
  public void deleteById(Object id) {
    var entityType = delegate().findOneById(id);
    if (entityType == null) {
      throw new UnknownEntityTypeException(id.toString());
    }
    auditDeleteAuditTag(entityType);
    delegate().delete(entityType);
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Stream<EntityType> entities) {
    var auditedStream = entities.filter(this::auditDeleteAuditTag);
    delegate().delete(auditedStream);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    var auditedStream = delegate().findAll(ids).filter(this::auditDeleteAuditTag);
    delegate().delete(auditedStream);
  }

  private boolean auditAuditTagChanges(EntityType entityType) {
    var oldEntityType = delegate().findOneById(entityType.getId());

    var wasAudit = oldEntityType != null && hasAuditTag(oldEntityType);
    var isAudit = hasAuditTag(entityType);

    if (wasAudit && !isAudit) {
      auditEventPublisher.publish(
          getActualUsername(), ENTITY_TYPE_AUDIT_DISABLED, createDataMap(entityType));
    } else if (!wasAudit && isAudit) {
      auditEventPublisher.publish(
          getActualUsername(), ENTITY_TYPE_AUDIT_ENABLED, createDataMap(entityType));
    }

    return true;
  }

  private boolean auditAddAuditTag(EntityType entityType) {
    var isAudit = hasAuditTag(entityType);
    if (isAudit) {
      auditEventPublisher.publish(
          getActualUsername(), ENTITY_TYPE_AUDIT_ENABLED, createDataMap(entityType));
    }
    return true;
  }

  private boolean auditDeleteAuditTag(EntityType entityType) {
    var wasAudit = hasAuditTag(entityType);
    if (wasAudit) {
      auditEventPublisher.publish(
          getActualUsername(), ENTITY_TYPE_AUDIT_DISABLED, createDataMap(entityType));
    }
    return true;
  }

  private static boolean hasAuditTag(EntityType oldEntityType) {
    return stream(oldEntityType.getTags())
        .anyMatch(EntityTypeRepositoryAuditDecorator::isAuditUsage);
  }

  private static boolean isAuditUsage(Tag tag) {
    return AUDIT_USAGE.toString().equals(tag.getObjectIri())
        && isAudited.getIRI().equals(tag.getRelationIri());
  }

  private static Map<String, Object> createDataMap(EntityType entityType) {
    var data = new HashMap<String, Object>();
    data.put("entityTypeId", entityType.getId());
    return data;
  }
}
