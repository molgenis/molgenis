package org.molgenis.data.platform.decorators;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;
import static org.molgenis.data.util.EntityTypeUtils.isSystemEntity;
import static org.molgenis.security.audit.AuditSettingsImpl.AUDIT_SETTINGS;

import com.google.common.collect.Sets;
import java.util.Set;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.event.BootstrappingEvent;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator;
import org.molgenis.security.audit.AuditSettings;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditingRepositoryDecoratorFactory {

  private static final Set<String> EXCLUDED = Sets.newHashSet(AUDIT_SETTINGS);

  private final AuditEventPublisher auditEventPublisher;
  private final AuditSettings auditSettings;
  private boolean bootstrappingDone = false;

  AuditingRepositoryDecoratorFactory(
      AuditEventPublisher auditEventPublisher, AuditSettings auditSettings) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
    this.auditSettings = requireNonNull(auditSettings);
  }

  public Repository<Entity> create(Repository<Entity> repository) {
    var entityType = repository.getEntityType();

    var decorate = false;
    if (!EXCLUDED.contains(entityType.getId()) && bootstrappingDone) {
      if (isSystemEntity(entityType)) {
        decorate = auditSettings.getSystemAuditEnabled();
      } else {
        decorate = isAuditedDataEntityType(entityType);
      }
    }

    if (decorate) {
      return new AuditingRepositoryDecorator(repository, auditEventPublisher);
    } else {
      return repository;
    }
  }

  public void excludeEntityType(String entityTypeId) {
    EXCLUDED.add(entityTypeId);
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

  @EventListener
  public void onBootstrappingEvent(BootstrappingEvent event) {
    this.bootstrappingDone = event.getStatus() == FINISHED;
  }
}
