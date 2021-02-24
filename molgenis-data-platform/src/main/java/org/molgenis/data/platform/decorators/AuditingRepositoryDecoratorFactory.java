package org.molgenis.data.platform.decorators;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.job.IndexJobExecutionMetadata.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetadata.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.semantic.Vocabulary.AUDITED;
import static org.molgenis.data.util.EntityTypeUtils.isSystemEntity;

import com.google.common.collect.ImmutableSet;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator;
import org.molgenis.security.audit.AuditSettings;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

@Component
public class AuditingRepositoryDecoratorFactory {

  private static final ImmutableSet<String> systemAuditExclusions =
      ImmutableSet.of(INDEX_JOB_EXECUTION, INDEX_ACTION, INDEX_ACTION_GROUP);

  private final AuditEventPublisher auditEventPublisher;
  private final AuditSettings auditSettings;

  AuditingRepositoryDecoratorFactory(AuditEventPublisher auditEventPublisher,
      AuditSettings auditSettings) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
    this.auditSettings = requireNonNull(auditSettings);
  }

  public Repository<Entity> create(Repository<Entity> repository) {
    var entityType = repository.getEntityType();
    if (isAuditedSystemEntityType(entityType)
        || isAuditedDataEntityType(entityType)) {
      return new AuditingRepositoryDecorator(repository, auditEventPublisher);
    } else {
      return repository;
    }
  }

  private boolean isAuditedSystemEntityType(EntityType entityType) {
    return auditSettings.getSystemAuditEnabled()
        && isSystemEntity(entityType)
        && !systemAuditExclusions.contains(entityType.getId());
  }

  private boolean isAuditedDataEntityType(EntityType entityType) {
    switch (auditSettings.getDataAuditSetting()) {
      case NONE:
        return false;
      case SOME:
        return stream(entityType.getTags())
            .anyMatch(tag -> AUDITED.toString().equals(tag.getObjectIri()));
      case ALL:
        return true;
      default:
        throw new UnexpectedEnumException(auditSettings.getDataAuditSetting());
    }
  }
}
