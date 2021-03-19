package org.molgenis.security.audit;

import static java.util.Objects.requireNonNull;

import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.security.audit.AuditSettingsImpl.Meta;
import org.springframework.stereotype.Component;

@Component
public class AuditSettingsRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Entity, AuditSettingsImpl.Meta> {

  private final AuditEventPublisher auditEventPublisher;

  public AuditSettingsRepositoryDecoratorFactory(
      Meta auditSettingsMeta, AuditEventPublisher auditEventPublisher) {
    super(auditSettingsMeta);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public Repository<Entity> createDecoratedRepository(Repository<Entity> repository) {
    return new AuditSettingsRepositoryDecorator(repository, auditEventPublisher);
  }
}
