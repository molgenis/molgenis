package org.molgenis.data.security.audit;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditingRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {

  private static final String ENTITY_CREATED = "ENTITY_CREATED";
  private static final String ENTITY_UPDATED = "ENTITY_UPDATED";
  private static final String ENTITY_DELETED = "ENTITY_DELETED";

  private final AuditEventPublisher auditEventPublisher;

  public AuditingRepositoryDecorator(
      Repository<Entity> delegateRepository, AuditEventPublisher auditEventPublisher) {
    super(delegateRepository);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public void update(Entity entity) {
    if (isUser()) {
      Map<String, Object> data =
          Map.of("entity_type", delegate().getEntityType().getId(), "entity", entity.getIdValue());
      auditEventPublisher.publish(getUsername(), ENTITY_UPDATED, data);
    }
    delegate().update(entity);
  }

  @Override
  public void delete(Entity entity) {
    if (isUser()) {
      Map<String, Object> data =
          Map.of("entity_type", delegate().getEntityType().getId(), "entity", entity.getIdValue());
      auditEventPublisher.publish(getUsername(), ENTITY_DELETED, data);
    }
    delegate().delete(entity);
  }

  @Override
  public void deleteById(Object id) {
    if (isUser()) {
      Map<String, Object> data = Map.of("entity_type", delegate().getEntityType().getId(), "entity",
          id);
      auditEventPublisher.publish(getUsername(), ENTITY_DELETED, data);
    }
    delegate().deleteById(id);
  }

  @Override
  public void add(Entity entity) {
    if (isUser()) {
      Map<String, Object> data =
          Map.of("entity_type", delegate().getEntityType().getId(), "entity", entity.getIdValue());
      auditEventPublisher.publish(getUsername(), ENTITY_CREATED, data);
    }
    delegate().add(entity);
  }

  private static String getUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private static boolean isUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal instanceof UserDetails;
  }
}
