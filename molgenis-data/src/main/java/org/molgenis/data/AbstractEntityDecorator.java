package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.time.Instant;
import java.time.LocalDate;
import org.molgenis.data.meta.model.EntityType;

public abstract class AbstractEntityDecorator extends ForwardingObject implements Entity {

  private final Entity delegateEntity;

  public AbstractEntityDecorator(Entity entity) {
    this.delegateEntity = requireNonNull(entity);
  }

  @Override
  protected Entity delegate() {
    return delegateEntity;
  }

  @Override
  public EntityType getEntityType() {
    return delegateEntity.getEntityType();
  }

  @Override
  public Iterable<String> getAttributeNames() {
    return delegateEntity.getAttributeNames();
  }

  @Override
  public Object getIdValue() {
    return delegateEntity.getIdValue();
  }

  @Override
  public void setIdValue(Object id) {
    delegateEntity.setIdValue(id);
  }

  @Override
  public Object getLabelValue() {
    return delegateEntity.getLabelValue();
  }

  @Override
  public Object get(String attributeName) {
    return delegateEntity.get(attributeName);
  }

  @Override
  public String getString(String attributeName) {
    return delegateEntity.getString(attributeName);
  }

  @Override
  public Integer getInt(String attributeName) {
    return delegateEntity.getInt(attributeName);
  }

  @Override
  public Long getLong(String attributeName) {
    return delegateEntity.getLong(attributeName);
  }

  @Override
  public Boolean getBoolean(String attributeName) {
    return delegateEntity.getBoolean(attributeName);
  }

  @Override
  public Double getDouble(String attributeName) {
    return delegateEntity.getDouble(attributeName);
  }

  @Override
  public Instant getInstant(String attributeName) {
    return delegateEntity.getInstant(attributeName);
  }

  @Override
  public LocalDate getLocalDate(String attributeName) {
    return delegateEntity.getLocalDate(attributeName);
  }

  @Override
  public Entity getEntity(String attributeName) {
    return delegateEntity.getEntity(attributeName);
  }

  @Override
  public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
    return delegateEntity.getEntity(attributeName, clazz);
  }

  @Override
  public Iterable<Entity> getEntities(String attributeName) {
    return delegateEntity.getEntities(attributeName);
  }

  @Override
  public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
    return delegateEntity.getEntities(attributeName, clazz);
  }

  @Override
  public void set(String attributeName, Object value) {
    delegateEntity.set(attributeName, value);
  }

  @Override
  public void set(Entity values) {
    delegateEntity.set(values);
  }
}
