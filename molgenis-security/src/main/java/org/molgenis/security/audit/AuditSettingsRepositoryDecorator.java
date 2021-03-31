package org.molgenis.security.audit;

import static org.molgenis.security.core.utils.SecurityUtils.getActualUsername;

import java.util.HashMap;
import java.util.stream.Stream;
import org.molgenis.audit.AuditEvent;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

/**
 * Publishes {@link AuditEvent}s when changes are made to any {@link
 * org.molgenis.settings.DefaultSettingsEntityType}. Audits every single change for each attribute
 * separately.
 */
public class AuditSettingsRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {

  static final String AUDIT_SETTING_CHANGED = "AUDIT_SETTING_CHANGED";
  private final AuditEventPublisher auditEventPublisher;

  public AuditSettingsRepositoryDecorator(
      Repository<Entity> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = auditEventPublisher;
  }

  @Override
  public void update(Entity entity) {
    auditChanges(entity);
    delegate().update(entity);
  }

  @Override
  public void update(Stream<Entity> entities) {
    delegate().update(entities.filter(this::auditChanges));
  }

  private boolean auditChanges(Entity entity) {
    var oldEntity = delegate().findOneById(entity.getIdValue());

    delegate()
        .getEntityType()
        .getAtomicAttributes()
        .forEach(
            attr -> {
              var oldValue = oldEntity == null ? null : oldEntity.get(attr.getName());
              var newValue = entity.get(attr.getName());

              if ((oldValue == null && newValue != null)
                  || (oldValue != null && !oldValue.equals(newValue))) {
                var data = new HashMap<String, Object>();
                data.put("setting", attr.getName());
                data.put("oldValue", oldValue);
                data.put("newValue", newValue);
                data.put("entityTypeId", delegate().getEntityType().getId());
                auditEventPublisher.publish(getActualUsername(), AUDIT_SETTING_CHANGED, data);
              }
            });

    return true;
  }
}
