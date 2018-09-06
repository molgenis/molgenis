package org.molgenis.data.icd10;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.TreeTraverser;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.molgenis.data.Entity;
import org.springframework.stereotype.Component;

@Component
public class Icd10ClassExpanderImpl implements Icd10ClassExpander {
  private static final String DISEASE_CLASS_CHILDREN_ATTRIBUTE_NAME = "children";

  @Override
  public Collection<Entity> expandClasses(Collection<Entity> diseaseClasses) {
    return diseaseClasses
        .stream()
        .flatMap(this::expandClass)
        .map(this::toDiseaseClass)
        .distinct()
        .map(this::toEntity)
        .collect(toList());
  }

  private Stream<Entity> expandClass(Entity diseaseClass) {
    Iterable<Entity> diseaseClasses =
        new DiseaseClassTreeTraverser().postOrderTraversal(diseaseClass);
    return StreamSupport.stream(diseaseClasses.spliterator(), false);
  }

  private DiseaseClass toDiseaseClass(Entity entity) {
    return new DiseaseClass(entity);
  }

  private Entity toEntity(DiseaseClass diseaseClass) {
    return diseaseClass.getEntity();
  }

  /** Disease class entity wrapper with equals and hashCode implementation. */
  private static class DiseaseClass {
    private final Entity entity;

    private DiseaseClass(Entity entity) {
      this.entity = entity;
    }

    private Entity getEntity() {
      return entity;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      DiseaseClass that = (DiseaseClass) o;

      return entity.getIdValue().equals(that.entity.getIdValue());
    }

    @Override
    public int hashCode() {
      return entity.getIdValue().hashCode();
    }
  }

  private static class DiseaseClassTreeTraverser extends TreeTraverser<Entity> {
    @Override
    public Iterable<Entity> children(@Nonnull Entity diseaseClassEntity) {
      return diseaseClassEntity.getEntities(DISEASE_CLASS_CHILDREN_ATTRIBUTE_NAME);
    }
  }
}
