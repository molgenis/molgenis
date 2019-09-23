package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.molgenis.data.Entity;
import org.springframework.validation.AbstractBindingResult;

public class EntityErrors extends AbstractBindingResult {
  private final transient Entity entity;

  public EntityErrors(Entity entity) {
    super("Entity");
    this.entity = requireNonNull(entity);
  }

  @Override
  public Object getTarget() {
    return entity;
  }

  @Override
  protected Object getActualFieldValue(@Nonnull String field) {
    return entity.get(field);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    EntityErrors that = (EntityErrors) o;
    return entity.equals(that.entity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), entity);
  }
}
