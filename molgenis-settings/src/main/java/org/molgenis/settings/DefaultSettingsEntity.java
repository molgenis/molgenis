package org.molgenis.settings;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;

import java.time.Instant;
import java.time.LocalDate;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for application and plugin settings entities. Settings are read/written from/to data
 * source. TODO: Bring this class up to date with 2.0, see http://www.molgenis.org/ticket/4787
 */
public abstract class DefaultSettingsEntity implements Entity {
  private final String entityId;
  private final String entityTypeId;
  private DataService dataService;

  public DefaultSettingsEntity(String entityId) {
    this.entityId = requireNonNull(entityId);
    this.entityTypeId = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + entityId;
  }

  @Autowired
  public void setDataService(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  public EntityType getEntityType() {
    return runAsSystem(() -> dataService.getEntityType(entityTypeId));
  }

  @Override
  public Iterable<String> getAttributeNames() {
    return getEntity().getAttributeNames();
  }

  @Override
  public Object getIdValue() {
    return getEntity().getIdValue();
  }

  @Override
  public void setIdValue(Object id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getLabelValue() {
    return getEntity().getLabelValue();
  }

  @Override
  public Object get(String attributeName) {
    return getEntity().get(attributeName);
  }

  @Override
  public String getString(String attributeName) {
    return getEntity().getString(attributeName);
  }

  @Override
  public Integer getInt(String attributeName) {
    return getEntity().getInt(attributeName);
  }

  @Override
  public Long getLong(String attributeName) {
    return getEntity().getLong(attributeName);
  }

  @Override
  public Boolean getBoolean(String attributeName) {
    return getEntity().getBoolean(attributeName);
  }

  @Override
  public Double getDouble(String attributeName) {
    return getEntity().getDouble(attributeName);
  }

  @Override
  public Instant getInstant(String attributeName) {
    return getEntity().getInstant(attributeName);
  }

  @Override
  public LocalDate getLocalDate(String attributeName) {
    return getEntity().getLocalDate(attributeName);
  }

  @Override
  public Entity getEntity(String attributeName) {
    return getEntity().getEntity(attributeName);
  }

  @Override
  public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
    return getEntity().getEntity(attributeName, clazz);
  }

  @Override
  public Iterable<Entity> getEntities(String attributeName) {
    return getEntity().getEntities(attributeName);
  }

  @Override
  public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
    return getEntity().getEntities(attributeName, clazz);
  }

  @Override
  public void set(String attributeName, Object value) {
    Entity entity = getEntity();
    entity.set(attributeName, value);
    updateEntity(entity);
  }

  @Override
  public void set(Entity values) {
    Entity entity = getEntity();
    entity.set(values);
    updateEntity(entity);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Entity)) return false;
    return EntityUtils.equals(this, (Entity) o);
  }

  @Override
  public int hashCode() {
    return EntityUtils.hashCode(this);
  }

  @Override
  public String toString() {
    return getEntity().toString();
  }

  private Entity getEntity() {
    return runAsSystem(() -> dataService.findOneById(entityTypeId, entityId));
  }

  private void updateEntity(Entity entity) {
    runAsSystem(() -> dataService.update(entityTypeId, entity));
  }
}
