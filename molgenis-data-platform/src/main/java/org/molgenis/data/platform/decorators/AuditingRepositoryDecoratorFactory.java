package org.molgenis.data.platform.decorators;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.job.IndexJobExecutionMetadata.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetadata.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;
import static org.molgenis.data.util.EntityTypeUtils.isSystemEntity;
import static org.molgenis.security.audit.AuditSettingsImpl.AUDIT_SETTINGS;

import com.google.common.collect.ImmutableSet;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator;
import org.molgenis.security.audit.AuditSettings;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

@Component
public class AuditingRepositoryDecoratorFactory {

  private static final ImmutableSet<String> SYSTEM_AUDIT_EXCLUSIONS =
      ImmutableSet.of(INDEX_JOB_EXECUTION, INDEX_ACTION, INDEX_ACTION_GROUP, AUDIT_SETTINGS);

  private final AuditEventPublisher auditEventPublisher;
  private final AuditSettings auditSettings;

  AuditingRepositoryDecoratorFactory(
      AuditEventPublisher auditEventPublisher, AuditSettings auditSettings) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
    this.auditSettings = requireNonNull(auditSettings);
  }

  public Repository<Entity> create(Repository<Entity> repository) {
    var entityType = repository.getEntityType();

    var decorate = false;
    if (isSystemEntity(entityType)) {
      decorate = isAuditedSystemEntityType(entityType);
    } else {
      decorate = isAuditedDataEntityType(entityType);
    }

    if (decorate) {
      return new AuditingRepositoryDecorator(repository, auditEventPublisher);
    } else {
      return repository;
    }
  }

  private boolean isAuditedSystemEntityType(EntityType entityType) {
    return auditSettings.getSystemAuditEnabled()
        && !SYSTEM_AUDIT_EXCLUSIONS.contains(entityType.getId());
  }

  private boolean isAuditedDataEntityType(EntityType entityType) {
    switch (auditSettings.getDataAuditSetting()) {
      case NONE:
        return false;
      case TAGGED:
        return stream(entityType.getTags()).anyMatch(this::isAuditUsage);
      case ALL:
        return true;
      default:
        throw new UnexpectedEnumException(auditSettings.getDataAuditSetting());
    }
  }

  private boolean isAuditUsage(Tag tag) {
    return AUDIT_USAGE.toString().equals(tag.getObjectIri())
        && isAudited.getIRI().equals(tag.getRelationIri());
  }
}
