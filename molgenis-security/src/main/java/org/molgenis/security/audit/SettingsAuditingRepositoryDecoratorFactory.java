package org.molgenis.security.audit;

import static java.util.Objects.requireNonNull;

import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

/** All {@link DefaultSettingsEntityType}s should always be audited. */
@Component
public class SettingsAuditingRepositoryDecoratorFactory {

  private final AuditEventPublisher auditEventPublisher;

  public SettingsAuditingRepositoryDecoratorFactory(AuditEventPublisher auditEventPublisher) {
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  public Repository<Entity> decorate(Repository<Entity> repository) {
    if (repository.getEntityType() instanceof DefaultSettingsEntityType) {
      return new AuditSettingsRepositoryDecorator(repository, auditEventPublisher);
    } else {
      return repository;
    }
  }
}
