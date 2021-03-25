package org.molgenis.data.security.audit;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;

import java.util.HashMap;
import java.util.stream.Stream;
import org.molgenis.audit.AuditEvent;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;

/** Publishes {@link AuditEvent}s when an {@link EntityType}'s auditing is enabled or disabled. */
public class EntityTypeRepositoryAuditDecorator extends AbstractRepositoryDecorator<EntityType> {

  static final String AUDIT_ENABLED = "AUDIT_ENABLED";
  static final String AUDIT_DISABLED = "AUDIT_DISABLED";

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
    auditAuditTag(entityType);
    delegate().add(entityType);
  }

  @Override
  public Integer add(Stream<EntityType> entities) {
    return delegate().add(entities.filter(this::auditAuditTag));
  }

  private boolean auditAuditTagChanges(EntityType entityType) {
    var oldEntityType = delegate().findOneById(entityType.getId());

    var wasAudit =
        oldEntityType != null && stream(oldEntityType.getTags()).anyMatch(this::isAuditUsage);
    var isAudit = stream(entityType.getTags()).anyMatch(this::isAuditUsage);

    if (wasAudit && !isAudit) {
      auditEventPublisher.publish(getUsername(), AUDIT_DISABLED, createDataMap(entityType));
    } else if (!wasAudit && isAudit) {
      auditEventPublisher.publish(getUsername(), AUDIT_ENABLED, createDataMap(entityType));
    }

    return true;
  }

  private boolean auditAuditTag(EntityType entityType) {
    var isAudit = stream(entityType.getTags()).anyMatch(this::isAuditUsage);
    if (isAudit) {
      auditEventPublisher.publish(getUsername(), AUDIT_ENABLED, createDataMap(entityType));
    }
    return true;
  }

  private boolean isAuditUsage(Tag tag) {
    return AUDIT_USAGE.toString().equals(tag.getObjectIri())
        && isAudited.getIRI().equals(tag.getRelationIri());
  }

  private static HashMap<String, Object> createDataMap(EntityType entityType) {
    var data = new HashMap<String, Object>();
    data.put("entityTypeId", entityType.getId());
    return data;
  }
}
